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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.utils.TraceUtils.classAndId
import timber.log.Timber

class MainViewModel(
    private val db: ODatabase,
    private val appContext: Application,
) : AndroidViewModel(appContext) {

    init {
        Timber.w("==================== ${classAndId(this)}")
    }

    //TODO hg42 it seems these flows should belong to app, because the resources are global
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - FLOWS

    // most flows transport complete states, so skipping intermediate states is allowed
    // (via conflate, mapLatest)
    // it is noted explicitly, if each state must be processed (e.g. updates for single packages)
    //
    // conflate:
    //      takes the latest item and processes it completely, then takes the next (latest again)
    //      if input rate is f_in and processing can run at max rate f_proc,
    //      then with f_in > f_proc the results will only come out with about f_proc
    // mapLatest:
    //      use mapLatest { it } as an equivalent form similar to conflate()
    //      kills processing the item, when a new one comes in
    //      so, as long as items come in faster than processing time, there won't be results, in short:
    //      if f_in > f_proc, then there is no output at all
    //      this is much like processing on idle only
    // buffer(UNLIMITED)
    //      use in case the flow isn't collected, yet, e.g. if using Lazily

    /*
    fun scope() = viewModelScope + Dispatchers.IO

    data class FlowJob<TFlow, TJob>(
        val flow: TFlow,
        val job: TJob,
    )

    data class StateFlowJob<TState, TJob>(
        val state: TState,
        val job: TJob,
    )

    fun <T> Flow<T>.jobStateIn(
        scope: CoroutineScope,
        started: SharingStarted, // for compatibility
        initialValue: T,
    ): StateFlowJob<MutableStateFlow<T>, Job> {
        val state = MutableStateFlow(initialValue)
        val job = scope.launch {
            this@jobStateIn.collect { value ->
                state.value = value
            }
        }
        return StateFlowJob(state, job)
    }

    data class UpdateFlow<TUpdated, TUpdate, TJob>(
        val update: TUpdated,
        val state: TUpdate,
        val job: TJob,
    )

    fun <T> updateFlow(
        initialValue: T,
        how: (MutableSharedFlow<T>) -> Flow<T>,
    ): UpdateFlow<MutableSharedFlow<T>, MutableStateFlow<T>, Job> {
        val updated = MutableSharedFlow<T>(replay = 1)
        val (flow, job) =
            how(updated)
                .jobStateIn(
                    scope(),
                    SharingStarted.Eagerly,
                    initialValue,
                )
        return UpdateFlow(updated, flow, job)
    }

    val schedulesDb =
        //------------------------------------------------------------------------------------------
        db.getScheduleDao().getAllFlow()
            .trace { "*** schedulesDb ->> ${it.size}" }
            .stateIn(
                scope(),
                SharingStarted.Eagerly,
                emptyList()
            )

    val blocklistDb =
        //------------------------------------------------------------------------------------------
        db.getBlocklistDao().getAllFlow()
            .trace { "*** blocklistDb ->> ${it.size}" }
            .stateIn(
                scope(),
                SharingStarted.Eagerly,
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val appExtrasDb =
        //------------------------------------------------------------------------------------------
        db.getAppExtrasDao().getAllFlow()
            .mapLatest { it.associateBy(AppExtras::packageName) }
            .trace { "*** appExtrasDb ->> ${it.size}" }
            .stateIn(
                scope(),
                SharingStarted.Eagerly,
                emptyMap()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val appInfosChanged =
        //------------------------------------------------------------------------------------------
        updateFlow(emptyList<AppInfo>()) {
            it
                .trace { "??? appInfosChanged <-- ${it.size}" }
                .mapLatest {
                    delay(250)
                    it
                }
                .onEach {
                    traceFlows { "appInfosChanged: ***----------------- retriggerFlowsForUI" }
                    retriggerFlowsForUI()  //TODO hg42 workaround
                }
                .trace {
                    "*** appInfosUpdate ->> ${it.size}"
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val backupsChanged =
        //------------------------------------------------------------------------------------------
        updateFlow(emptyMap<String, List<Backup>>()) {
            it
                .trace { "??? backupsChanged <-- ${formatBackups(it)}" }
                .mapLatest {
                    delay(250)
                    it
                }
                .onEach {
                    traceFlows { "backupsChanged: ***----------------- retriggerFlowsForUI" }
                    retriggerFlowsForUI()  //TODO hg42 workaround
                }
                .trace { "*** backupsChanged ->> ${formatBackups(it)}" }
        }

    val allPackagesRetrigger =
        MutableComposableFlow(false, scope(), "allPackagesRetrigger")


    @OptIn(ExperimentalCoroutinesApi::class)
    val allPackages =
        //------------------------------------------------------------------------------------------
        combine(
            appInfosChanged.state,
            backupsChanged.state,
            allPackagesRetrigger.flow
        ) { appInfos, backups, retrigger ->

            traceFlows {
                "***< allPackages <-- appInfos: ${appInfos.size} ${formatBackups(backups)}"
            }

            val pkgs = runOrLog(emptyList()) {
                appInfos.toPackageList(appContext, emptyList(), backups)
            }

            traceFlows { "***<< allPackages <<- ${pkgs.size}" }
            pkgs
        }
            .mapLatest { pkgs ->
                var timeout = 30000L
                val timeStep = 250L
                while (
                    OABX.startup
                    || !OABX.validBackups
                    || pkgs.isEmpty()
                //|| pkgs.all { it.isSpecial }   // specials no more added to empty list
                ) {
                    trace {
                        "allPackages: waiting: startup=${
                            OABX.startup
                        } backups=${
                            OABX.validBackups
                        } pkgs=${
                            pkgs.size
                        }"
                    }

                    delay(timeStep)
                    timeout -= timeStep
                    if (!OABX.startup && timeout < 0)
                        break
                }
                delay(500)
                OABX.ready = true

                IconCache.dropAllButUsed(pkgs.drop(0))

                pkgs
            }
            .retry { cause ->
                logException(cause)
                true // restart flow
            }
            .trace { "****** allPackages ->> ${it.size}" }
            .stateIn(
                scope(),
                SharingStarted.Eagerly,
                emptyList(),
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allPackagesByNames =
        //------------------------------------------------------------------------------------------
        allPackages
            .mapLatest { it.associateBy(Package::packageName) }
            .trace { "********* allPackagesByNames ->> ${it.size}" }
            .stateIn(
                scope(),
                SharingStarted.Eagerly,
                emptyMap()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val packages =
        //==========================================================================================
        combine(allPackages, blocklistDb) { pkgs, blocked ->

            traceFlows {
                "******< packages <-- allPackages: ${pkgs.size} blocklistDb: ${
                    blocked.joinToString(",")
                }"
            }

            val block = blocked.map { it.packageName }
            val list = pkgs.filterNot { block.contains(it.packageName) }

            traceFlows { "******<< packages <<- ${list.size}" }
            list
        }
            .mapLatest { it }
            .trace { "********* packages ->> ${it.size}" }
            .stateIn(
                scope(),
                SharingStarted.Eagerly,
                emptyList()
            )

    val searchQuery =
        //------------------------------------------------------------------------------------------
        MutableComposableFlow(
            "",
            scope(),
            "searchQuery"
        )

    val modelSortFilter =
        //------------------------------------------------------------------------------------------
        MutableComposableFlow(
            sortFilterModel,
            scope(),
            "modelSortFilter"
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPackages =
        //==========================================================================================
        combine(
            packages,
            modelSortFilter.flow,
            searchQuery.flow,
            appExtrasDb
        ) { pkgs, filter, search, extras ->

            traceFlows { "*********< filteredPackages <-- ${pkgs.size} filter: $filter" }

            val filtered = pkgs
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

            traceFlows { "*********<< filteredPackages <<- ${filtered.size}" }

            filtered
        }
            // if the filter changes we can drop the older filters
            .mapLatest { it }
            .trace { "************ filteredPackages ->> ${it.size}" }
            .stateIn(
                scope(),
                SharingStarted.Eagerly,
                emptyList()
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val updatedPackages =
        //------------------------------------------------------------------------------------------
        packages
            .mapLatest {
                it.filter { it.isUpdated || (pref_newAndUpdatedNotification.value && it.isNew) }
                    .toMutableList()
            }
            .trace {
                "************ updatedPackages ->> (${it.size})${
                    it.map {
                        "${it.packageName}(${it.versionCode}!=${it.latestBackup?.versionCode ?: ""})"
                    }
                }"
            }
            .stateIn(
                scope(),
                SharingStarted.Eagerly,
                emptyList()
            )

    //----------------------------------------------------------------------------------------------
    fun retriggerFlowsForUI() {
        allPackagesRetrigger.value = !allPackagesRetrigger.value
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
                        //db.getAppInfoDao().update(new.packageInfo as AppInfo)
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

    */

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

