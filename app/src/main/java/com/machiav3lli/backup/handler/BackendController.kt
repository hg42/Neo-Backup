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
package com.machiav3lli.backup.handler

import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import com.machiav3lli.backup.BACKUP_INSTANCE_PROPERTIES_INDIR
import com.machiav3lli.backup.BACKUP_INSTANCE_REGEX_PATTERN
import com.machiav3lli.backup.BACKUP_PACKAGE_FOLDER_REGEX_PATTERN
import com.machiav3lli.backup.BACKUP_SPECIAL_FOLDER_REGEX_PATTERN
import com.machiav3lli.backup.IGNORED_PERMISSIONS
import com.machiav3lli.backup.MAIN_FILTER_SYSTEM
import com.machiav3lli.backup.MAIN_FILTER_USER
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PROP_NAME
import com.machiav3lli.backup.R
import com.machiav3lli.backup.actions.BaseAppAction.Companion.ignoredPackages
import com.machiav3lli.backup.dbs.dao.AppInfoDao
import com.machiav3lli.backup.dbs.dao.BackupDao
import com.machiav3lli.backup.dbs.entity.AppInfo
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.SpecialInfo
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.handler.ShellHandler.Companion.runAsRoot
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.items.StorageFile.Companion.invalidateCache
import com.machiav3lli.backup.preferences.pref_backupSuspendApps
import com.machiav3lli.backup.traceBackups
import com.machiav3lli.backup.traceBackupsScan
import com.machiav3lli.backup.utils.TraceUtils
import com.machiav3lli.backup.utils.getBackupDir
import com.machiav3lli.backup.utils.getInstalledPackageInfosWithPermissions
import com.machiav3lli.backup.utils.specialBackupsEnabled
import timber.log.Timber
import java.io.IOException
import java.util.*
import kotlin.system.measureTimeMillis

val regexBackupInstance = Regex(BACKUP_INSTANCE_REGEX_PATTERN)
val regexPackageFolder = Regex(BACKUP_PACKAGE_FOLDER_REGEX_PATTERN)
val regexSpecialFolder = Regex(BACKUP_SPECIAL_FOLDER_REGEX_PATTERN)

fun scanBackups(
    directory: StorageFile,
    packageName: String = "",
    rootDir: StorageFile = OABX.context.getBackupDir(),
    level: Int = 0,
    onPropsFile: (StorageFile) -> Unit,
) {

    val files = directory.listFiles()
    val names = files.map { it.name }

    fun formatBackupFile(file: StorageFile) = "${file.path?.replace(rootDir.path ?: "", "")}"

    fun traceBackupsScanPackage(todo: () -> String) {
        if (packageName.isNotEmpty())
            traceBackupsScan(todo)
    }

    files
        .forEach { file ->
            file.name?.let { name ->
                if (name.contains(regexPackageFolder) ||
                    name.contains(regexBackupInstance)
                ) {
                    if (name.contains(packageName)) {
                        if (name.contains(regexBackupInstance)) {
                            traceBackupsScanPackage {
                                ":::${"|:::".repeat(level)}b     ${
                                    formatBackupFile(file)
                                } backupInstance"
                            }
                            if (file.isPropertyFile) {
                                traceBackupsScanPackage {
                                    ":::${"|:::".repeat(level)}>     ${
                                        formatBackupFile(file)
                                    } ++++++++++++++++++++ props ok"
                                }
                                try {
                                    onPropsFile(file)
                                } catch (_: Throwable) {
                                    file.renameTo(".ERROR.${file.name}")
                                }
                            } else {
                                if (name.contains(regexPackageFolder) &&
                                    !name.contains(regexSpecialFolder) &&
                                    file.isDirectory
                                ) {
                                    if ("${file.name}.${PROP_NAME}" !in names)
                                        try {
                                            file.findFile(BACKUP_INSTANCE_PROPERTIES_INDIR)
                                                ?.let { onPropsFile(it) }
                                        } catch (_: Throwable) {
                                            file.renameTo(".ERROR.${file.name}")
                                        }
                                }
                            }
                        } else {
                            if (file.isPropertyFile) {
                                traceBackupsScanPackage {
                                    ":::${"|:::".repeat(level)}> ${
                                        formatBackupFile(file)
                                    } ++++++++++++++++++++ props ok"
                                }
                                onPropsFile(file)
                            } else {
                                if (file.isDirectory) {
                                    traceBackupsScanPackage {
                                        ":::${"|:::".repeat(level)}/     ${
                                            formatBackupFile(file)
                                        } //////////////////// dir ok"
                                    }
                                    scanBackups(
                                        file,
                                        packageName = packageName,
                                        rootDir = rootDir,
                                        level = level + 1,
                                        onPropsFile = onPropsFile
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (!name.contains(regexSpecialFolder) &&
                        file.isDirectory
                    ) {
                        traceBackupsScanPackage {
                            ":::${"|:::".repeat(level)}F     ${
                                formatBackupFile(file)
                            } %%%%%%%%%%%%%%%%%%%% folder ok"
                        }
                        scanBackups(
                            file,
                            packageName = packageName,
                            rootDir = rootDir,
                            level = level + 1,
                            onPropsFile = onPropsFile
                        )
                    }

                }
            }
        }
}

fun Context.getBackups(packageName: String = ""): Map<String, List<Backup>> {

    if (packageName.isEmpty())
        OABX.beginBusy("getBackups($packageName)")

    val backups = mutableListOf<Backup>()

    val backupDir = getBackupDir()

    if (packageName.isEmpty()) {
        invalidateCache {
            it.startsWith(backupDir.path ?: "")
        }
    } else {
        invalidateCache {
            it.startsWith(backupDir.path ?: "") &&
                    it.contains(packageName)
        }
    }

    scanBackups(backupDir, packageName) { propsFile ->
        Backup.createFrom(propsFile)
            ?.let(backups::add)
            ?: run {
                throw Exception("props file ${propsFile.path} not loaded")
            }
    }

    val backupsMap = backups.groupBy { it.packageName }

    if (packageName.isNotEmpty())
        traceBackups { "<$packageName> getBackups: ${TraceUtils.formatSortedBackups(backupsMap[packageName])}}" }

    if (packageName.isEmpty())
        OABX.endBusy("getBackups($packageName)")

    return backupsMap
}

// TODO respect special filter
fun Context.getPackageInfoList(filter: Int): List<PackageInfo> =
    packageManager.getInstalledPackageInfosWithPermissions()
        .filter { packageInfo: PackageInfo ->
            val isSystem =
                packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM
            val isIgnored = packageInfo.packageName.matches(ignoredPackages)
            if (isIgnored)
                Timber.i("ignored package: ${packageInfo.packageName}")
            (if (filter and MAIN_FILTER_SYSTEM == MAIN_FILTER_SYSTEM) isSystem && !isIgnored else false)
                    || (if (filter and MAIN_FILTER_USER == MAIN_FILTER_USER) !isSystem && !isIgnored else false)
        }
        .toList()

fun Context.getInstalledPackageList(): MutableList<Package> { // only used in ScheduledActionTask

    var packageList: MutableList<Package> = mutableListOf()

    try {
        OABX.beginBusy("getInstalledPackageList")

        val time = measureTimeMillis {

            val pm = packageManager
            val backupRoot = getBackupDir()
            val includeSpecial = specialBackupsEnabled
            val packageInfoList = pm.getInstalledPackageInfosWithPermissions()
            packageList = packageInfoList
                .filterNotNull()
                .filterNot { it.packageName.matches(ignoredPackages) }
                .mapNotNull {
                    try {
                        Package(this, it, backupRoot)
                    } catch (e: AssertionError) {
                        Timber.e("Could not create Package for ${it}: $e")
                        null
                    }
                }
                .toMutableList()

            if (!OABX.appsSuspendedChecked) {
                packageList.filter { appPackage ->
                    0 != (OABX.activity?.packageManager
                        ?.getPackageInfo(appPackage.packageName, 0)
                        ?.applicationInfo
                        ?.flags
                        ?: 0) and ApplicationInfo.FLAG_SUSPENDED
                }.apply {
                    OABX.main?.whileShowingSnackBar(getString(R.string.supended_apps_cleanup)) {
                        // cleanup suspended package if lock file found
                        this.forEach { appPackage ->
                            runAsRoot("pm unsuspend ${appPackage.packageName}")
                        }
                        OABX.appsSuspendedChecked = true
                    }
                }
            }

            // Special Backups must added before the uninstalled packages, because otherwise it would
            // discover the backup directory and run in a special case where no the directory is empty.
            // This would mean, that no package info is available – neither from backup.properties
            // nor from PackageManager.
            if (includeSpecial) {
                SpecialInfo.getSpecialPackages(this).forEach {
                    packageList.add(it)
                }
            }

            // don't get backups here, get them lazily if they are used,
            // e.g. to filter old backups
            //val backupsMap = getAllBackups()                              //TODO WECH
            //packageList = packageList.map {
            //    it.apply { updateBackupListAndDatabase(backupsMap[it.packageName].orEmpty()) }
            //}.toMutableList()
        }

        OABX.addInfoText(
            "getPackageList: ${(time / 1000 + 0.5).toInt()} sec"
        )
    } catch (e: Throwable) {
        logException(e)
    } finally {
        OABX.endBusy("getInstalledPackageList")
    }

    return packageList
}

fun List<Package>.toAppInfoList(): List<AppInfo> =
    filterNot { it.isSpecial }.map { it.packageInfo as AppInfo }

fun List<AppInfo>.toPackageList(
    context: Context,
    blockList: List<String> = listOf(),
    backupMap: Map<String, List<Backup>> = mapOf(),
): MutableList<Package> {

    var packageList: MutableList<Package> = mutableListOf()

    try {
        OABX.beginBusy("toPackageList")

        val includeSpecial = context.specialBackupsEnabled

        packageList =
            this.filterNot {
                it.packageName.matches(ignoredPackages)
            }
                .mapNotNull {
                    val pkg = try {
                        Package(context, it)
                    } catch (e: AssertionError) {
                        Timber.e("Could not create Package for ${it}: $e")
                        null
                    }
                    //pkg?.updateBackupList(backupMap[pkg.packageName].orEmpty())
                    pkg
                }
                .toMutableList()

        // Special Backups must added before the uninstalled packages, because otherwise it would
        // discover the backup directory and run in a special case where no the directory is empty.
        // This would mean, that no package info is available – neither from backup.properties
        // nor from PackageManager.
        // TODO show special packages directly wihtout restarting NB
        //val specialList = mutableListOf<String>()
        if (includeSpecial) {
            SpecialInfo.getSpecialPackages(context).forEach {
                if (!blockList.contains(it.packageName)) {
                    //it.updateBackupList(backupMap[it.packageName].orEmpty())
                    packageList.add(it)
                }
                //specialList.add(it.packageName)
            }
        }

    } catch (e: Throwable) {
        LogsHandler.unhandledException(e)
    } finally {
        OABX.endBusy("toPackageList")
    }

    return packageList
}

fun Context.updateAppTables(appInfoDao: AppInfoDao, backupDao: BackupDao) {

    OABX.beginBusy("updateAppTables")

    try {
        val installedPackageInfos = packageManager.getInstalledPackageInfosWithPermissions()
        val installedNames = installedPackageInfos.map { it.packageName }.toSet()

        try {
            OABX.beginBusy("unsuspend")

            if (!OABX.appsSuspendedChecked && pref_backupSuspendApps.value) {
                installedNames.filter { packageName ->
                    0 != (OABX.activity?.packageManager
                        ?.getPackageInfo(packageName, 0)
                        ?.applicationInfo
                        ?.flags
                        ?: 0) and ApplicationInfo.FLAG_SUSPENDED
                }.apply {
                    OABX.main?.whileShowingSnackBar(getString(R.string.supended_apps_cleanup)) {
                        // cleanup suspended package if lock file found
                        this.forEach { packageName ->
                            runAsRoot("pm unsuspend $packageName")
                        }
                        OABX.appsSuspendedChecked = true
                    }
                }
            }
        } catch (e: Throwable) {
            logException(e)
        } finally {
            OABX.endBusy("unsuspend")
        }

        val backups = mutableListOf<Backup>()

        val backupsMap = getBackups()
        OABX.main?.viewModel?.backupsMap?.clear()
        backupsMap.forEach {
            OABX.main?.viewModel?.backupsMap?.put(it.key, it.value)
            it.value.forEach {
                backups.add(it)
            }
        }

        val specialPackages = SpecialInfo.getSpecialPackages(this)
        val specialNames = specialPackages.map { it.packageName }.toSet()

        OABX.beginBusy("uninstalledPackagesWithBackup")

        val uninstalledPackagesWithBackup =
            (backupsMap.keys - installedNames - specialNames)
                .mapNotNull {
                    backupsMap[it]?.maxByOrNull { it.backupDate }?.toAppInfo()
                }

        OABX.endBusy("uninstalledPackagesWithBackup")

        OABX.beginBusy("appInfoList")

        val appInfoList =
            installedPackageInfos
                .map { AppInfo(this, it) }
                .union(uninstalledPackagesWithBackup)

        OABX.endBusy("appInfoList")

        OABX.beginBusy("dbUpdate")

        backupDao.updateList(*backups.toTypedArray())
        appInfoDao.updateList(*appInfoList.toTypedArray())

        OABX.endBusy("dbUpdate")

    } catch (e: Throwable) {
        logException(e)
    } finally {
        OABX.endBusy("updateAppTables")
    }
}

@Throws(PackageManager.NameNotFoundException::class)
fun Context.getPackageStorageStats(
    packageName: String,
    storageUuid: UUID = packageManager.getApplicationInfo(packageName, 0).storageUuid,
): StorageStats? {
    val storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
    return try {
        storageStatsManager.queryStatsForPackage(storageUuid, packageName, Process.myUserHandle())
    } catch (e: IOException) {
        Timber.e("Could not retrieve storage stats of $packageName: $e")
        null
    } catch (e: Throwable) {
        LogsHandler.unhandledException(e, packageName)
        null
    }
}

fun Context.getSpecial(packageName: String) = SpecialInfo.getSpecialPackages(this)
    .find { it.packageName == packageName }

val PackageInfo.grantedPermissions: List<String>
    get() = requestedPermissions?.filterIndexed { index, perm ->
        requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED == PackageInfo.REQUESTED_PERMISSION_GRANTED &&
                perm !in IGNORED_PERMISSIONS
    }.orEmpty()
