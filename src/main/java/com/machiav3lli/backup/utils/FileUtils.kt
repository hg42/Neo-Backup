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
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.backupRoot
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.SpecialInfo
import com.machiav3lli.backup.entity.Package
import com.machiav3lli.backup.entity.StorageFile
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.findBackups
import com.machiav3lli.backup.handler.updateAppTables
import java.io.File
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

object FileUtils {

    // TODO Change to StorageFile-based
    fun getExternalStorageDirectory(context: Context): File? {
        return context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile?.parentFile
    }

    fun getName(fullPath: String): String {
        var path = fullPath
        if (path.endsWith(File.separator)) path = path.substring(0, path.length - 1)
        return path.substring(path.lastIndexOf(File.separator) + 1)
    }

    fun translatePosixPermissionToMode(permissions: String): Int {
        var str = permissions.takeLast(9)
        str = str.replace('s', 'x', false)
        str = str.replace('S', '-', false)
        val set = PosixFilePermissions.fromString(str)
        return translatePosixPermissionToMode(set)
    }

    fun translatePosixPermissionToMode(permissions: Set<PosixFilePermission?>): Int {
        var mode = 0
        PosixFilePermission.values().forEach {
            mode = mode shl 1
            mode += if (permissions.contains(it)) 1 else 0
        }
        return mode
    }

    //TODO hg42 move the following to somewhere else, maybe a class that handles (multiple) BackupLocations
    //TODO hg42 this may work (after invalidateBackups or on startup)
    //TODO hg42 but should probably check an empty backups map instead or additionally?
    //TODO hg42 the name does not reflect all cases
    fun ensureBackups(): Map<String, List<Backup>> {
        runCatching {
            if (backupRoot == null)
                OABX.context.findBackups()
        }
        return OABX.getBackups()
    }

    /**
     * Invalidates the cached value for the backup location URI so that the next call to
     * `getBackupDir` will set it again.
     */
    fun invalidateBackupLocation() {
        Package.invalidateBackupCacheForPackage()
        SpecialInfo.clearCache()
        backupRoot = null // after clearing caches, because they probably need the location
        try {
            // updateAppTables does ensureBackups, but make intention clear here
            OABX.context.findBackups()
            OABX.context.updateAppTables()
        } catch (e: Throwable) {
            LogsHandler.logException(e, backTrace = true)
        }
    }

    class BackupLocationInAccessibleException : Exception {
        constructor() : super()
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
    }
}
