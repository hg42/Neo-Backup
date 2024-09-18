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
package com.machiav3lli.backup.dbs.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.machiav3lli.backup.BACKUP_INSTANCE_PROPERTIES_INDIR
import com.machiav3lli.backup.BACKUP_INSTANCE_REGEX_PATTERN
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PROP_NAME
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.handler.regexPackageFolder
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.utils.LocalDateTimeSerializer
import com.machiav3lli.backup.utils.getBackupRoot
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDateTime

@Entity(primaryKeys = ["packageName", "backupDate"])
@Serializable
data class Backup @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class) constructor(
    var backupVersionCode: Int = 0,
    var packageName: String,
    var packageLabel: String,
    @ColumnInfo(defaultValue = "-")
    var versionName: String? = "-",
    var versionCode: Int = 0,
    var profileId: Int = 0,
    var sourceDir: String? = null,
    var splitSourceDirs: Array<String> = arrayOf(),
    var isSystem: Boolean = false,
    @Serializable(with = LocalDateTimeSerializer::class)
    var backupDate: LocalDateTime,
    var hasApk: Boolean = false,
    var hasAppData: Boolean = false,
    var hasDevicesProtectedData: Boolean = false,
    var hasExternalData: Boolean = false,
    var hasObbData: Boolean = false,
    var hasMediaData: Boolean = false,
    var compressionType: String? = null,
    var cipherType: String? = null,
    var iv: ByteArray? = byteArrayOf(),
    var cpuArch: String?,
    var permissions: List<String> = listOf(),
    var size: Long = 0,
    var note: String = "",
    @ColumnInfo(defaultValue = "0")
    var persistent: Boolean = false,
) {
    constructor(
        base: PackageInfo,
        backupDate: LocalDateTime,
        hasApk: Boolean,
        hasAppData: Boolean,
        hasDevicesProtectedData: Boolean,
        hasExternalData: Boolean,
        hasObbData: Boolean,
        hasMediaData: Boolean,
        compressionType: String?,
        cipherType: String?,
        iv: ByteArray?,
        cpuArch: String?,
        permissions: List<String>,
        size: Long,
        persistent: Boolean = false,
        note: String = "",
    ) : this(
        backupVersionCode = com.machiav3lli.backup.BuildConfig.MAJOR * 1000 + com.machiav3lli.backup.BuildConfig.MINOR,
        packageName = base.packageName,
        packageLabel = base.packageLabel,
        versionName = base.versionName,
        versionCode = base.versionCode,
        profileId = base.profileId,
        sourceDir = base.sourceDir,
        splitSourceDirs = base.splitSourceDirs,
        isSystem = base.isSystem,
        backupDate = backupDate,
        hasApk = hasApk,
        hasAppData = hasAppData,
        hasDevicesProtectedData = hasDevicesProtectedData,
        hasExternalData = hasExternalData,
        hasObbData = hasObbData,
        hasMediaData = hasMediaData,
        compressionType = compressionType,
        cipherType = cipherType,
        iv = iv,
        cpuArch = cpuArch,
        permissions = permissions.sorted(),
        size = size,
        persistent = persistent,
        note = note,
    )

    val isCompressed: Boolean
        get() = compressionType != null && compressionType?.isNotEmpty() == true

    val isEncrypted: Boolean
        get() = cipherType != null && cipherType?.isNotEmpty() == true

    val hasData: Boolean
        get() = hasAppData || hasExternalData || hasDevicesProtectedData || hasMediaData || hasObbData

    fun toAppInfo() = AppInfo(
        packageName,
        packageLabel,
        versionName,
        versionCode,
        profileId,
        sourceDir,
        splitSourceDirs,
        isSystem,
        permissions
    )

    override fun toString(): String = "Backup{" +
            "packageName=" + packageName +
            ", backupDate=" + backupDate +
            ", hasApk=" + hasApk +
            ", hasAppData=" + hasAppData +
            ", hasDevicesProtectedData=" + hasDevicesProtectedData +
            ", hasExternalData=" + hasExternalData +
            ", hasObbData=" + hasObbData +
            ", hasMediaData=" + hasMediaData +
            ", persistent='" + persistent + '\'' +
            ", size=" + size +
            ", backupVersionCode='" + backupVersionCode + '\'' +
            ", cpuArch='" + cpuArch + '\'' +
            ", compressionType='" + compressionType + '\'' +
            ", cipherType='" + cipherType + '\'' +
            ", iv='" + iv + '\'' +
            ", permissions='" + permissions + '\'' +
            ", persistent=" + persistent +
            ", note='" + note + '\'' +
            '}'

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        javaClass != other?.javaClass
                || other !is Backup
                || packageName != other.packageName
                || backupDate != other.backupDate
                || backupVersionCode != other.backupVersionCode
                || packageLabel != other.packageLabel
                || versionName != other.versionName
                || versionCode != other.versionCode
                || profileId != other.profileId
                || sourceDir != other.sourceDir
                || !splitSourceDirs.contentEquals(other.splitSourceDirs)
                || isSystem != other.isSystem
                || hasApk != other.hasApk
                || hasAppData != other.hasAppData
                || hasDevicesProtectedData != other.hasDevicesProtectedData
                || hasExternalData != other.hasExternalData
                || hasObbData != other.hasObbData
                || hasMediaData != other.hasMediaData
                || compressionType != other.compressionType
                || cipherType != other.cipherType
                || iv != null && other.iv == null
                || iv != null && !iv.contentEquals(other.iv)
                || iv == null && other.iv != null
                || cpuArch != other.cpuArch
                || isEncrypted != other.isEncrypted
                || permissions != other.permissions
                || persistent != other.persistent
                || note != other.note
                || file?.path != other.file?.path
                || dir?.path != other.dir?.path
                       -> false

        else           -> true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + backupVersionCode
        result = 31 * result + (packageLabel?.hashCode() ?: 0)
        result = 31 * result + (versionName?.hashCode() ?: 0)
        result = 31 * result + versionCode
        result = 31 * result + profileId
        result = 31 * result + (sourceDir?.hashCode() ?: 0)
        result = 31 * result + splitSourceDirs.contentHashCode()
        result = 31 * result + isSystem.hashCode()
        result = 31 * result + backupDate.hashCode()
        result = 31 * result + hasApk.hashCode()
        result = 31 * result + hasAppData.hashCode()
        result = 31 * result + hasDevicesProtectedData.hashCode()
        result = 31 * result + hasExternalData.hashCode()
        result = 31 * result + hasObbData.hashCode()
        result = 31 * result + hasMediaData.hashCode()
        result = 31 * result + (compressionType?.hashCode() ?: 0)
        result = 31 * result + (cipherType?.hashCode() ?: 0)
        result = 31 * result + (iv?.contentHashCode() ?: 0)
        result = 31 * result + (cpuArch?.hashCode() ?: 0)
        result = 31 * result + isEncrypted.hashCode()
        result = 31 * result + permissions.hashCode()
        result = 31 * result + persistent.hashCode()
        result = 31 * result + note.hashCode()
        result = 31 * result + file?.path.hashCode()
        result = 31 * result + dir?.path.hashCode()
        return result
    }

    fun toSerialized() = OABX.toSerialized(OABX.propsSerializer, this)

    class BrokenBackupException @JvmOverloads internal constructor(
        message: String?,
        cause: Throwable? = null,
    ) : Exception(message, cause)

    @Ignore
    @Transient
    var file: StorageFile? = null

    @Ignore
    @Transient
    var dir: StorageFile? = null
        get() {
            if (field == null) {
                field = if (file?.name == BACKUP_INSTANCE_PROPERTIES_INDIR) {
                    file?.parent
                } else {
                    val baseName = file?.name?.removeSuffix(".$PROP_NAME")
                    baseName?.let { dirName ->
                        file?.parent?.findFile(dirName)
                    }
                }
            }
            return field
        }

    val tag: String
        get() {
            val pkg = "📦" // "📁"
            return (dir?.path
                ?.replace(OABX.context.getBackupRoot().path ?: "", "")
                ?.replace(packageName, pkg)
                ?.replace(Regex("""($pkg@)?$BACKUP_INSTANCE_REGEX_PATTERN"""), "")
                ?.replace(Regex("""[-:\s]+"""), "-")
                ?.replace(Regex("""/+"""), "/")
                ?.replace(Regex("""[-]+$"""), "-")
                ?.replace(Regex("""^[-/]+"""), "")
                ?: "") + if (file?.name == BACKUP_INSTANCE_PROPERTIES_INDIR) "🔹" else ""
        }

    companion object {

        fun fromSerialized(serialized: String) = OABX.fromSerialized<Backup>(serialized)

        fun createFrom(propertiesFile: StorageFile): Backup? {
            var serialized = ""
            try {

                serialized = propertiesFile.readText()

                val backup = fromSerialized(serialized)

                //TODO bug: list serialization (jsonPretty, yaml) adds a space in front of each value
                // found older multiline json and yaml without the bug, so it was introduced lately (by lib versions)
                backup.permissions = backup.permissions.map { it.trim() } //TODO workaround

                backup.file = propertiesFile

                return backup

            } catch (e: FileNotFoundException) {
                logException(e, "Cannot open ${propertiesFile.path}", backTrace = false)
                return null
            } catch (e: IOException) {
                logException(e, "Cannot read ${propertiesFile.path}", backTrace = false)
                return null
            } catch (e: Throwable) {
                logException(e, "file: ${propertiesFile.path} =\n$serialized", backTrace = false)
                return null
            }
        }

        fun createInvalidFrom(
            directory: StorageFile,
            propertiesFile: StorageFile? = null,
            packageName: String? = null,
            why: String? = null,
        ): Backup? {
            try {

                val packageNameFixed = packageName ?: run {
                    if (propertiesFile != null) {
                        if (propertiesFile.name == BACKUP_INSTANCE_PROPERTIES_INDIR) {
                            propertiesFile.parent?.name
                        } else {
                            val baseName = propertiesFile.name?.removeSuffix(".$PROP_NAME")
                            baseName?.let { dirName ->
                                propertiesFile.parent?.findFile(dirName)?.name
                            }
                        }
                    } else {
                        directory.name?.let { name ->
                            if (regexPackageFolder.matches(name)) {
                                name
                            } else {
                                regexPackageFolder.find(name)?.let { match ->
                                    match.groups[0]?.value
                                }
                            }
                        }
                    }
                } ?: ""

                val backup = Backup(
                    base = PackageInfo(
                        packageName = "...$packageNameFixed",
                        versionName = "INVALID" + if (why != null) ": $why" else "",
                        versionCode = 0,
                    ),
                    backupDate = LocalDateTime.parse("2000-01-01T00:00:00"),
                    hasApk = false,
                    hasAppData = false,
                    hasDevicesProtectedData = false,
                    hasExternalData = false,
                    hasObbData = false,
                    hasMediaData = false,
                    compressionType = null,
                    cipherType = null,
                    iv = null,
                    cpuArch = "",
                    permissions = emptyList(),
                    persistent = false,
                    note = "INVALID",
                    size = 0,
                )
                backup.apply {
                    file = propertiesFile
                    dir = directory
                    packageLabel = "? INVALID BACKUP"
                    backupVersionCode = -1
                    profileId = 0
                    isSystem = false
                }

                return backup

            } catch (e: Throwable) {
                logException(e,
                    "creating invalid backup item also failed for directory ${
                        directory.path
                    }${
                        if (propertiesFile != null)
                            " and file ${propertiesFile.path}"
                        else
                            ""
                    }",
                    backTrace = false
                )
                return null
            }
        }
    }
}