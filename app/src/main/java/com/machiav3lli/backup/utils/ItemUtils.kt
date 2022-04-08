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

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.format.Formatter
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.SheetAppBinding
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.items.Package
import timber.log.Timber

val COLOR_UPDATE  = Color.parseColor("#FFFF33")
val COLOR_SYSTEM  = Color.parseColor("#BB66FF")
val COLOR_USER    = Color.parseColor("#FF9966")
val COLOR_SPECIAL = Color.parseColor("#DDAA00")
const val COLOR_DISABLED = Color.RED
const val COLOR_UNINSTALLED = Color.DKGRAY

fun SheetAppBinding.pickSheetDataSizes(context: Context, app: Package, update: Boolean) {
    if (app.isSpecial || !app.isInstalled) {
        appSizeLine.changeVisibility(View.GONE, update)
        dataSizeLine.changeVisibility(View.GONE, update)
        splitsLine.changeVisibility(View.GONE, update)
        cacheSizeLine.changeVisibility(View.GONE, update)
    } else {
        try {
            appSize.text = Formatter.formatFileSize(
                context, app.storageStats?.appBytes
                    ?: 0
            )
            dataSize.text = Formatter.formatFileSize(
                context, (app.storageStats?.dataBytes ?: 0) - (app.storageStats?.cacheBytes ?: 0)
            )
            cacheSize.text = Formatter.formatFileSize(
                context, app.storageStats?.cacheBytes
                    ?: 0
            )
            if (app.storageStats?.cacheBytes == 0L) {
                wipeCache.changeVisibility(View.INVISIBLE, update)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e("Package ${app.packageName} is not installed? Exception: $e")
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, app)
        }
    }
}

fun SheetAppBinding.pickSheetVersionName(app: Package) {
    if (app.isUpdated) {
        val latestBackupVersion = app.latestBackup?.versionName
        val updatedVersionString = "$latestBackupVersion (${app.versionName})"
        versionName.text = updatedVersionString
        versionName.setTextColor(COLOR_UPDATE)
    } else {
        versionName.text = app.versionName
        versionName.setTextColor(packageName.textColors)
    }
}

fun AppCompatTextView.pickSheetAppType(app: Package) {
    var color: Int
    if (app.isInstalled) {
        color = when {
            app.isSpecial -> COLOR_SPECIAL
            app.isSystem -> COLOR_SYSTEM
            else -> COLOR_USER
        }
        if (app.isDisabled) {
            color = COLOR_DISABLED
        }
    } else {
        color = COLOR_UNINSTALLED
    }
    setTextColor(color)
}

fun getStats(appsList: MutableList<Package>): Triple<Int, Int, Int> {
    var backupsNumber = 0
    var updatedNumber = 0
    appsList.forEach {
        if (it.hasBackups) {
            backupsNumber += it.backupHistory.size
            if (it.isUpdated) updatedNumber += 1
        }
    }
    return Triple(appsList.size, backupsNumber, updatedNumber)
}

fun PackageManager.getInstalledPackagesWithPermissions() =
    getInstalledPackages(0).map { getPackageInfo(it.packageName, PackageManager.GET_PERMISSIONS) }

fun List<AppExtras>.get(packageName: String) =
    find { it.packageName == packageName } ?: AppExtras(packageName)

fun Int.itemIdToOrder(): Int = when (this) {
    R.id.backupFragment, R.id.serviceFragment -> 1
    R.id.restoreFragment, R.id.advancedFragment -> 2
    R.id.schedulerFragment, R.id.toolsFragment -> 3
    else -> 0 // R.id.homeFragment, R.id.userFragment
}