package com.machiav3lli.backup.dbs.repository

import com.machiav3lli.backup.dbs.ODatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class AppExtrasRepository(
    private val db: ODatabase
) {
    fun getAllFlow() = db.getAppExtrasDao().getAllFlow()
        .flowOn(Dispatchers.IO)

    fun getAll() = db.getAppExtrasDao().getAll()
}