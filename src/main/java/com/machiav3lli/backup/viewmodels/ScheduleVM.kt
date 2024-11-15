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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.dao.ScheduleDao
import com.machiav3lli.backup.dbs.entity.AppExtras
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.preferences.traceSchedule
import com.machiav3lli.backup.tasks.ScheduleWork
import com.machiav3lli.backup.utils.TraceUtils.trace
import com.machiav3lli.backup.utils.scheduleNext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class ScheduleVM(database: ODatabase) : ViewModel(), KoinComponent { // TODO add repos
    private val cc = Dispatchers.IO
    private val scheduleDB: ScheduleDao = database.getScheduleDao()

    private val _scheduleID = MutableStateFlow(-1L)

    val schedule: StateFlow<Schedule?> =
        combine(_scheduleID, scheduleDB.getScheduleFlow(_scheduleID.value)) { id, schedule ->
            withContext(cc) { scheduleDB.getSchedule(id) }
        }
            //TODO hg42 .trace { "*** schedule <<- ${it}" }     // what can here be null? (something is null that is not declared as nullable)
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                Schedule(0)
            )

    val customList =
        combine(_scheduleID, scheduleDB.getCustomListFlow(_scheduleID.value)) { id, bl ->
            withContext(cc) { scheduleDB.getCustomList(id) }
        }
    val blockList = combine(_scheduleID, scheduleDB.getBlockListFlow(_scheduleID.value)) { id, bl ->
        withContext(cc) { scheduleDB.getBlockList(id) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allTags =
        //------------------------------------------------------------------------------------------ allTags
        database.getAppExtrasDao().getAllFlow()
            .mapLatest { it.flatMap(AppExtras::customTags) }
            .trace { "*** tags <<- ${it.size}" }
            .stateIn(
                viewModelScope + Dispatchers.IO,
                SharingStarted.Eagerly,
                emptyList()
            )

    fun setSchedule(value: Long) {
        viewModelScope.launch { _scheduleID.update { value } }
    }

    fun updateSchedule(schedule: Schedule?, rescheduleBoolean: Boolean) {
        viewModelScope.launch {
            schedule?.let { updateS(it, rescheduleBoolean) }
        }
    }

    private suspend fun updateS(schedule: Schedule, rescheduleBoolean: Boolean) {
        withContext(Dispatchers.IO) {
            scheduleDB.update(schedule)
            if (schedule.enabled) {
                traceSchedule { "[$schedule.id] ScheduleViewModel.updateS -> ${if (rescheduleBoolean) "re-" else ""}schedule" }
                scheduleNext(
                    getKoin().get(),
                    schedule.id,
                    rescheduleBoolean
                )
            } else {
                traceSchedule { "[$schedule.id] ScheduleViewModel.updateS -> cancelAlarm" }
                ScheduleWork.cancel(getKoin().get(), schedule.id)
            }
        }
    }

    fun deleteSchedule() {
        viewModelScope.launch {
            deleteS()
        }
    }

    private suspend fun deleteS() {
        withContext(Dispatchers.IO) {
            scheduleDB.deleteById(_scheduleID.value)
        }
    }
}