package com.machiav3lli.backup.preferences

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

open class PrefBoolean(
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<Boolean>,
    defaultValue: Boolean = false,
    onChange: (Boolean) -> Unit = {}
) : PrefDelegate<Boolean>(titleId, summaryId, dataStore, key, defaultValue, onChange)

open class PrefInt(
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    dataStore: DataStore<Preferences>,
    key: Preferences.Key<Int>,
    val defaultValue: Int = 0,
    val entries: List<Int>,
    onChange: (Int) -> Unit = {}
) : PrefDelegate<Int>(titleId, summaryId, dataStore, key, defaultValue, onChange)

abstract class PrefDelegate<T : Any>(
    @StringRes var titleId: Int = -1,
    @StringRes var summaryId: Int = -1,
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    private val defaultValue: T,
    val onChange: (T) -> Unit
) {
    var value: T
        get() = runBlocking(Dispatchers.IO) {
            get().firstOrNull() ?: defaultValue
        }
        set(value) = runBlocking(Dispatchers.IO) {
            set(value)
        }

    val state: State<T>
        @Composable
        get() = get().collectAsState(initial = defaultValue)

    open fun get(): Flow<T> {
        return dataStore.data.map { prefs -> prefs[key] ?: defaultValue }
    }

    protected open suspend fun set(newValue: T) {
        if (value == newValue) return
        dataStore.edit { prefs -> prefs[key] = newValue }
        onChange(newValue)
    }
}