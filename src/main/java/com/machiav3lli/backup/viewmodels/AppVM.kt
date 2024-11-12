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
package com.machiav3lli.backup.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.entity.Package
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellCommands.Companion.currentProfile
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.ui.compose.MutableComposableStateFlow
import com.machiav3lli.backup.utils.SystemUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppVM(private val database: ODatabase) : ViewModel() {
    private val thePackage: MutableStateFlow<Package?> = MutableStateFlow(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    var appExtras = database.getAppExtrasDao().getFlow(thePackage.value?.packageName).mapLatest {
        it ?: AppExtras(thePackage.value?.packageName ?: "")
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppExtras(thePackage.value?.packageName ?: "")
    )

    val snackbarText = MutableComposableStateFlow(
        "",
        viewModelScope,
        "snackBarText"
    )

    private var notificationId: Int = SystemUtils.now.toInt()
    val refreshNow = mutableStateOf(true)
    val dismissNow = mutableStateOf(false)

    fun setApp(app: Package) {
        viewModelScope.launch { thePackage.update { app } }
    }

    fun uninstallApp() {
        viewModelScope.launch {
            val users = listOf(currentProfile.toString())
            uninstall(users)
            refreshNow.value = true
        }
    }

    private suspend fun uninstall(users: List<String>) {
        val packageName = thePackage.value?.packageName ?: return
        withContext(Dispatchers.IO) {
            thePackage.value?.let { mPackage ->
                Timber.i("uninstalling: ${mPackage.packageLabel}")
                try {
                    ShellCommands.uninstall(
                        users,
                        mPackage.packageName, mPackage.apkPath,
                        mPackage.dataPath, mPackage.isSystem
                    )
                    if (mPackage.backupList.isEmpty()) {
                        database.getAppInfoDao().deleteAllOf(mPackage.packageName)
                        dismissNow.value = true
                    }
                    showNotification(
                        OABX.NB,
                        MainActivityX::class.java,
                        notificationId++,
                        mPackage.packageLabel,
                        OABX.NB.getString(com.machiav3lli.backup.R.string.uninstallSuccess),
                        true
                    )
                } catch (e: ShellCommands.ShellActionFailedException) {
                    showNotification(
                        OABX.NB,
                        MainActivityX::class.java,
                        notificationId++,
                        mPackage.packageLabel,
                        OABX.NB.getString(com.machiav3lli.backup.R.string.uninstallFailure),
                        true
                    )
                    e.message?.let { message -> LogsHandler.logErrors(message) }
                }
            }
        }
    }

    fun enableDisableApp(enable: Boolean) {
        viewModelScope.launch {
            val users = listOf(currentProfile.toString())
            enableDisable(users, enable)
            refreshNow.value = true
        }
    }

    @Throws(ShellCommands.ShellActionFailedException::class)
    private suspend fun enableDisable(users: List<String>, enable: Boolean) {
        withContext(Dispatchers.IO) {
            ShellCommands.enableDisable(users, thePackage.value?.packageName, enable)
        }
    }

    fun deleteBackup(backup: Backup) {              //TODO hg42 launchDeleteBackup ?
        viewModelScope.launch {
            delete(backup)
        }
    }

    private suspend fun delete(backup: Backup) {
        withContext(Dispatchers.IO) {
            thePackage.value?.let { pkg ->
                pkg.deleteBackup(backup)
                if (!pkg.isInstalled && pkg.backupList.isEmpty()) {
                    database.getAppInfoDao().deleteAllOf(pkg.packageName)
                    dismissNow.value = true
                }
            }
        }
    }

    fun deleteAllBackups() {
        viewModelScope.launch {
            deleteAll()
        }
    }

    private suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            thePackage.value?.let { pkg ->
                pkg.deleteAllBackups()
                if (!pkg.isInstalled && pkg.backupList.isEmpty()) {
                    database.getAppInfoDao().deleteAllOf(pkg.packageName)
                    dismissNow.value = true
                }
            }
        }
    }

    fun setExtras(appExtras: AppExtras?) {
        viewModelScope.launch {
            replaceExtras(appExtras)
            refreshNow.value = true
        }
    }

    private suspend fun replaceExtras(appExtras: AppExtras?) {
        withContext(Dispatchers.IO) {
            if (appExtras != null)
                database.getAppExtrasDao().replaceInsert(appExtras)
            else
                thePackage.value?.let {
                    database.getAppExtrasDao().deleteByPackageName(it.packageName)
                }
        }
    }

    fun rewriteBackup(backup: Backup, changedBackup: Backup) {
        viewModelScope.launch {
            rewriteBackupSuspendable(backup, changedBackup)
        }
    }

    private suspend fun rewriteBackupSuspendable(backup: Backup, changedBackup: Backup) {
        withContext(Dispatchers.IO) {
            thePackage.value?.rewriteBackup(backup, changedBackup)
        }
    }
}