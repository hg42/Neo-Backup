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
package com.machiav3lli.backup.schedules

import android.content.Context
import android.content.SharedPreferences
import android.os.PowerManager
import android.util.Log
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.activities.SchedulerActivityX
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.handler.BackendController
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.BackupRestoreHelper.OnBackupRestoreListener
import com.machiav3lli.backup.handler.NotificationHelper
import com.machiav3lli.backup.items.ActionResult
import com.machiav3lli.backup.items.AppInfoX
import com.machiav3lli.backup.schedules.CustomPackageList.getScheduleCustomList
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.LogUtils
import com.machiav3lli.backup.utils.PrefUtils
import com.machiav3lli.backup.utils.PrefUtils.StorageLocationNotConfiguredException

class HandleScheduledBackups(private val context: Context) {
    private val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val prefs: SharedPreferences = PrefUtils.getDefaultSharedPreferences(context)
    private val listeners: MutableList<OnBackupRestoreListener>

    init {
        listeners = ArrayList()
    }

    fun setOnBackupListener(listener: OnBackupRestoreListener) {
        listeners.add(listener)
    }

    fun initiateBackup(id: Int, mode: Schedule.Mode?, subMode: Int, excludeSystem: Boolean, enableCustomList: Boolean) {
        Thread(Runnable {
            val notificationId = System.currentTimeMillis().toInt()
            NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId, context.getString(R.string.fetching_backup_list), "", true)
            val list: List<AppInfoX>
            list = try {
                BackendController.getApplicationList(context)
            } catch (e: BackupLocationIsAccessibleException) {
                Log.e(TAG, String.format("Scheduled backup failed due to %s: %s", e.javaClass.simpleName, e))
                LogUtils.logErrors(context, e.toString())
                return@Runnable
            } catch (e: StorageLocationNotConfiguredException) {
                Log.e(TAG, String.format("Scheduled backup failed due to %s: %s", e.javaClass.simpleName, e))
                LogUtils.logErrors(context, e.toString())
                return@Runnable
            }
            val selectedPackages = getScheduleCustomList(context, id)
            val inCustomList = { packageName: String -> !enableCustomList || selectedPackages!!.contains(packageName) }
            val predicate: (AppInfoX) -> Boolean = when (mode) {
                Schedule.Mode.USER -> { appInfoX: AppInfoX ->
                    appInfoX.isInstalled &&
                            !appInfoX.isSystem && inCustomList(appInfoX.packageName)
                }
                Schedule.Mode.SYSTEM -> { appInfoX: AppInfoX ->
                    appInfoX.isInstalled &&
                            appInfoX.isSystem && inCustomList(appInfoX.packageName)
                }
                Schedule.Mode.NEW_UPDATED -> { appInfoX: AppInfoX ->
                    (appInfoX.isInstalled && (!excludeSystem || !appInfoX.isSystem)
                            && (!appInfoX.hasBackups || appInfoX.isUpdated)
                            && inCustomList(appInfoX.packageName))
                }
                else -> { appInfoX: AppInfoX -> inCustomList(appInfoX.packageName) }
            }
            val listToBackUp = list
                    .filter(predicate)
                    .toList()
                    .sortedWith { m1: AppInfoX, m2: AppInfoX -> m1.packageLabel.compareTo(m2.packageLabel, ignoreCase = true) }
            startScheduledBackup(listToBackUp, subMode, notificationId)
        }).start()
    }

    fun startScheduledBackup(backupList: List<AppInfoX>, subMode: Int, notificationId: Int) {
        if (PrefUtils.checkStoragePermissions(context)) {
            Thread {
                Log.i(TAG, "Starting scheduled backup for " + backupList.size + " items")
                val wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
                if (prefs.getBoolean("acquireWakelock", true)) {
                    wl.acquire(60 * 60 * 1000L /*60 minutes*/)
                    Log.i(TAG, "wakelock acquired")
                }
                try {
                    val totalOfActions = backupList.size
                    var i = 1
                    val blacklistsDBHelper = BlacklistsDBHelper(context)
                    val db = blacklistsDBHelper.readableDatabase
                    val blacklistedPackages = blacklistsDBHelper.getBlacklistedPackages(db, SchedulerActivityX.GLOBALBLACKLISTID)
                    val results: MutableList<ActionResult> = mutableListOf()
                    var packageLabel = "NONE"
                    try {
                        backupList.forEach { appInfo ->
                            packageLabel = appInfo.packageLabel
                            if (blacklistedPackages.contains(appInfo.packageName)) {
                                Log.i(TAG, "${appInfo.packageName} ignored")
                                i++
                                return@forEach
                            }
                            val title = context.getString(R.string.backupProgress) + " (" + i + "/" + totalOfActions + ")"
                            NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId, title, appInfo.packageLabel, false)
                            val backupRestoreHelper = BackupRestoreHelper()
                            var result: ActionResult? = null
                            try {
                                result = backupRestoreHelper.backup(context, MainActivityX.shellHandlerInstance!!, appInfo, subMode)
                            } catch (e: Throwable) {
                                result = ActionResult(appInfo, null, "not processed: $packageLabel: $e", false)
                                Log.w(TAG, "package: ${appInfo.packageLabel} result: $e")
                            } finally {
                                if (!result!!.succeeded)
                                    NotificationHelper.showNotification(context, MainActivityX::class.java, result!!.hashCode(), appInfo.packageLabel, result!!.message, false)
                            }
                            results.add(result)
                            i++
                        }
                    } catch (e: Throwable) {
                        LogUtils.unhandledException(e, packageLabel)
                    } finally {
                        // Calculate the overall result
                        val errors = results
                                .map { it.message }
                                .filter { it.isNotEmpty() }
                                .joinToString(separator = "\n")
                        val overAllResult = ActionResult(null, null, errors, results.parallelStream().anyMatch(ActionResult::succeeded))

                        // Update the notification
                        val notificationTitle = if (overAllResult.succeeded) context.getString(R.string.batchSuccess) else context.getString(R.string.batchFailure)
                        val notificationMessage = context.getString(R.string.sched_notificationMessage)
                        NotificationHelper.showNotification(context, MainActivityX::class.java, notificationId, notificationTitle, notificationMessage, true)
                        if (!overAllResult.succeeded) {
                            LogUtils.logErrors(context, errors)
                        }
                        for (l in listeners) l.onBackupRestoreDone()
                        blacklistsDBHelper.close()
                    }
                } catch (e: Throwable) {
                    LogUtils.unhandledException(e)
                } finally {
                    if (wl.isHeld) {
                        wl.release()
                        Log.i(TAG, "wakelock released")
                    }
                }
            }.start()
        }
    }

    companion object {
        private val TAG = classTag(".HandleScheduledBackups")
    }
}