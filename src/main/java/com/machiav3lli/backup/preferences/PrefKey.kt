package com.machiav3lli.backup.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PrefKey {
    // SortFilter.home
    val SORT_ASC_HOME = booleanPreferencesKey("sortAsc_home")
    val SORT_HOME = intPreferencesKey("sort_home")
    val MAIN_FILTER_HOME = intPreferencesKey("main_filter_home")
    val BACKUP_FILTER_HOME = intPreferencesKey("backup_filter_home")
    val INSTALLED_FILTER_HOME = intPreferencesKey("installed_filter_home")
    val LAUNCHABLE_FILTER_HOME = intPreferencesKey("launchable_filter_home")
    val UPDATED_FILTER_HOME = intPreferencesKey("updated_filter_home")
    val LATEST_FILTER_HOME = intPreferencesKey("latest_filter_home")
    val ENABLED_FILTER_HOME = intPreferencesKey("enabled_filter_home")
    // SortFilter.backup
    val SORT_ASC_BACKUP = booleanPreferencesKey("sortAsc_backup")
    val SORT_BACKUP = intPreferencesKey("sort_backup")
    val MAIN_FILTER_BACKUP = intPreferencesKey("main_filter_backup")
    val BACKUP_FILTER_BACKUP = intPreferencesKey("backup_filter_backup")
    val INSTALLED_FILTER_BACKUP = intPreferencesKey("installed_filter_backup")
    val LAUNCHABLE_FILTER_BACKUP = intPreferencesKey("launchable_filter_backup")
    val UPDATED_FILTER_BACKUP = intPreferencesKey("updated_filter_backup")
    val LATEST_FILTER_BACKUP = intPreferencesKey("latest_filter_backup")
    val ENABLED_FILTER_BACKUP = intPreferencesKey("enabled_filter_backup")
    // SortFilter.restore
    val SORT_ASC_RESTORE = booleanPreferencesKey("sortAsc_restore")
    val SORT_RESTORE = intPreferencesKey("sort_restore")
    val MAIN_FILTER_RESTORE = intPreferencesKey("main_filter_restore")
    val BACKUP_FILTER_RESTORE = intPreferencesKey("backup_filter_restore")
    val INSTALLED_FILTER_RESTORE = intPreferencesKey("installed_filter_restore")
    val LAUNCHABLE_FILTER_RESTORE = intPreferencesKey("launchable_filter_restore")
    val UPDATED_FILTER_RESTORE = intPreferencesKey("updated_filter_restore")
    val LATEST_FILTER_RESTORE = intPreferencesKey("latest_filter_restore")
    val ENABLED_FILTER_RESTORE = intPreferencesKey("enabled_filter_restore")
}