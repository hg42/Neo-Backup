package com.machiav3lli.backup.handler

import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import com.machiav3lli.backup.Constants
import com.machiav3lli.backup.Constants.classTag
import com.machiav3lli.backup.dbs.Schedule
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.items.SpecialAppMetaInfo.Companion.getSpecialPackages
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.StorageFile.Companion.invalidateCache
import com.machiav3lli.backup.utils.DocumentUtils.getBackupRoot
import com.machiav3lli.backup.utils.FileUtils.BackupLocationIsAccessibleException
import com.machiav3lli.backup.utils.LogUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.getDefaultSharedPreferences
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

object BackendController {
    private val TAG = classTag(".BackendController")

    /*
    List of packages ignored for any reason
     */
    private val ignoredPackages = listOf(
            "android"   // virtual package. Data directory is /data -> not a good idea to backup
    )

    fun getPackageInfoList(context: Context, mode: Schedule.Mode?): List<PackageInfo> {
        val pm = context.packageManager
        return pm.getInstalledPackages(0)
                .filter { packageInfo: PackageInfo ->
                    val isSystem = packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
                    val isNotIgnored = !ignoredPackages.contains(packageInfo.packageName)
                    when (mode) {
                        Schedule.Mode.USER -> return@filter !isSystem && isNotIgnored
                        Schedule.Mode.SYSTEM -> return@filter isSystem && isNotIgnored
                        else -> return@filter isNotIgnored
                    }
                }
                .toList()
    }

    @Throws(BackupLocationIsAccessibleException::class, StorageLocationNotConfiguredException::class)
    fun getApplicationList(context: Context): MutableList<AppInfo> = getApplicationList(context, true)

    @Throws(BackupLocationIsAccessibleException::class, StorageLocationNotConfiguredException::class)
    fun getApplicationList(context: Context, includeUninstalled: Boolean): MutableList<AppInfo> {
        invalidateCache()
        val includeSpecial = getDefaultSharedPreferences(context).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, false)
        val pm = context.packageManager
        val backupRoot = getBackupRoot(context)
        val packageInfoList = pm.getInstalledPackages(0)
        val packageList = packageInfoList
                .filterNotNull()
                .filter { !ignoredPackages.contains(it.packageName) }
                .map { AppInfo(context, it, backupRoot.uri) }
                .toMutableList()
        // Special Backups must added before the uninstalled packages, because otherwise it would
        // discover the backup directory and run in a special case where no the directory is empty.
        // This would mean, that no package info is available – neither from backup.properties
        // nor from PackageManager.
        if (includeSpecial) {
            packageList.addAll(getSpecialPackages(context))
        }
        if (includeUninstalled) {
            val installedPackageNames = packageList
                    .map(AppInfo::packageName)
                    .toList()
            val directoriesInBackupRoot = getDirectoriesInBackupRoot(context)
            val missingAppsWithBackup: List<AppInfo> =
            // Try to create AppInfoX objects
            // if it fails, null the object for filtering in the next step to avoid crashes
                    // filter out previously failed backups
                    directoriesInBackupRoot
                            .filter { !installedPackageNames.contains(it.name) }
                            .mapNotNull {
                                try {
                                    AppInfo(context, it.uri, it.name)
                                } catch (e: AssertionError) {
                                    Log.e(TAG, "Could not process backup folder for uninstalled application in " + it.name + ": " + e)
                                    null
                                }
                            }
                            .toList()
            packageList.addAll(missingAppsWithBackup)
        }
        return packageList
    }

    @Throws(BackupLocationIsAccessibleException::class, StorageLocationNotConfiguredException::class)
    fun getDirectoriesInBackupRoot(context: Context): List<StorageFile> {
        val backupRoot = getBackupRoot(context)
        try {
            return backupRoot.listFiles()
                    .filter(StorageFile::isDirectory)
                    .toList()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, e.javaClass.simpleName + ": " + e.message)
        } catch (e: Throwable) {
            LogUtils.unhandledException(e)
        }
        return arrayListOf()
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageStorageStats(context: Context, packageName: String): StorageStats? {
        val storageUuid = context.packageManager.getApplicationInfo(packageName, 0).storageUuid
        return getPackageStorageStats(context, packageName, storageUuid)
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageStorageStats(context: Context, packageName: String, storageUuid: UUID): StorageStats? {
        val storageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        return try {
            storageStatsManager.queryStatsForPackage(storageUuid, packageName, Process.myUserHandle())
        } catch (e: IOException) {
            Log.e(TAG, "Could not retrieve storage stats of $packageName: $e")
            null
        } catch (e: Throwable) {
            LogUtils.unhandledException(e, packageName)
            null
        }
    }
}