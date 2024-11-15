package com.machiav3lli.backup.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.machiav3lli.backup.BACKUP_FILTER_DEFAULT
import com.machiav3lli.backup.EnabledFilter
import com.machiav3lli.backup.InstalledFilter
import com.machiav3lli.backup.LatestFilter
import com.machiav3lli.backup.LaunchableFilter
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.Sort
import com.machiav3lli.backup.UpdatedFilter
import com.machiav3lli.backup.batchModesSequence
import com.machiav3lli.backup.entity.SortFilterModel
import com.machiav3lli.backup.possibleMainFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import org.koin.core.component.KoinComponent
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.getKoin

class NeoPrefs private constructor(val context: Context) : KoinComponent {
    private val dataStore: DataStore<Preferences> by getKoin().inject()

    val sortHome = PrefInt(
        dataStore = dataStore,
        key = PrefKey.SORT_HOME,
        defaultValue = Sort.LABEL.ordinal,
        entries = Sort.entries.map { it.ordinal },
    )

    val sortBackup = PrefInt(
        dataStore = dataStore,
        key = PrefKey.SORT_BACKUP,
        defaultValue = Sort.LABEL.ordinal,
        entries = Sort.entries.map { it.ordinal },
    )

    val sortRestore = PrefInt(
        dataStore = dataStore,
        key = PrefKey.SORT_RESTORE,
        defaultValue = Sort.LABEL.ordinal,
        entries = Sort.entries.map { it.ordinal },
    )

    val sortAscHome = PrefBoolean(
        dataStore = dataStore,
        key = PrefKey.SORT_ASC_HOME,
        defaultValue = true,
    )

    val sortAscBackup = PrefBoolean(
        dataStore = dataStore,
        key = PrefKey.SORT_ASC_BACKUP,
        defaultValue = true,
    )

    val sortAscRestore = PrefBoolean(
        dataStore = dataStore,
        key = PrefKey.SORT_ASC_RESTORE,
        defaultValue = true,
    )

    val mainFilterHome = PrefInt(
        dataStore = dataStore,
        key = PrefKey.MAIN_FILTER_HOME,
        defaultValue = MAIN_FILTER_DEFAULT,
        entries = possibleMainFilters, // not really, but shouldn't have an effect
    )

    val mainFilterBackup = PrefInt(
        dataStore = dataStore,
        key = PrefKey.MAIN_FILTER_BACKUP,
        defaultValue = MAIN_FILTER_DEFAULT,
        entries = possibleMainFilters, // not really, but shouldn't have an effect
    )

    val mainFilterRestore = PrefInt(
        dataStore = dataStore,
        key = PrefKey.MAIN_FILTER_RESTORE,
        defaultValue = MAIN_FILTER_DEFAULT,
        entries = possibleMainFilters, // not really, but shouldn't have an effect
    )

    val backupFilterHome = PrefInt(
        dataStore = dataStore,
        key = PrefKey.BACKUP_FILTER_HOME,
        defaultValue = BACKUP_FILTER_DEFAULT,
        entries = batchModesSequence, // not really, but shouldn't have an effect
    )

    val backupFilterBackup = PrefInt(
        dataStore = dataStore,
        key = PrefKey.BACKUP_FILTER_BACKUP,
        defaultValue = BACKUP_FILTER_DEFAULT,
        entries = batchModesSequence, // not really, but shouldn't have an effect
    )

    val backupFilterRestore = PrefInt(
        dataStore = dataStore,
        key = PrefKey.BACKUP_FILTER_RESTORE,
        defaultValue = BACKUP_FILTER_DEFAULT,
        entries = batchModesSequence, // not really, but shouldn't have an effect
    )

    val installedFilterHome = PrefInt(
        dataStore = dataStore,
        key = PrefKey.INSTALLED_FILTER_HOME,
        defaultValue = InstalledFilter.ALL.ordinal,
        entries = InstalledFilter.entries.map { it.ordinal },
    )

    val installedFilterBackup = PrefInt(
        dataStore = dataStore,
        key = PrefKey.INSTALLED_FILTER_BACKUP,
        defaultValue = InstalledFilter.ALL.ordinal,
        entries = InstalledFilter.entries.map { it.ordinal },
    )

    val installedFilterRestore = PrefInt(
        dataStore = dataStore,
        key = PrefKey.INSTALLED_FILTER_RESTORE,
        defaultValue = InstalledFilter.ALL.ordinal,
        entries = InstalledFilter.entries.map { it.ordinal },
    )

    val launchableFilterHome = PrefInt(
        dataStore = dataStore,
        key = PrefKey.LAUNCHABLE_FILTER_HOME,
        defaultValue = LaunchableFilter.ALL.ordinal,
        entries = LaunchableFilter.entries.map { it.ordinal },
    )

    val launchableFilterBackup = PrefInt(
        dataStore = dataStore,
        key = PrefKey.LAUNCHABLE_FILTER_BACKUP,
        defaultValue = LaunchableFilter.ALL.ordinal,
        entries = LaunchableFilter.entries.map { it.ordinal },
    )

    val launchableFilterRestore = PrefInt(
        dataStore = dataStore,
        key = PrefKey.LAUNCHABLE_FILTER_RESTORE,
        defaultValue = LaunchableFilter.ALL.ordinal,
        entries = LaunchableFilter.entries.map { it.ordinal },
    )

    val updatedFilterHome = PrefInt(
        dataStore = dataStore,
        key = PrefKey.UPDATED_FILTER_HOME,
        defaultValue = UpdatedFilter.ALL.ordinal,
        entries = UpdatedFilter.entries.map { it.ordinal },
    )

    val updatedFilterBackup = PrefInt(
        dataStore = dataStore,
        key = PrefKey.UPDATED_FILTER_BACKUP,
        defaultValue = UpdatedFilter.ALL.ordinal,
        entries = UpdatedFilter.entries.map { it.ordinal },
    )

    val updatedFilterRestore = PrefInt(
        dataStore = dataStore,
        key = PrefKey.UPDATED_FILTER_RESTORE,
        defaultValue = UpdatedFilter.ALL.ordinal,
        entries = UpdatedFilter.entries.map { it.ordinal },
    )

    val latestFilterHome = PrefInt(
        dataStore = dataStore,
        key = PrefKey.LATEST_FILTER_HOME,
        defaultValue = LatestFilter.ALL.ordinal,
        entries = LatestFilter.entries.map { it.ordinal },
    )

    val latestFilterBackup = PrefInt(
        dataStore = dataStore,
        key = PrefKey.LATEST_FILTER_BACKUP,
        defaultValue = LatestFilter.ALL.ordinal,
        entries = LatestFilter.entries.map { it.ordinal },
    )

    val latestFilterRestore = PrefInt(
        dataStore = dataStore,
        key = PrefKey.LATEST_FILTER_RESTORE,
        defaultValue = LatestFilter.ALL.ordinal,
        entries = LatestFilter.entries.map { it.ordinal },
    )

    val enabledFilterHome = PrefInt(
        dataStore = dataStore,
        key = PrefKey.ENABLED_FILTER_HOME,
        defaultValue = EnabledFilter.ALL.ordinal,
        entries = EnabledFilter.entries.map { it.ordinal },
    )

    val enabledFilterBackup = PrefInt(
        dataStore = dataStore,
        key = PrefKey.ENABLED_FILTER_BACKUP,
        defaultValue = EnabledFilter.ALL.ordinal,
        entries = EnabledFilter.entries.map { it.ordinal },
    )

    val enabledFilterRestore = PrefInt(
        dataStore = dataStore,
        key = PrefKey.ENABLED_FILTER_RESTORE,
        defaultValue = EnabledFilter.ALL.ordinal,
        entries = EnabledFilter.entries.map { it.ordinal },
    )

    fun homeSortFilterFlow(): Flow<SortFilterModel> = combine(
        sortHome.get(),
        sortAscHome.get(),
        mainFilterHome.get(),
        backupFilterHome.get(),
        installedFilterHome.get(),
        launchableFilterHome.get(),
        updatedFilterHome.get(),
        latestFilterHome.get(),
        enabledFilterHome.get(),
    ) { args ->
        SortFilterModel(
            sort = args[0] as Int,
            sortAsc = args[1] as Boolean,
            mainFilter = args[2] as Int,
            backupFilter = args[3] as Int,
            installedFilter = args[4] as Int,
            launchableFilter = args[5] as Int,
            updatedFilter = args[6] as Int,
            latestFilter = args[7] as Int,
            enabledFilter = args[8] as Int,
        )
    }.flowOn(Dispatchers.IO)

    fun backupSortFilterFlow(): Flow<SortFilterModel> = combine(
        sortBackup.get(),
        sortAscBackup.get(),
        mainFilterBackup.get(),
        backupFilterBackup.get(),
        installedFilterBackup.get(),
        launchableFilterBackup.get(),
        updatedFilterBackup.get(),
        latestFilterBackup.get(),
        enabledFilterBackup.get(),
    ) { args ->
        SortFilterModel(
            sort = args[0] as Int,
            sortAsc = args[1] as Boolean,
            mainFilter = args[2] as Int,
            backupFilter = args[3] as Int,
            installedFilter = args[4] as Int,
            launchableFilter = args[5] as Int,
            updatedFilter = args[6] as Int,
            latestFilter = args[7] as Int,
            enabledFilter = args[8] as Int,
        )
    }.flowOn(Dispatchers.IO)

    fun restoreSortFilterFlow(): Flow<SortFilterModel> = combine(
        sortRestore.get(),
        sortAscRestore.get(),
        mainFilterRestore.get(),
        backupFilterRestore.get(),
        installedFilterRestore.get(),
        launchableFilterRestore.get(),
        updatedFilterRestore.get(),
        latestFilterRestore.get(),
        enabledFilterRestore.get(),
    ) { args ->
        SortFilterModel(
            sort = args[0] as Int,
            sortAsc = args[1] as Boolean,
            mainFilter = args[2] as Int,
            backupFilter = args[3] as Int,
            installedFilter = args[4] as Int,
            launchableFilter = args[5] as Int,
            updatedFilter = args[6] as Int,
            latestFilter = args[7] as Int,
            enabledFilter = args[8] as Int,
        )
    }.flowOn(Dispatchers.IO)

    companion object {
        val prefsModule = module {
            single { NeoPrefs(get()) }
            single { provideDataStore(get()) }
        }

        private fun provideDataStore(context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                produceFile = {
                    context.preferencesDataStoreFile("neo_backup")
                },
            )
        }

        fun getInstance(): NeoPrefs = getKoin().get()
    }
}