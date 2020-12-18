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
package com.machiav3lli.backup.actions

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import com.machiav3lli.backup.classTag
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.utils.LogUtils
import com.topjohnwu.superuser.Shell
import timber.log.Timber

abstract class BaseAppAction protected constructor(protected val context: Context, protected val shell: ShellHandler) {

    protected val deviceProtectedStorageContext: Context = context.createDeviceProtectedStorageContext()

    fun getBackupArchiveFilename(what: String, isEncrypted: Boolean): String {
        return "$what.tar.gz${if (isEncrypted) ".enc" else ""}"
    }

    fun prependUtilbox(command: String?): String {
        return "${shell.utilboxPath} $command"
    }

    abstract class AppActionFailedException : Exception {
        protected constructor(message: String?) : super(message) {}
        protected constructor(message: String?, cause: Throwable?) : super(message, cause) {}
    }

    @SuppressLint("DefaultLocale")
    open fun preprocessPackage(packageName: String) {
        try {
            val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            Timber.i(String.format("package %s uid %d", packageName, applicationInfo.uid))
            if (applicationInfo.uid < 10000) { // exclude several system users, e.g. system, radio
                Timber.w("Requested to kill processes of UID 1000. Refusing to kill system's processes!")
                return
            }
            if (!doNotStop.contains(packageName)) { // will stop most activity, needs a good blacklist
                // pause corresponding processes (but files may still be in the middle and buffers contain unwritten data)
                //   also pauses essential processes (because some uids are shared between apps and essential services)
                //ShellHandler.runAsRoot(String.format("ps -o PID -u %d | grep -v PID | xargs kill -STOP", applicationInfo.uid));
                //   try to exclude essential services android.* via grep
                runAsRoot(String.format("ps -o PID,USER,NAME -u %d | grep -v -E ' PID | android\\.|\\.providers\\.|systemui' | while read pid user name; do kill -STOP \$pid ; done", applicationInfo.uid))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.w("$packageName does not exist. Cannot preprocess!")
        } catch (e: ShellCommandFailedException) {
            Timber.w("Could not stop package " + packageName + ": " + java.lang.String.join(" ", e.shellResult.err))
        } catch (e: Throwable) {
            LogUtils.unhandledException(e)
        }
    }

    @SuppressLint("DefaultLocale")
    open fun postprocessPackage(packageName: String) {
        try {
            val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            runAsRoot(String.format(
                    "ps -o PID,USER,NAME -u %d | grep -v -E ' PID | android\\.|\\.providers\\.|systemui' | while read pid user name; do kill -CONT \$pid ; done",
                    applicationInfo.uid
            ))
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.w("$packageName does not exist. Cannot preprocess!")
        } catch (e: ShellCommandFailedException) {
            Timber.w("Could not continue package $packageName: ${e.shellResult.err.joinToString(separator = " ")}")
        } catch (e: Throwable) {
            LogUtils.unhandledException(e)
        }
    }

    companion object {
        private val TAG = classTag(".BaseAppAction")
        const val MODE_UNSET = 0
        const val MODE_APK = 1
        const val MODE_DATA = 2
        const val MODE_BOTH = 3
        const val BACKUP_DIR_DATA = "data"
        const val BACKUP_DIR_DEVICE_PROTECTED_FILES = "device_protected_files"
        const val BACKUP_DIR_EXTERNAL_FILES = "external_files"
        const val BACKUP_DIR_OBB_FILES = "obb_files"
        val DATA_EXCLUDED_DIRS = listOf("cache", "code_cache", "lib")
        private val doNotStop = listOf(
                BuildConfig.APPLICATION_ID, // ignore own package, it must run
                "com.android.shell",        // don't remove this
                "com.android.systemui",
                "com.android.externalstorage",
                "com.android.providers.media",
                "com.google.android.gms",
                "com.google.android.gsf"
        )

        fun extractErrorMessage(shellResult: Shell.Result): String {
            // if stderr does not say anything, try stdout
            val err = if (shellResult.err.isEmpty()) shellResult.out else shellResult.err
            return if (err.isEmpty()) {
                "Unknown Error"
            } else err[err.size - 1]
        }
    }
}