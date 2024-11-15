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
package com.machiav3lli.backup.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.repository.PackageRepository
import com.machiav3lli.backup.entity.Package
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.preferences.pref_autoLogUnInstallBroadcast
import com.machiav3lli.backup.preferences.supportLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// TODO make main way of refresh & handle new installed and backup list
class PackageUnInstalledReceiver : BroadcastReceiver(), KoinComponent {
    private val packageRepository: PackageRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val packageName =
                intent.data?.let { if (it.scheme == "package") it.schemeSpecificPart else null }
            if (packageName != null) {
                Package.invalidateSystemCacheForPackage(packageName)
                when (intent.action.orEmpty()) {
                    Intent.ACTION_PACKAGE_ADDED,
                    Intent.ACTION_PACKAGE_REPLACED,
                        -> {
                        context.packageManager.getPackageInfo(
                            packageName,
                            PackageManager.GET_PERMISSIONS
                        )?.let { packageInfo ->
                            val appInfo = AppInfo(context, packageInfo)
                            GlobalScope.launch(Dispatchers.IO) {
                                packageRepository.upsertAppInfo(appInfo)
                            }
                        }
                    }

                    Intent.ACTION_PACKAGE_REMOVED,
                        -> {
                        GlobalScope.launch(Dispatchers.IO) {
                            val backups = packageRepository.getBackups(packageName)
                            if (backups.isEmpty())
                                packageRepository.deleteAppInfoOf(packageName)
                            else {
                                val appInfo = backups.maxBy { it.backupDate }.toAppInfo()
                                packageRepository.upsertAppInfo(appInfo)
                            }
                        }
                    }
                }

                if (pref_autoLogUnInstallBroadcast.value) {
                    GlobalScope.launch(Dispatchers.IO) {
                        delay(60_0000)
                        supportLog("PackageUnInstalledReceiver")
                    }
                }
            }
        } catch (e: Throwable) {
            //TODO how to communicate that error to the main app?
            // it currently leads to a "missing directories from PM" error,
            // even when only restoring the apk
            logException(e)
        }
    }
}
