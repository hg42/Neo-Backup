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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.repository.BlocklistRepository
import com.machiav3lli.backup.dbs.repository.PackageRepository
import com.machiav3lli.backup.entity.MainState
import com.machiav3lli.backup.entity.Package
import com.machiav3lli.backup.entity.SortFilterModel
import com.machiav3lli.backup.preferences.NeoPrefs
import com.machiav3lli.backup.preferences.pref_newAndUpdatedNotification
import com.machiav3lli.backup.preferences.traceBackups
import com.machiav3lli.backup.preferences.traceFlows
import com.machiav3lli.backup.ui.compose.item.IconCache
import com.machiav3lli.backup.ui.navigation.NavItem
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class MainVM(
    private val packageRepository: PackageRepository,
    private val blocklistRepository: BlocklistRepository,
    private val prefs: NeoPrefs,
    private val appContext: Application,
) : ViewModel() {
    init {
        Timber.w("==================== ${classAndId(this)}")
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - FLOWS

    private val _homeState = MutableStateFlow(MainState())
    val homeState: StateFlow<MainState> = _homeState.asStateFlow()
    private val _backupState = MutableStateFlow(MainState())
    val backupState: StateFlow<MainState> = _backupState.asStateFlow()
    private val _restoreState = MutableStateFlow(MainState())
    val restoreState: StateFlow<MainState> = _restoreState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    private val selection = MutableStateFlow(emptySet<String>())
    private val homeSortFilterModelFlow = prefs.homeSortFilterFlow()
    private val backupSortFilterModelFlow = prefs.backupSortFilterFlow()
    private val restoreSortFilterModelFlow = prefs.restoreSortFilterFlow()

    val backupsUpdateFlow = MutableSharedFlow<Pair<String, List<Backup>>?>()

    // Used as channel to update the database
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
                packageRepository.updateBackups(
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

    val packageMap =
        //========================================================================================== packageList
        combine(
            packageRepository.getPackagesFlow(),
            packageRepository.getBackupsFlow()
        ) { pkgs, backups ->

            traceFlows {
                "******************** packages-db: ${pkgs.size} backups-db: ${
                    backups.map { it.value.size }.sum()
                }"
            }

            IconCache.dropAllButUsed(pkgs.drop(0))

            traceFlows { "***** packages ->> ${pkgs.size}" }
            pkgs
        }
            .mapLatest { it.associateBy(Package::packageName) }
            .trace { "*** packageMap <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyMap()
            )

    val notBlockedList =
        //========================================================================================== notBlockedList
        combine(
            packageRepository.getPackagesFlow(),
            blocklistRepository.getBlocklistFlow()
        ) { pkgs, block ->

            traceFlows {
                "******************** blocking - list: ${pkgs.size} block: ${
                    block.joinToString(",")
                }"
            }

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

    val updatedPackages = // TODO move into state
        //------------------------------------------------------------------------------------------ updatedPackages
        notBlockedList
            .trace { "updatePackages? ..." }
            .mapLatest {
                it.filter { item ->
                    item.isUpdated || (pref_newAndUpdatedNotification.value && item.isNew)
                }
            }
            .trace {
                "*** updatedPackages <<- updated: (${it.size})${
                    it.map { item ->
                        "${item.packageName}(${item.versionCode}!=${item.latestBackup?.versionCode ?: ""})"
                    }
                }"
            }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - FLOWS end

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                packageRepository.getPackagesFlow(),
                blocklistRepository.getBlocklistFlow(),
                homeSortFilterModelFlow,
                searchQuery,
                selection
            ) { packages, blocklist, sortFilter, search, selection ->
                val filteredPackages = packages
                    .filterNot { blocklist.contains(it.packageName) }
                    .applySearchAndFilter(appContext, search, emptyMap(), sortFilter)

                MainState(
                    packages = packages,
                    filteredPackages = filteredPackages,
                    blocklist = blocklist,
                    schedules = emptyList(), // Add schedule repository if needed
                    searchQuery = search,
                    sortFilter = sortFilter,
                    selection = selection
                )
            }.collect { newState ->
                _homeState.update { newState }
            }
        }
        viewModelScope.launch {
            combine(
                packageRepository.getPackagesFlow(),
                blocklistRepository.getBlocklistFlow(),
                backupSortFilterModelFlow,
                searchQuery,
                selection
            ) { packages, blocklist, sortFilter, search, selection ->
                val filteredPackages = packages
                    .filterNot { blocklist.contains(it.packageName) }
                    .applySearchAndFilter(appContext, search, emptyMap(), sortFilter)

                MainState(
                    packages = packages,
                    filteredPackages = filteredPackages,
                    blocklist = blocklist,
                    schedules = emptyList(), // Add schedule repository if needed
                    searchQuery = search,
                    sortFilter = sortFilter,
                    selection = selection
                )
            }.collect { newState ->
                _backupState.update { newState }
            }
        }
        viewModelScope.launch {
            combine(
                packageRepository.getPackagesFlow(),
                blocklistRepository.getBlocklistFlow(),
                restoreSortFilterModelFlow,
                searchQuery,
                selection
            ) { packages, blocklist, sortFilter, search, selection ->
                val filteredPackages = packages
                    .filterNot { blocklist.contains(it.packageName) }
                    .applySearchAndFilter(appContext, search, emptyMap(), sortFilter)

                MainState(
                    packages = packages,
                    filteredPackages = filteredPackages,
                    blocklist = blocklist,
                    schedules = emptyList(), // Add schedule repository if needed
                    searchQuery = search,
                    sortFilter = sortFilter,
                    selection = selection
                )
            }.collect { newState ->
                _restoreState.update { newState }
            }
        }
    }

    fun setSearchQuery(value: String) {
        viewModelScope.launch { searchQuery.update { value } }
    }

    fun setSortFilter(value: SortFilterModel, sourcePage: NavItem) {
        viewModelScope.launch {
            when (sourcePage) {
                NavItem.Backup  -> {
                    prefs.sortBackup.value = value.sort
                    prefs.sortAscBackup.value = value.sortAsc
                    prefs.mainFilterBackup.value = value.mainFilter
                    prefs.backupFilterBackup.value = value.backupFilter
                    prefs.installedFilterBackup.value = value.installedFilter
                    prefs.launchableFilterBackup.value = value.launchableFilter
                    prefs.updatedFilterBackup.value = value.updatedFilter
                    prefs.latestFilterBackup.value = value.latestFilter
                    prefs.enabledFilterBackup.value = value.enabledFilter
                }

                NavItem.Restore -> {
                    prefs.sortRestore.value = value.sort
                    prefs.sortAscRestore.value = value.sortAsc
                    prefs.mainFilterRestore.value = value.mainFilter
                    prefs.backupFilterRestore.value = value.backupFilter
                    prefs.installedFilterRestore.value = value.installedFilter
                    prefs.launchableFilterRestore.value = value.launchableFilter
                    prefs.updatedFilterRestore.value = value.updatedFilter
                    prefs.latestFilterRestore.value = value.latestFilter
                    prefs.enabledFilterRestore.value = value.enabledFilter
                }

                else            -> {
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
            }
        }
    }

    fun toggleSelection(packageName: String) {
        viewModelScope.launch {
            selection.update { current ->
                if (current.contains(packageName)) {
                    current - packageName
                } else {
                    current + packageName
                }
            }
        }
    }

    fun updatePackage(packageName: String) {
        viewModelScope.launch {
            packageRepository.updatePackage(packageName)
        }
    }

    fun addToBlocklist(packageName: String) {
        viewModelScope.launch {
            blocklistRepository.addToBlocklist(packageName)
        }
    }

    fun updateBlocklist(packages: Set<String>) {
        viewModelScope.launch {
            blocklistRepository.updateBlocklist(packages)
        }
    }
}