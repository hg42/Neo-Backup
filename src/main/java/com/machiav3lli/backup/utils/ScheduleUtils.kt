/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.machiav3lli.backup.ISO_DATE_TIME_FORMAT_MIN
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.getString
import com.machiav3lli.backup.OABX.Companion.runningSchedules
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.dao.ScheduleDao
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.formatTimeTrace
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.preferences.pref_fakeScheduleMin
import com.machiav3lli.backup.preferences.pref_useAlarmClock
import com.machiav3lli.backup.preferences.pref_useExactAlarm
import com.machiav3lli.backup.services.AlarmReceiver
import com.machiav3lli.backup.services.ScheduleService
import com.machiav3lli.backup.traceSchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.max

fun calculateTimeToRunNext(schedule: Schedule, now: Long, next: Boolean = false): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = schedule.timePlaced

    val limitIncrements = 366 / schedule.interval

    val fakeMin = pref_fakeScheduleMin.value

    var minTime = now

    if (fakeMin > 0) {

        minTime += TimeUnit.SECONDS.toMillis(60) // ensure it's not now
        //c[Calendar.HOUR_OF_DAY] = schedule.timeHour
        c[Calendar.MINUTE] = (c[Calendar.MINUTE] / fakeMin + 1) * fakeMin % 60
        c[Calendar.SECOND] = schedule.timeHour
        c[Calendar.MILLISECOND] = schedule.timeMinute
        var increments = 0
        while (c.timeInMillis < minTime && increments++ < limitIncrements) {
            c.add(Calendar.MINUTE, fakeMin)
        }
        traceSchedule {
            "[${schedule.id}] added $increments * ${schedule.interval} min -> ${
                formatTimeTrace(
                    c.timeInMillis
                )
            }"
        }

    } else {

        minTime += TimeUnit.MINUTES.toMillis(60) // ensure it's not now
        c[Calendar.HOUR_OF_DAY] = schedule.timeHour
        c[Calendar.MINUTE] = schedule.timeMinute
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        var increments = 0
        while (c.timeInMillis < minTime && increments++ < limitIncrements) {
            c.add(Calendar.DAY_OF_MONTH, schedule.interval)
        }
        traceSchedule {
            "[${schedule.id}] added $increments * ${schedule.interval} days -> ${
                formatTimeTrace(
                    c.timeInMillis
                )
            }"
        }

    }

    traceSchedule {
        "[${schedule.id}] calculateTimeToRun: next: ${
            formatTimeTrace(c.timeInMillis)
        } now: ${
            formatTimeTrace(now)
        }${
            if (now != minTime) "minimum: ${formatTimeTrace(minTime)}" else ""
        } placed: ${
            formatTimeTrace(schedule.timePlaced)
        } interval: ${
            schedule.interval
        }"
    }
    return c.timeInMillis
}

val updateInterval = 1_000L
val useSeconds = updateInterval < 60_000

fun calcTimeLeft(schedule: Schedule): Pair<String, String> {
    var absTime = ""
    var relTime = ""
    val now = SystemUtils.now
    val at = calculateTimeToRunNext(schedule, now)
    absTime = ISO_DATE_TIME_FORMAT_MIN.format(at)
    val timeDiff = max(at - now, 0)
    val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff).toInt()
    val days = TimeUnit.MILLISECONDS.toDays(timeDiff).toInt()
    if (days != 0) {
        relTime +=
            "${OABX.context.resources.getQuantityString(R.plurals.days_left, days, days)} + "
    }
    val hours = TimeUnit.MILLISECONDS.toHours(timeDiff).toInt() % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff).toInt() % 60
    if (useSeconds && remainingMinutes < 10) {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff).toInt() % 60
        relTime += LocalTime.of(hours, minutes, seconds).toString()
    } else {
        relTime += LocalTime.of(hours, minutes).toString()
    }
    return Pair(absTime, relTime)
}

@Composable
fun timeLeft(
    schedule: Schedule,
    scope: CoroutineScope,
): MutableStateFlow<Pair<String, String>> {
    val state = MutableStateFlow(calcTimeLeft(schedule))

    LaunchedEffect(state) {
        delay(updateInterval)
        state.emit(calcTimeLeft(schedule))
    }

    //traceDebug { state.value.let { "⏳ ${it[0]}  🕒 ${it[1]}" } }

    return state
}

fun setAlarmInSystem(scheduleId: Long, timeForAlarm: Long) {

    val alarmManager = OABX.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val hasPermission: Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    val pendingIntent = createPendingIntent(OABX.context, scheduleId)

    if (hasPermission && pref_useAlarmClock.value) {
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(timeForAlarm, null),
            pendingIntent
        )
        traceSchedule { "[${scheduleId}] alarmManager.setAlarmClock ${formatTimeTrace(timeForAlarm)}" }
    } else {
        if (hasPermission && pref_useExactAlarm.value) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeForAlarm,
                pendingIntent
            )
            traceSchedule {
                "[${scheduleId}] alarmManager.setExactAndAllowWhileIdle ${
                    formatTimeTrace(
                        timeForAlarm
                    )
                }"
            }
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeForAlarm,
                pendingIntent
            )
            traceSchedule {
                "[${scheduleId}] alarmManager.setAndAllowWhileIdle ${
                    formatTimeTrace(
                        timeForAlarm
                    )
                }"
            }
        }
    }

    traceSchedule {
        "[$scheduleId] schedule starting in: ${
            TimeUnit.MILLISECONDS.toMinutes(timeForAlarm - SystemUtils.now)
        } minutes"
    }
}

fun scheduleAlarm(scheduleId: Long, scheduleNext: Boolean) {
    if (scheduleId >= 0) {
        Thread {
            val scheduleDao = OABX.db.getScheduleDao()
            var schedule = scheduleDao.getSchedule(scheduleId)
            if (schedule?.enabled == true) {

                if (scheduleNext) {
                    val now = SystemUtils.now
                    val timePlaced = now + TimeUnit.SECONDS.toMillis(60 + 59)
                    val timeToRunNext = calculateTimeToRunNext(schedule, timePlaced, true)
                    schedule = schedule.copy(
                        timePlaced = timePlaced,
                        timeToRun = timeToRunNext
                    )
                    traceSchedule { "[${schedule.id}] re-scheduling $schedule" }
                    scheduleDao.update(schedule)
                }

                setAlarmInSystem(schedule.id, schedule.timeToRun)

            } else
                traceSchedule { "[$scheduleId] no alarm, schedule is disabled" }
        }.start()
    } else {
        Timber.e("[$scheduleId] no valid scheduleId")
    }
}

fun cancelAlarm(context: Context, scheduleId: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent = createPendingIntent(context, scheduleId)
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
    traceSchedule { "[$scheduleId] cancelled schedule" }
}

fun scheduleAlarms(scheduleNext: Boolean) {
    Thread {
        val scheduleDao = OABX.db.getScheduleDao()
        scheduleDao.getAll()
            .forEach { schedule ->
                // do not set or cancel schedules that are just going to be started
                // (on boot or fresh start from an alarm)
                // setting a past time as alarm will start schedule immediately
                traceSchedule { "[${schedule.id}] ****** alarm for ${schedule.name} <- ${runningSchedules[schedule.id]}" }
                val scheduleAlreadyRuns = runningSchedules[schedule.id] != null
                when {
                    scheduleAlreadyRuns -> {
                        traceSchedule { "[${schedule.id}] *** scheduleAlarms: ignore $schedule, schedule is running now" }
                    }

                    schedule.enabled    -> {
                        traceSchedule { "[${schedule.id}] *** scheduleAlarms: enable $schedule" }
                        scheduleAlarm(schedule.id, scheduleNext)
                    }

                    else                -> {
                        traceSchedule { "[${schedule.id}] *** scheduleAlarms: cancel $schedule" }
                        cancelAlarm(OABX.context, schedule.id)
                    }
                }
            }
    }.start()
}

var alarmsHaveBeenScheduled = false

fun scheduleAlarmsOnce() {

    // schedule alarms only once
    // whichever event comes first:
    //   - any activity started
    //   - after all current schedules are queued
    //   - the app is terminated (too early)
    //   - on a timeout

    if (alarmsHaveBeenScheduled)
        return
    alarmsHaveBeenScheduled = true

    scheduleAlarms(scheduleNext = false)
}


fun createPendingIntent(context: Context, scheduleId: Long): PendingIntent {
    val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
        action = "schedule"
        putExtra("scheduleId", scheduleId)
        addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
    }
    return PendingIntent.getBroadcast(
        context,
        scheduleId.toInt(),
        alarmIntent,
        PendingIntent.FLAG_IMMUTABLE
    )
}


fun startSchedule(schedule: Schedule) {

    OABX.main?.let {

        val database = OABX.db

        val message = StringBuilder()

        message.append(
            "\n${getString(R.string.sched_mode)} ${
                modesToString(
                    OABX.context,
                    modeToModes(schedule.mode)
                )
            }"
        )
        message.append(
            "\n${getString(R.string.backup_filters)} ${
                filterToString(
                    OABX.context,
                    schedule.filter
                )
            }"
        )
        message.append(
            "\n${getString(R.string.other_filters_options)} ${
                specialFilterToString(
                    OABX.context,
                    schedule.specialFilter
                )
            }"
        )
        // TODO list the CL packages
        message.append(
            "\n${getString(R.string.customListTitle)}: ${
                if (schedule.customList.isNotEmpty()) getString(
                    R.string.dialogYes
                ) else getString(R.string.dialogNo)
            }"
        )
        // TODO list the BL packages
        message.append(
            "\n${getString(R.string.sched_blocklist)}: ${
                if (schedule.blockList.isNotEmpty()) getString(
                    R.string.dialogYes
                ) else getString(R.string.dialogNo)
            }"
        )
        message.append(
            "\n${getString(R.string.filters_tags)}: ${
                if (schedule.tagsList.isNotEmpty()) schedule.tagsList.joinToString(",")
                else getString(R.string.dialogNo)
            }"
        )
        AlertDialog.Builder(it)
            .setTitle("${schedule.name}: ${getString(R.string.sched_activateButton)}?")
            .setMessage(message)
            .setPositiveButton(R.string.dialogOK) { _: DialogInterface?, _: Int ->
                if (schedule.mode != MODE_UNSET)
                    StartSchedule(OABX.context, database.getScheduleDao(), schedule.id).execute()
            }
            .setNegativeButton(R.string.dialogCancel) { _: DialogInterface?, _: Int -> }
            .show()
    }
}

internal class StartSchedule(
    val context: Context,
    val scheduleDao: ScheduleDao,
    private val scheduleId: Long,
) :
    ShellCommands.Command {

    override fun execute() {
        Thread {
            val now = SystemUtils.now
            val serviceIntent = Intent(context, ScheduleService::class.java)
            scheduleDao.getSchedule(scheduleId)?.let { schedule ->
                serviceIntent.putExtra("scheduleId", scheduleId)
                serviceIntent.putExtra("name", schedule.getBatchName(now))
                context.startService(serviceIntent)
            }
        }.start()
    }
}

