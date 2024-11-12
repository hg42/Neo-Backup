package com.machiav3lli.backup.ui.compose.item

import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.PackageInfo
import com.machiav3lli.backup.handler.ShellCommands.Companion.currentProfile
import com.machiav3lli.backup.preferences.pref_altBackupDate
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.Lock
import com.machiav3lli.backup.ui.compose.icons.phosphor.LockOpen
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.utils.BACKUP_DATE_TIME_SHOW_FORMATTER
import com.machiav3lli.backup.utils.getFormattedDate
import java.time.LocalDateTime

@Composable
fun BackupItem_headlineContent(
    item: Backup,
) {
    Row {
        Text(
            text = "${item.versionName ?: ""} (${item.versionCode})",
            overflow = TextOverflow.Ellipsis,
            maxLines = 5,
        )
        Spacer(modifier = Modifier.weight(1f))
        AnimatedVisibility(visible = (item.cpuArch != android.os.Build.SUPPORTED_ABIS[0])) {
            Text(
                text = " ${item.cpuArch} ",
                color = Color.Red,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        BackupLabels(item = item)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun BackupItem_supportingContent(item: Backup) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (pref_altBackupDate.value)
                item.backupDate.format(BACKUP_DATE_TIME_SHOW_FORMATTER)
            else item.backupDate.getFormattedDate(true),
            modifier = Modifier.align(Alignment.Top),
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )
        FlowRow {
            Text(
                text = if (item.backupVersionCode == 0)
                    "old"
                else
                    "${item.backupVersionCode / 1000}.${item.backupVersionCode % 1000}",
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            if (item.isEncrypted) {
                val description = "${item.cipherType}"
                val showTooltip = remember { mutableStateOf(false) }
                if (showTooltip.value) {
                    Tooltip(description, showTooltip)
                }
                Text(
                    text = " enc",
                    color = Color.Red,
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showTooltip.value = true }
                        ),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
            val compressionText = if (item.isCompressed) {
                if (item.compressionType.isNullOrEmpty())
                    " gz"
                else
                    " ${item.compressionType}"
            } else ""
            if (compressionText.isNotEmpty()) Text(
                text = compressionText,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            val fileSizeText = if (item.backupVersionCode != 0)
                Formatter.formatFileSize(LocalContext.current, item.size)
            else ""
            Text(
                text = " - ${item.directoryTag}$fileSizeText",
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            AnimatedVisibility(visible = (item.profileId != currentProfile)) {
                Row {
                    Text(
                        text = " 👤",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = "${item.profileId}",
                        color = Color.Red,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun BackupItem(
    item: Backup,
    onRestore: (Backup) -> Unit = { },
    onDelete: (Backup) -> Unit = { },
    onNote: (Backup) -> Unit = { },
    rewriteBackup: (Backup, Backup) -> Unit = { _, _ -> },
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        headlineContent = { BackupItem_headlineContent(item) },
        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BackupItem_supportingContent(item)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    var persistent by remember(item.persistent) {
                        mutableStateOf(item.persistent)
                    }
                    val togglePersistent = {
                        persistent = !persistent
                        rewriteBackup(item, item.copy(persistent = persistent))
                    }

                    NoteTagItem(
                        item = item,
                        modifier = Modifier.weight(1f, false),
                        maxLines = 3,
                        onNote = onNote,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (persistent)
                            RoundButton(
                                icon = Phosphor.Lock,
                                tint = Color.Red,
                                onClick = togglePersistent
                            )
                        else
                            RoundButton(
                                icon = Phosphor.LockOpen,
                                onClick = togglePersistent
                            )
                        RoundButton(
                            icon = Phosphor.TrashSimple,
                            description = stringResource(id = R.string.deleteBackup),
                            tint = MaterialTheme.colorScheme.tertiary,
                            onClick = { onDelete(item) }
                        )
                        ElevatedActionButton(
                            icon = Phosphor.ClockCounterClockwise,
                            text = stringResource(id = R.string.restore),
                            positive = true,
                            onClick = { onRestore(item) },
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun RestoreBackupItem(
    item: Backup,
    index: Int,
    isApkChecked: Boolean = false,
    isDataChecked: Boolean = false,
    onApkClick: (String, Boolean, Int) -> Unit = { _: String, _: Boolean, _: Int -> },
    onDataClick: (String, Boolean, Int) -> Unit = { _: String, _: Boolean, _: Int -> },
) {
    var apkChecked by remember(isApkChecked) { mutableStateOf(isApkChecked) }
    var dataChecked by remember(isDataChecked) { mutableStateOf(isDataChecked) }
    val showApk by remember(item) { mutableStateOf(item.hasApk) }
    val showData by remember(item) { mutableStateOf(item.hasData) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = apkChecked,
                enabled = showApk,
                onCheckedChange = {
                    apkChecked = it
                    onApkClick(item.packageName, it, index)
                }
            )
            Checkbox(checked = dataChecked,
                enabled = showData,
                onCheckedChange = {
                    dataChecked = it
                    onDataClick(item.packageName, it, index)
                }
            )
            ListItem(
                modifier = Modifier.fillMaxWidth(),
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent,
                ),
                headlineContent = { BackupItem_headlineContent(item) },
                supportingContent = { BackupItem_supportingContent(item) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun BackupRestorePreview() {

    OABX.fakeContext = LocalContext.current.applicationContext

    var note by remember { mutableStateOf("a very very very long note text") }

    val backup = Backup(
        base = PackageInfo(
            packageName = "some.package.name",
            versionName = "1.2.3.4-some-version",
            versionCode = 1234,
        ),
        backupDate = LocalDateTime.now(),
        hasApk = true,
        hasAppData = true,
        hasDevicesProtectedData = true,
        hasExternalData = false,
        hasObbData = false,
        hasMediaData = false,
        compressionType = "zst",
        cipherType = "aes-256-cbc-etc",
        iv = null,
        cpuArch = "arm64",
        permissions = emptyList(),
        persistent = false,
        note = note,
        size = 123456789,
    )

    val backup_without_note = backup.copy(note = "")

    Column(modifier = Modifier.width(500.dp)) {
        FlowRow {
            ActionButton("none") {
                note = ""
            }
            ActionButton("short") {
                note = "note text"
            }
            ActionButton("middle") {
                note = "a longer note text"
            }
            ActionButton("long") {
                note = "a very very very long note text"
            }
            ActionButton("extreme") {
                note = "a very very very very very very very very long note text and even longer"
            }
            ActionButton("multiline") {
                note = "a very very very long note text\nmultiple\nlines"
            }
        }

        Text("BackupItem:")
        Spacer(modifier = Modifier.height(8.dp))
        BackupItem(item = backup)
        Spacer(modifier = Modifier.height(8.dp))
        BackupItem(item = backup_without_note)

        Spacer(modifier = Modifier.height(32.dp))

        Text("RestoreItem:")
        Spacer(modifier = Modifier.height(8.dp))
        RestoreBackupItem(item = backup, index = 3)
        Spacer(modifier = Modifier.height(8.dp))
        RestoreBackupItem(item = backup_without_note, index = 4)
    }
}
