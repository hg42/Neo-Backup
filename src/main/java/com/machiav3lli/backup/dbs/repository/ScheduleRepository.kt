package com.machiav3lli.backup.dbs.repository

import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class ScheduleRepository(
    private val db: ODatabase
) {
    fun getAllFlow() = db.getScheduleDao().getAllFlow()
        .flowOn(Dispatchers.IO)

    fun getAll() = db.getScheduleDao().getAll()

    fun getSchedule(id: Long) = db.getScheduleDao().getSchedule(id)

    fun getSchedule(name: String) = db.getScheduleDao().getSchedule(name)

    fun update(value: Schedule) {
        db.getScheduleDao().update(value)
    }
}