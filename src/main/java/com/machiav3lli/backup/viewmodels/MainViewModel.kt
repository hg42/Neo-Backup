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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Blocklist
import com.machiav3lli.backup.handler.toPackageList
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.Package.Companion.invalidateCacheForPackage
import com.machiav3lli.backup.preferences.pref_newAndUpdatedNotification
import com.machiav3lli.backup.preferences.pref_skipBackupsDatabase
import com.machiav3lli.backup.traceBackups
import com.machiav3lli.backup.traceFlows
import com.machiav3lli.backup.ui.compose.MutableComposableFlow
import com.machiav3lli.backup.ui.compose.item.IconCache
import com.machiav3lli.backup.utils.TraceUtils.classAndId
import com.machiav3lli.backup.utils.TraceUtils.formatSortedBackups
import com.machiav3lli.backup.utils.TraceUtils.trace
import com.machiav3lli.backup.utils.applyFilter
import com.machiav3lli.backup.utils.sortFilterModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import timber.log.Timber

class MainViewModel(
    private val db: ODatabase,
    private val appContext: Application,
) : AndroidViewModel(appContext) {

    init {
        Timber.w("==================== ${classAndId(this)}")
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - FLOWS

    // most flows are for complete states, so skipping (conflate, mapLatest) is usually allowed
    // it's noted otherwise
    // conflate:
    //   takes the latest item and processes it completely, then takes the next (latest again)
    //   if input rate is f_in and processing can run at max rate f_proc,
    //   then with f_in > f_proc the results will only come out with about f_proc
    // mapLatest: (use mapLatest { it } as an equivalent form similar to conflate())
    //   kills processing the item, when a new one comes in
    //   so, as long as items come in faster than processing time, there won't be results, in short:
    //   if f_in > f_proc, then there is no output at all
    //   this is much like processing on idle only

    val schedulesDb =
        //------------------------------------------------------------------------------------------ blocklist
        db.getScheduleDao().getAllFlow()
            .trace { "*** schedulesDb <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    val blocklistDb =
        //------------------------------------------------------------------------------------------ blocklist
        db.getBlocklistDao().getAllFlow()
            .trace { "*** blocklistDb <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    //TODO wech
    @OptIn(ExperimentalCoroutinesApi::class)
    val backupsDb =
        //------------------------------------------------------------------------------------------ backupsMap
        if (pref_skipBackupsDatabase.value)
            MutableSharedFlow()
        else {
            db.getBackupDao().getAllFlow()
                .mapLatest { it.groupBy(Backup::packageName) }
                .trace { "*** backupsDb <<- p=${it.size} b=${it.map { it.value.size }.sum()}" }
                //.trace { "*** backupsDb <<- p=${it.size} b=${it.map { it.value.size }.sum()} #################### egg ${showSortedBackups(it["com.android.egg"])}" }  // for testing use com.android.egg
                .stateIn(
                    viewModelScope + Dispatchers.IO,
                    SharingStarted.Eagerly,
                    emptyMap()
                )
        }

    val appInfosDb = db.getAppInfoDao().getAllFlow()
        .trace { "*** appInfosDb <<- ${it.size}" }
        .stateIn(
            viewModelScope + Dispatchers.IO,
            SharingStarted.Eagerly,
            emptyList()
        )

    //TODO wech
    val packageBackupsUpdated = MutableSharedFlow<Pair<String, List<Backup>>?>()
    val packageBackupsUpdate =
        if (pref_skipBackupsDatabase.value)
            MutableSharedFlow()
        else {
            packageBackupsUpdated
                // don't skip anything here (no conflate or map Latest etc.)
                // we need to process each update as it's the update for a single package
                .filterNotNull()
                //.buffer(UNLIMITED)   // use in case the flow isn't collected, yet, e.g. if using Lazily
                .trace { "*** packageBackupsUpdate <<- ${it.first} ${formatSortedBackups(it.second)}" }
                .onEach {
                    viewModelScope.launch(Dispatchers.IO) {
                        traceBackups {
                            "*** updating database ---------------------------> ${it.first} ${
                                formatSortedBackups(
                                    it.second
                                )
                            }"
                        }
                        db.getBackupDao().updateList(
                            it.first,
                            it.second.sortedByDescending { it.backupDate },
                        )
                    }
                }
                .stateIn(
                    viewModelScope + Dispatchers.IO,
                    SharingStarted.Eagerly,
                    null
                )
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val appExtrasDb =
        //------------------------------------------------------------------------------------------ appExtrasMap
        db.getAppExtrasDao().getAllFlow()
            .mapLatest { it.associateBy(AppExtras::packageName) }
            .trace { "*** appExtrasDb <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyMap()
            )

    //val appinfoList = MutableSharedFlow<List<AppInfo>>()
    val backupsUpdated = MutableSharedFlow<Map<String, List<Backup>>>()
    @OptIn(ExperimentalCoroutinesApi::class)
    val backupsUpdate = backupsUpdated
        .mapLatest {
            delay(100)
            it
        }
        .trace {
            "*** backupsUpdate: packages: ${it.keys.size} backups: ${
                it.values.map { it.size }.sum()
            }"
        }
        .stateIn(
            viewModelScope + Dispatchers.IO,
            SharingStarted.Eagerly,
            emptyMap()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allPackages =
        //========================================================================================== packageList
        combine(
            appInfosDb,
            if (pref_skipBackupsDatabase.value)
                backupsUpdate
            else
                backupsDb
        ) { appinfos, backups ->

            traceFlows {
                "******************** allPackages: appinfos: ${appinfos.size} backups: ${
                    backups.values.map { it.size }.sum()
                }"
            }

            val pkgs = appinfos.toPackageList(appContext, emptyList(), backups)

            IconCache.dropAllButUsed(pkgs.drop(0))

            traceFlows { "***** allPackages ->> ${pkgs.size}" }
            pkgs
        }
            .mapLatest { it }
            .trace { "*** allPackages <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allPackagesByNames =
        //------------------------------------------------------------------------------------------ packageMap
        allPackages
            .mapLatest { it.associateBy(Package::packageName) }
            .trace { "*** allPackagesByNames <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyMap()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val packages =
        //========================================================================================== notBlockedList
        combine(allPackages, blocklistDb) { pkgs, blocked ->

            traceFlows {
                "******************** blocking - list: ${pkgs.size} block: ${
                    blocked.joinToString(",")
                }"
            }

            val block = blocked.map { it.packageName }
            val list = pkgs.filterNot { block.contains(it.packageName) }

            traceFlows { "***** packages ->> ${list.size}" }
            list
        }
            .mapLatest { it }
            .trace { "*** packages <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    val searchQuery =
        //------------------------------------------------------------------------------------------ searchQuery
        MutableComposableFlow(
            "",
            viewModelScope + Dispatchers.IO,
            "searchQuery"
        )

    val modelSortFilter =
        //------------------------------------------------------------------------------------------ modelSortFilter
        MutableComposableFlow(
            sortFilterModel,
            viewModelScope + Dispatchers.IO,
            "modelSortFilter"
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPackages =
        //========================================================================================== filteredList
        combine(
            packages,
            modelSortFilter.flow,
            searchQuery.flow,
            appExtrasDb
        ) { pkgs, filter, search, extras ->

            var filtered = emptyList<Package>()

            traceFlows { "******************** filtering - packages: ${pkgs.size} filter: $filter" }

            filtered = pkgs
                .filter { item: Package ->
                    search.isEmpty() || (
                            (extras[item.packageName]?.customTags ?: emptySet()).plus(
                                listOfNotNull(
                                    item.packageName,
                                    item.packageLabel,
                                    extras[item.packageName]?.note
                                )
                            )
                                .any { it.contains(search, ignoreCase = true) }
                            )
                }
                .applyFilter(filter, OABX.context)

            traceFlows { "***** filteredPackages ->> ${filtered.size}" }

            filtered
        }
            // if the filter changes we can drop the older filters
            .mapLatest { it }
            .trace { "*** filteredPackages <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val updatedPackages =
        //------------------------------------------------------------------------------------------ updatedPackages
        packages
            .mapLatest {
                it.filter { it.isUpdated || (pref_newAndUpdatedNotification.value && it.isNew) }
                    .toMutableList()
            }
            .trace {
                "*** updatedPackages <<- updated: (${it.size})${
                    it.map {
                        "${it.packageName}(${it.versionCode}!=${it.latestBackup?.versionCode ?: ""})"
                    }
                }"
            }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    //---------------------------------------------------------------------------------------------- retriggerFlowsForUI

    fun retriggerFlowsForUI() {
        traceFlows { "******************** retriggerFlowsForUI" }
        runBlocking {
            val saved = searchQuery.value
            // in case same value isn't triggering
            val retrigger = saved + "<RETRIGGERING>"
            searchQuery.value = retrigger
            // wait until we really get that value
            while (searchQuery.value != retrigger)
                yield()
            // now switch back
            searchQuery.value = saved
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - FLOWS end

    val selection = mutableStateMapOf<String, Boolean>()
    val menuExpanded = mutableStateOf(false)

    fun updatePackage(packageName: String) {
        viewModelScope.launch {
            allPackagesByNames.value[packageName]?.let {
                updateDataOf(packageName)
            }
        }
    }

    private suspend fun updateDataOf(packageName: String) =
        withContext(Dispatchers.IO) {
            try {
                invalidateCacheForPackage(packageName)
                val appPackage = allPackagesByNames.value[packageName]
                appPackage?.apply {
                    val new = Package(appContext, packageName)
                    if (!isSpecial) {
                        new.refreshFromPackageManager(OABX.context)
                        db.getAppInfoDao().update(new.packageInfo as AppInfo)
                    }
                    //new.refreshBackupList()     //TODO hg42 ??? who calls this? take it from backupsMap?
                }
            } catch (e: AssertionError) {
                Timber.w(e.message ?: "")
                null
            }
        }

    fun updateExtras(appExtras: AppExtras) {
        viewModelScope.launch {
            updateExtrasWith(appExtras)
        }
    }

    private suspend fun updateExtrasWith(appExtras: AppExtras) {
        withContext(Dispatchers.IO) {
            db.getAppExtrasDao().replaceInsert(appExtras)
            true
        }
    }

    fun setExtras(appExtras: Map<String, AppExtras>) {
        viewModelScope.launch { replaceExtras(appExtras.values) }
    }

    private suspend fun replaceExtras(appExtras: Collection<AppExtras>) {
        withContext(Dispatchers.IO) {
            db.getAppExtrasDao().deleteAll()
            db.getAppExtrasDao().insert(*appExtras.toTypedArray())
        }
    }

    fun addToBlocklist(packageName: String) {
        viewModelScope.launch {
            insertIntoBlocklistDB(packageName)
        }
    }

    //fun removeFromBlocklist(packageName: String) {
    //    viewModelScope.launch {
    //        removeFromBlocklistDB(packageName)
    //    }
    //}

    private suspend fun insertIntoBlocklistDB(packageName: String) {
        withContext(Dispatchers.IO) {
            db.getBlocklistDao().insert(
                Blocklist.Builder()
                    .withId(0)
                    .withBlocklistId(PACKAGES_LIST_GLOBAL_ID)
                    .withPackageName(packageName)
                    .build()
            )
        }
    }

    //private suspend fun removeFromBlocklistDB(packageName: String) {
    //    updateBlocklist(
    //        (blocklist.value
    //            ?.map { it.packageName }
    //            ?.filterNotNull()
    //            ?.filterNot { it == packageName }
    //            ?: listOf()
    //        ).toSet()
    //    )
    //}

    fun setBlocklist(newList: Set<String>) {
        viewModelScope.launch {
            insertIntoBlocklistDB(newList)
        }
    }

    fun getBlocklist() = blocklistDb.value.mapNotNull { it.packageName }

    private suspend fun insertIntoBlocklistDB(newList: Set<String>) =
        withContext(Dispatchers.IO) {
            db.getBlocklistDao().updateList(PACKAGES_LIST_GLOBAL_ID, newList)
        }

    class Factory(
        private val database: ODatabase,
        private val application: Application,
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(database, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

