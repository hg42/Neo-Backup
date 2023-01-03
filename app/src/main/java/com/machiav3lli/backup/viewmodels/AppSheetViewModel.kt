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

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.showNotification
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.ui.compose.MutableComposableFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppSheetViewModel(
    app: Package?,
    private val database: ODatabase,
    private var shellCommands: ShellCommands,
    private val appContext: Application
) : AndroidViewModel(appContext) {

    var thePackage = flow<Package?> { app }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        app
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    var appExtras = database.appExtrasDao.getFlow(app?.packageName).mapLatest {
        it ?: AppExtras(app?.packageName ?: "")
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppExtras(app?.packageName ?: "")
    )

    val snackbarText = MutableComposableFlow(
        "",
        viewModelScope,
        "snackBarText"
    )

    private var notificationId: Int = System.currentTimeMillis().toInt()
    val refreshNow = mutableStateOf(true)

    fun uninstallApp() {
        viewModelScope.launch {
            uninstall()
            refreshNow.value = true
        }
    }

    private suspend fun uninstall() {
        withContext(Dispatchers.IO) {
            thePackage.value?.let { mPackage ->
                Timber.i("uninstalling: ${mPackage.packageLabel}")
                try {
                    shellCommands.uninstall(
                        mPackage.packageName, mPackage.apkPath,
                        mPackage.dataPath, mPackage.isSystem
                    )
                    showNotification(
                        appContext,
                        MainActivityX::class.java,
                        notificationId++,
                        mPackage.packageLabel,
                        appContext.getString(com.machiav3lli.backup.R.string.uninstallSuccess),
                        true
                    )
                } catch (e: ShellCommands.ShellActionFailedException) {
                    showNotification(
                        appContext,
                        MainActivityX::class.java,
                        notificationId++,
                        mPackage.packageLabel,
                        appContext.getString(com.machiav3lli.backup.R.string.uninstallFailure),
                        true
                    )
                    e.message?.let { message -> LogsHandler.logErrors(message) }
                }
            }
        }
    }

    fun enableDisableApp(users: MutableList<String>, enable: Boolean) {
        viewModelScope.launch {
            enableDisable(users, enable)
            refreshNow.value = true
        }
    }

    @Throws(ShellCommands.ShellActionFailedException::class)
    private suspend fun enableDisable(users: MutableList<String>, enable: Boolean) {
        withContext(Dispatchers.IO) {
            shellCommands.enableDisablePackage(thePackage.value?.packageName, users, enable)
        }
    }

    fun getUsers(): Array<String> {
        return shellCommands.getUsers()?.toTypedArray() ?: arrayOf()
    }

    fun deleteBackup(backup: Backup) {
        viewModelScope.launch {
            delete(backup)
            refreshNow.value = true
        }
    }

    private suspend fun delete(backup: Backup) {
        withContext(Dispatchers.IO) {
            thePackage.value?.deleteBackup(backup)
        }
    }

    fun deleteAllBackups() {
        viewModelScope.launch {
            deleteAll()
            refreshNow.value = true
        }
    }

    private suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            thePackage.value?.deleteAllBackups()
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
                database.appExtrasDao.replaceInsert(appExtras)
            else
                thePackage.value?.let { database.appExtrasDao.deleteByPackageName(it.packageName) }
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

    class Factory(
        private val packageInfo: Package?,
        private val database: ODatabase,
        private val shellCommands: ShellCommands,
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppSheetViewModel::class.java)) {
                return AppSheetViewModel(packageInfo, database, shellCommands, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}