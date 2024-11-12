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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.OABX.Companion.getBackups
import com.machiav3lli.backup.PACKAGES_LIST_GLOBAL_ID
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.Blocklist
import com.machiav3lli.backup.entity.Package
import com.machiav3lli.backup.entity.Package.Companion.invalidateCacheForPackage
import com.machiav3lli.backup.entity.SortFilterModel
import com.machiav3lli.backup.handler.toPackageList
import com.machiav3lli.backup.preferences.NeoPrefs
import com.machiav3lli.backup.preferences.pref_newAndUpdatedNotification
import com.machiav3lli.backup.preferences.traceBackups
import com.machiav3lli.backup.preferences.traceFlows
import com.machiav3lli.backup.ui.compose.item.IconCache
import com.machiav3lli.backup.utils.TraceUtils.classAndId
import com.machiav3lli.backup.utils.TraceUtils.formatSortedBackups
import com.machiav3lli.backup.utils.TraceUtils.trace
import com.machiav3lli.backup.utils.applySearchAndFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class MainVM(
    private val db: ODatabase,
    private val prefs: NeoPrefs,
    private val appContext: Application,
) : ViewModel() {
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

    val homeSortFilterModel: StateFlow<SortFilterModel> = combine(
        prefs.sortHome.get(),
        prefs.sortAscHome.get(),
        prefs.mainFilterHome.get(),
        prefs.backupFilterHome.get(),
        prefs.installedFilterHome.get(),
        prefs.launchableFilterHome.get(),
        prefs.updatedFilterHome.get(),
        prefs.latestFilterHome.get(),
        prefs.enabledFilterHome.get(),
    ) { args ->
        SortFilterModel(
            args[0] as Int,
            args[1] as Boolean,
            args[2] as Int,
            args[3] as Int,
            args[4] as Int,
            args[5] as Int,
            args[6] as Int,
            args[7] as Int,
            args[8] as Int,
        )
    }
        .stateIn(
            viewModelScope + Dispatchers.IO,
            SharingStarted.Lazily,
            SortFilterModel()
        )

    val backupSortFilterModel: StateFlow<SortFilterModel> = combine(
        prefs.sortBackup.get(),
        prefs.sortAscBackup.get(),
        prefs.mainFilterBackup.get(),
        prefs.backupFilterBackup.get(),
        prefs.installedFilterBackup.get(),
        prefs.launchableFilterBackup.get(),
        prefs.updatedFilterBackup.get(),
        prefs.latestFilterBackup.get(),
        prefs.enabledFilterBackup.get(),
    ) { args ->
        SortFilterModel(
            args[0] as Int,
            args[1] as Boolean,
            args[2] as Int,
            args[3] as Int,
            args[4] as Int,
            args[5] as Int,
            args[6] as Int,
            args[7] as Int,
            args[8] as Int,
        )
    }
        .stateIn(
            viewModelScope + Dispatchers.IO,
            SharingStarted.Lazily,
            SortFilterModel()
        )

    val restoreSortFilterModel: StateFlow<SortFilterModel> = combine(
        prefs.sortRestore.get(),
        prefs.sortAscRestore.get(),
        prefs.mainFilterRestore.get(),
        prefs.backupFilterRestore.get(),
        prefs.installedFilterRestore.get(),
        prefs.launchableFilterRestore.get(),
        prefs.updatedFilterRestore.get(),
        prefs.latestFilterRestore.get(),
        prefs.enabledFilterRestore.get(),
    ) { args ->
        SortFilterModel(
            args[0] as Int,
            args[1] as Boolean,
            args[2] as Int,
            args[3] as Int,
            args[4] as Int,
            args[5] as Int,
            args[6] as Int,
            args[7] as Int,
            args[8] as Int,
        )
    }
        .stateIn(
            viewModelScope + Dispatchers.IO,
            SharingStarted.Lazily,
            SortFilterModel()
        )

    val schedules =
        //------------------------------------------------------------------------------------------ blocklist
        db.getScheduleDao().getAllFlow()
            .trace { "*** schedules <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    val blocklist =
        //------------------------------------------------------------------------------------------ blocklist
        db.getBlocklistDao().getAllFlow()
            .trace { "*** blocklist <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    private val backupsMapDb =
        //------------------------------------------------------------------------------------------ backupsMap
        db.getBackupDao().getAllFlow()
            .mapLatest { it.groupBy(Backup::packageName) }
            .trace { "*** backupsMapDb <<- p=${it.size} b=${it.map { it.value.size }.sum()}" }
            //.trace { "*** backupsMap <<- p=${it.size} b=${it.map { it.value.size }.sum()} #################### egg ${showSortedBackups(it["com.android.egg"])}" }  // for testing use com.android.egg
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyMap()
            )

    val backupsUpdateFlow = MutableSharedFlow<Pair<String, List<Backup>>?>()
    val backupsUpdate = backupsUpdateFlow
        // don't skip anything here (no conflate or map Latest etc.)
        // we need to process each update as it's the update for a single package
        .filterNotNull()
        //.buffer(UNLIMITED)   // use in case the flow isn't collected, yet, e.g. if using Lazily
        .trace { "*** backupsUpdate <<- ${it.first} ${formatSortedBackups(it.second)}" }
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

    private val appExtrasMap =
        //------------------------------------------------------------------------------------------ appExtrasMap
        db.getAppExtrasDao().getAllFlow()
            .mapLatest { it.associateBy(AppExtras::packageName) }
            .trace { "*** appExtrasMap <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyMap()
            )

    val packageList =
        //========================================================================================== packageList
        combine(db.getAppInfoDao().getAllFlow(), backupsMapDb) { appinfos, backups ->

            traceFlows {
                "******************** packages-db: ${appinfos.size} backups-db: ${
                    backups.map { it.value.size }.sum()
                }"
            }

            // use the current backups instead of slow and async turn around from db
            // but keep backups in combine, because it signals changes of the backups
            //TODO hg42 might be done differently later
            //val appinfos = appinfos.toPackageList(appContext, emptyList(), backups)
            val pkgs = appinfos.toPackageList(appContext, emptyList(), getBackups())

            IconCache.dropAllButUsed(pkgs.drop(0))

            traceFlows { "***** packages ->> ${pkgs.size}" }
            pkgs
        }
            .mapLatest { it }
            .trace { "*** packageList <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    val packageMap =
        //------------------------------------------------------------------------------------------ packageMap
        packageList
            .mapLatest { it.associateBy(Package::packageName) }
            .trace { "*** packageMap <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyMap()
            )

    val notBlockedList =
        //========================================================================================== notBlockedList
        combine(packageList, blocklist) { pkgs, blocked ->

            traceFlows {
                "******************** blocking - list: ${pkgs.size} block: ${
                    blocked.joinToString(",")
                }"
            }

            val block = blocked.map { it.packageName }
            val list = pkgs.filterNot { block.contains(it.packageName) }

            traceFlows { "***** blocked ->> ${list.size}" }
            list
        }
            .mapLatest { it }
            .trace { "*** notBlockedList <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val homeFilteredList =
        //========================================================================================== filteredList
        combine(
            notBlockedList,
            homeSortFilterModel,
            searchQuery,
            appExtrasMap
        ) { pkgs, filter, search, extras ->
            traceFlows { "******************** filtering - list: ${pkgs.size} filter: $filter" }
            pkgs.applySearchAndFilter(appContext, search, extras, filter)
        }
            // if the filter changes we can drop the older filters
            .mapLatest { it }
            .trace { "*** filteredList <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    val backupFilteredList =
        //========================================================================================== filteredList
        combine(
            notBlockedList,
            backupSortFilterModel,
            searchQuery,
            appExtrasMap
        ) { pkgs, filter, search, extras ->
            traceFlows { "******************** filtering - list: ${pkgs.size} filter: $filter" }
            pkgs.applySearchAndFilter(appContext, search, extras, filter)
        }
            // if the filter changes we can drop the older filters
            .mapLatest { it }
            .trace { "*** backupFilteredList <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    val restoreFilteredList =
        //========================================================================================== filteredList
        combine(
            notBlockedList,
            restoreSortFilterModel,
            searchQuery,
            appExtrasMap
        ) { pkgs, filter, search, extras ->
            traceFlows { "******************** filtering - list: ${pkgs.size} filter: $filter" }
            pkgs.applySearchAndFilter(appContext, search, extras, filter)
        }
            // if the filter changes we can drop the older filters
            .mapLatest { it }
            .trace { "*** filteredList <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    val updatedPackages =
        //------------------------------------------------------------------------------------------ updatedPackages
        notBlockedList
            .trace { "updatePackages? ..." }
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

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - FLOWS end

    val selection = mutableStateMapOf<String, Boolean>()
    val menuExpanded = mutableStateOf(false)

    fun setSearchQuery(value: String) {
        viewModelScope.launch { _searchQuery.update { value } }
    }

    fun setSortFilter(value: SortFilterModel) = viewModelScope.launch {
        prefs.sortHome.value = value.sort
        prefs.sortAscHome.value = value.sortAsc
        prefs.mainFilterHome.value = value.mainFilter
        prefs.backupFilterHome.value = value.backupFilter
        prefs.installedFilterHome.value = value.installedFilter
        prefs.launchableFilterHome.value = value.launchableFilter
        prefs.updatedFilterHome.value = value.updatedFilter
        prefs.latestFilterHome.value = value.latestFilter
        prefs.enabledFilterHome.value = value.enabledFilter
    }

    fun updatePackage(packageName: String) {
        viewModelScope.launch {
            packageMap.value[packageName]?.let {
                updateDataOf(packageName)
            }
        }
    }

    private suspend fun updateDataOf(packageName: String) =
        withContext(Dispatchers.IO) {
            try {
                invalidateCacheForPackage(packageName)
                val appPackage = packageMap.value[packageName]
                appPackage?.apply {
                    val new = Package(appContext, packageName)
                    if (!isSpecial) {
                        new.refreshFromPackageManager(appContext)
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

    fun getBlocklist() = blocklist.value.mapNotNull { it.packageName }

    private suspend fun insertIntoBlocklistDB(newList: Set<String>) =
        withContext(Dispatchers.IO) {
            db.getBlocklistDao().updateList(PACKAGES_LIST_GLOBAL_ID, newList)
        }
}

