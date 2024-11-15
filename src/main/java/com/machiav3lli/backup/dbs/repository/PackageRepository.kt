package com.machiav3lli.backup.dbs.repository

import android.app.Application
import com.machiav3lli.backup.OABX.Companion.getBackups
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.entity.Package
import com.machiav3lli.backup.entity.Package.Companion.invalidateCacheForPackage
import com.machiav3lli.backup.handler.toPackageList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PackageRepository(
    private val db: ODatabase,
    private val appContext: Application
) {
    fun getPackagesFlow(): Flow<List<Package>> =
        db.getAppInfoDao().getAllFlow()
            .map { it.toPackageList(appContext, emptyList(), getBackups()) }
            .flowOn(Dispatchers.IO)

    fun getBackupsFlow(): Flow<Map<String, List<Backup>>> =
        db.getBackupDao().getAllFlow()
            .map { it.groupBy(Backup::packageName) }
            .flowOn(Dispatchers.IO)

    fun getBackups(packageName: String): List<Backup> = db.getBackupDao().get(packageName)

    suspend fun updatePackage(packageName: String) {
        withContext(Dispatchers.IO) {
            invalidateCacheForPackage(packageName)
            val new = Package(appContext, packageName)
            if (!new.isSpecial) {
                new.refreshFromPackageManager(appContext)
                db.getAppInfoDao().update(new.packageInfo as AppInfo)
            }
        }
    }

    fun upsertAppInfo(vararg appInfos: AppInfo) {
        db.getAppInfoDao().upsert(*appInfos)
    }

    fun replaceAppInfos(vararg appInfos: AppInfo) {
        db.getAppInfoDao().updateList(*appInfos)
    }

    fun updateBackups(packageName: String, backups: List<Backup>) {
        db.getBackupDao().updateList(packageName, backups)
    }

    fun replaceBackups(vararg backups: Backup) {
        db.getBackupDao().updateList(*backups)
    }

    fun deleteAppInfoOf(packageName: String) = db.getAppInfoDao().deleteAllOf(packageName)
}