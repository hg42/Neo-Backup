package com.machiav3lli.backup.tasks

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.backup.ACTION_CANCEL_SCHEDULE
import com.machiav3lli.backup.EXTRA_NAME
import com.machiav3lli.backup.EXTRA_SCHEDULE_ID
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.beginLogSection
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.handler.getInstalledPackageList
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.preferences.pref_autoLogAfterSchedule
import com.machiav3lli.backup.preferences.pref_autoLogSuspicious
import com.machiav3lli.backup.preferences.pref_fakeScheduleDups
import com.machiav3lli.backup.preferences.pref_useForegroundInService
import com.machiav3lli.backup.preferences.supportInfo
import com.machiav3lli.backup.preferences.textLog
import com.machiav3lli.backup.preferences.traceSchedule
import com.machiav3lli.backup.services.CommandReceiver
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.SystemUtils
import com.machiav3lli.backup.utils.calculateTimeToRun
import com.machiav3lli.backup.utils.filterPackages
import com.machiav3lli.backup.utils.scheduleAlarmsOnce
import com.machiav3lli.backup.utils.scheduleNext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class ScheduleWork(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    val database: ODatabase by inject()

    private var scheduleId = inputData.getLong(EXTRA_SCHEDULE_ID, -1L)
    private val notificationId = SystemUtils.now.toInt()
    private var notification: Notification? = null
    private val scheduleJob = Job()

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = createForegroundNotification()
        return ForegroundInfo(
            notification.hashCode(),
            notification,
            if (OABX.minSDK(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO + scheduleJob) {
        try {
            scheduleId = inputData.getLong(EXTRA_SCHEDULE_ID, -1L)
            val name = inputData.getString(EXTRA_NAME) ?: ""

            OABX.wakelock(true)

            traceSchedule {
                buildString {
                    append("[$scheduleId] %%%%% ScheduleWork starting for name='$name'")
                }
            }

            if (scheduleId < 0) {
                return@withContext Result.failure()
            }

            if (runningSchedules[scheduleId] == true) {
                val message =
                    "[$scheduleId] duplicate schedule detected: $name (as designed, ignored)"
                Timber.w(message)
                if (pref_autoLogSuspicious.value) {
                    textLog(
                        listOf(
                            message,
                            "--- autoLogSuspicious $scheduleId $name"
                        ) + supportInfo()
                    )
                }
                return@withContext Result.failure()
            }

            runningSchedules[scheduleId] = false

            repeat(1 + pref_fakeScheduleDups.value) {
                val now = SystemUtils.now
                scheduleNext(context, scheduleId, true)

                val result = processSchedule(name, now)
                if (!result) {
                    return@withContext Result.failure()
                }
            }

            scheduleAlarmsOnce()
            OABX.wakelock(false)

            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        } finally {
            OABX.wakelock(false)
        }
    }

    private suspend fun processSchedule(name: String, now: Long): Boolean =
        coroutineScope {
            val scheduleDao = database.getScheduleDao()

            val schedule = scheduleDao.getSchedule(scheduleId)

            val selectedItems = schedule?.let { getFilteredPackages(it) } ?: emptyList()

            if (selectedItems.isEmpty()) {
                handleEmptySelectedItems(name)
                return@coroutineScope false
            }

            val worksList = mutableListOf<OneTimeWorkRequest>()
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)

            val batchName = WorkHandler.getBatchName(name, now)
            OABX.work.beginBatch(batchName)

            var errors = ""
            var resultsSuccess = true
            var finished = 0
            val queued = selectedItems.size

            val workJobs = selectedItems.map { packageName ->
                val oneTimeWorkRequest = AppActionWork.Request(
                    packageName = packageName,
                    mode = schedule?.mode ?: MODE_UNSET,
                    backupBoolean = true,
                    notificationId = notificationId,
                    batchName = batchName,
                    immediate = false
                )
                worksList.add(oneTimeWorkRequest)

                async {
                    OABX.work.manager.getWorkInfoByIdFlow(oneTimeWorkRequest.id)
                        .collectLatest { workInfo ->
                            when (workInfo?.state) {
                                androidx.work.WorkInfo.State.SUCCEEDED,
                                androidx.work.WorkInfo.State.FAILED,
                                androidx.work.WorkInfo.State.CANCELLED -> {
                                    finished++
                                    val succeeded =
                                        workInfo.outputData.getBoolean("succeeded", false)
                                    val packageLabel =
                                        workInfo.outputData.getString("packageLabel") ?: ""
                                    val error = workInfo.outputData.getString("error") ?: ""

                                    if (error.isNotEmpty()) {
                                        errors = "$errors$packageLabel: ${
                                            LogsHandler.handleErrorMessages(
                                                context,
                                                error
                                            )
                                        }\n"
                                    }
                                    resultsSuccess = resultsSuccess && succeeded

                                    if (finished >= queued) {
                                        endSchedule(name, "all jobs finished")
                                    }
                                }

                                else                                   -> {}
                            }
                        }
                }
            }

            if (worksList.isNotEmpty()) {
                if (beginSchedule(name, "queueing work")) {
                    OABX.work.manager
                        .beginWith(worksList)
                        .enqueue()
                    workJobs.awaitAll()
                    true
                } else {
                    endSchedule(name, "duplicate detected")
                    false
                }
            } else {
                beginSchedule(name, "no work")
                endSchedule(name, "no work")
                false
            }
        }

    private suspend fun getFilteredPackages(schedule: Schedule): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                FileUtils.ensureBackups()

                val blacklistDao = database.getBlocklistDao()
                val extrasDao = database.getAppExtrasDao()

                val customBlocklist = schedule.blockList
                val globalBlocklist = blacklistDao.getBlocklistedPackages(PACKAGES_LIST_GLOBAL_ID)
                val blockList = globalBlocklist.plus(customBlocklist)
                val extrasMap = extrasDao.getAll().associateBy(AppExtras::packageName)
                val allTags = extrasDao.getAll().flatMap { it.customTags }.distinct()
                val tagsList = schedule.tagsList.filter { it in allTags }

                val unfilteredPackages = context.getInstalledPackageList()

                filterPackages(
                    packages = unfilteredPackages,
                    extrasMap = extrasMap,
                    filter = schedule.filter,
                    specialFilter = schedule.specialFilter,
                    whiteList = schedule.customList.toList(),
                    blackList = blockList,
                    tagsList = tagsList,
                ).map { it.packageName }

            } catch (e: FileUtils.BackupLocationInAccessibleException) {
                Timber.e("Schedule failed: ${e.message}")
                emptyList()
            } catch (e: StorageLocationNotConfiguredException) {
                Timber.e("Schedule failed: ${e.message}")
                emptyList()
            }
        }
    }

    private fun handleEmptySelectedItems(name: String) {
        beginSchedule(name, "no work")
        endSchedule(name, "no work")
        showNotification(
            context,
            MainActivityX::class.java,
            notificationId,
            context.getString(R.string.schedule_failed),
            context.getString(R.string.empty_filtered_list),
            false
        )
        traceSchedule { "[$scheduleId] no packages matching" }
    }

    private fun beginSchedule(name: String, details: String = ""): Boolean {
        return if (OABX.runningSchedules[scheduleId] != true) {
            OABX.runningSchedules[scheduleId] = true
            beginLogSection("schedule $name")
            true
        } else {
            val message =
                "duplicate schedule detected: id=$scheduleId name='$name' (late, ignored) $details"
            Timber.w(message)
            if (pref_autoLogSuspicious.value)
                textLog(
                    listOf(
                        message,
                        "--- autoLogAfterSchedule $scheduleId $name${if (details.isEmpty()) "" else " ($details)"}"
                    ) + supportInfo()
                )
            false
        }
    }

    private fun endSchedule(name: String, details: String = "") {
        if (OABX.runningSchedules[scheduleId] != null) {
            OABX.runningSchedules.remove(scheduleId)
            if (pref_autoLogAfterSchedule.value) {
                textLog(
                    listOf(
                        "--- autoLogAfterSchedule id=$scheduleId name=$name${if (details.isEmpty()) "" else " ($details)"}"
                    ) + supportInfo()
                )
            }
            OABX.endLogSection("schedule $name")
        } else {
            traceSchedule { "[$scheduleId] duplicate schedule end: name='$name'${if (details.isEmpty()) "" else " ($details)"}" }
        }
    }

    private fun createForegroundNotification(): Notification {
        if (notification != null) return notification!!

        if (pref_useForegroundInService.value) {
            createNotificationChannel()
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivityX::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelIntent = Intent(context, CommandReceiver::class.java).apply {
            action = ACTION_CANCEL_SCHEDULE
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            cancelIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.sched_notificationMessage))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                R.drawable.ic_close,
                context.getString(R.string.dialogCancel),
                cancelPendingIntent
            )
            .build()
            .also { notification = it }
    }

    private fun createNotificationChannel() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(notificationChannel)
    }

    companion object {
        private val CHANNEL_ID = ScheduleWork::class.java.name
        private const val SCHEDULE_WORK = "schedule_work_"
        private val runningSchedules = ConcurrentHashMap<Long, Boolean>()

        fun schedule(context: Context, schedule: Schedule, immediately: Boolean = false) {
            val workManager = WorkManager.getInstance(context)
            // Cancel any existing work for this schedule
            //workManager.cancelUniqueWork("$SCHEDULE_WORK${schedule.id}")

            if (!schedule.enabled && !immediately) return

            val timeToRun = calculateTimeToRun(schedule, SystemUtils.now)
            val initialDelay = timeToRun - SystemUtils.now

            val constraints = Constraints.Builder()
                .setRequiresCharging(false)
                .build()

            val scheduleWorkRequest = OneTimeWorkRequestBuilder<ScheduleWork>()
                .setInputData(
                    workDataOf(
                        EXTRA_SCHEDULE_ID to schedule.id,
                        EXTRA_NAME to schedule.name,
                    )
                )
                .setInitialDelay(Duration.ofMillis(if (immediately) 0L else initialDelay))
                .setConstraints(constraints)
                .addTag("schedule_${schedule.id}")
                .build()

            workManager.enqueueUniqueWork(
                "$SCHEDULE_WORK${schedule.id}",
                ExistingWorkPolicy.REPLACE,
                scheduleWorkRequest
            )
            traceSchedule {
                "[${schedule.id}] schedule starting in: ${
                    TimeUnit.MILLISECONDS.toMinutes(
                        if (immediately) 0L
                        else (timeToRun - SystemUtils.now)
                    )
                } minutes name=${schedule.name}"
            }
        }

        fun scheduleAll(context: Context) {
            Thread {
                val scheduleDao = OABX.db.getScheduleDao()
                scheduleDao.getAll().forEach { schedule ->
                    val scheduleAlreadyRuns = runningSchedules[schedule.id] == true
                    when {
                        scheduleAlreadyRuns -> {
                            traceSchedule { "[${schedule.id}]: ignore $schedule" }
                        }

                        schedule.enabled    -> {
                            traceSchedule { "[${schedule.id}]: enable $schedule" }
                            schedule(context, schedule)
                        }

                        else                -> {
                            traceSchedule { "[${schedule.id}]: cancel $schedule" }
                            cancel(context, schedule.id)
                        }
                    }
                }
            }.start()
        }

        fun cancel(context: Context, scheduleId: Long) {
            traceSchedule { "[$scheduleId]: Canceling" }
            WorkManager.getInstance(context)
                .cancelUniqueWork("$SCHEDULE_WORK$scheduleId")
        }
    }
}