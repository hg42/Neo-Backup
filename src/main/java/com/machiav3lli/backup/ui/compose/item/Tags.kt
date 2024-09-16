package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.dbs.entity.PackageInfo
import com.machiav3lli.backup.preferences.pref_useNoteIcon
import com.machiav3lli.backup.ui.compose.balancedWrap
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.NotePencil
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlusCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.compose.icons.phosphor.XCircle
import com.machiav3lli.backup.ui.compose.ifThen
import com.machiav3lli.backup.ui.compose.ifThenElse
import java.time.LocalDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagsBlock(
    modifier: Modifier = Modifier,
    tags: Set<String>?,
    onRemove: (String) -> Unit,
    onAdd: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags?.forEach { tag -> TagItem(tag = tag, onClick = onRemove) }
            TagItem(
                tag = stringResource(id = R.string.add_tag),
                icon = Phosphor.PlusCircle,
                action = true,
                onClick = { onAdd() },
            )
        }
    }
}

@Composable
fun TagItem(
    modifier: Modifier = Modifier,
    tag: String,
    icon: ImageVector = Phosphor.XCircle,
    action: Boolean = false,
    onClick: (String) -> Unit,
) {
    InputChip(
        modifier = modifier,
        selected = false,
        colors = InputChipDefaults.inputChipColors(
            containerColor = if (action) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
            labelColor = if (action) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            trailingIconColor = if (action) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.tertiary,
        ),
        shape = MaterialTheme.shapes.small,
        border = null,
        trailingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(id = R.string.dialogCancel),
            )
        },
        onClick = {
            onClick(tag)
        },
        label = {
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}

@Composable
fun NoteTagItem(
    item: Backup,
    modifier: Modifier = Modifier,
    useIcon: Boolean = pref_useNoteIcon.value,
    maxLines: Int = 1,
    onNote: ((Backup) -> Unit)? = null,
) {
    val note = item.note
    // with useNoteIcon enabled, show edit icon or chip with note
    // with useNoteIcon disabled, show filled chip "edit note" or chip with note
    // with onNote not set, disable editing (nobody reacts on the the change)
    val fillChip = if (useIcon) true else note.isEmpty() && (onNote != null)
    val showNode = note.isNotEmpty()
    val showIcon = useIcon && note.isEmpty() && (onNote != null)
    val showBadge = if (useIcon) note.isNotEmpty() else note.isNotEmpty() || (onNote != null)

    if (showIcon) {
        Icon(
            modifier = Modifier
                .size(24.dp)
                .clickable { onNote?.let { it(item) } },
            imageVector = Phosphor.NotePencil,
            contentDescription = stringResource(id = R.string.edit_note),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else if (showBadge) {
        Badge(
            modifier = modifier
                // a max width doesn't make sense, restriction depends on outside,
                // why shorten the note, if there is enough space? and 128.dp is a random value
                .ifThen(note.isNotEmpty()) { balancedWrap() }
                .clickable(onClick = { onNote?.let { it(item) } })
                .ifThenElse(fillChip,
                    { clip(MaterialTheme.shapes.medium) },
                    {
                        border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            MaterialTheme.shapes.medium
                        )
                    }
                ),
            containerColor = (
                    if (fillChip) MaterialTheme.colorScheme.primary
                    else Color.Transparent),
            contentColor = (
                    if (fillChip) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface),
        ) {
            Text(
                modifier = Modifier
                    .padding(2.dp)
                    .widthIn(min = 32.dp),
                text = note.ifEmpty { stringResource(id = R.string.edit_note) },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
            //TODO hg42 Tooltip(text = tag, openPopup = ???)
        }
    }
}

@Composable
fun AddTagView(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onAdd: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    SideEffect { textFieldFocusRequester.requestFocus() }

    var tagName by remember { mutableStateOf("") }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(tagName, TextRange(tagName.length)))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.large
            )
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.large
            ),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            onCancel()
        }) {
            Icon(
                imageVector = Phosphor.X,
                contentDescription = stringResource(id = R.string.dialogCancel)
            )
        }
        TextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                tagName = it.text
            },
            modifier = Modifier
                .weight(1f)
                .focusRequester(textFieldFocusRequester),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.add_tag)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        IconButton(onClick = {
            onAdd(tagName)
            textFieldValue = TextFieldValue("", TextRange(0))
            tagName = ""
        }) {
            Icon(
                imageVector = Phosphor.PlusCircle,
                contentDescription = stringResource(id = R.string.add_tag)
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun NoteTagItemPreview() {

    OABX.fakeContext = LocalContext.current.applicationContext

    var note by remember { mutableStateOf("note text") }
    var maxLines by remember { mutableStateOf(1) }
    var useIcon by remember { mutableStateOf(pref_useNoteIcon.value) }

    val packageInfo = PackageInfo(
        packageName = "com.machiav3lli.backup",
        versionName = "1.0",
        versionCode = 1,
    )
    val backup = Backup(
        base = packageInfo,
        backupDate = LocalDateTime.parse("2000-01-01T00:00:00"),
        hasApk = true,
        hasAppData = true,
        hasDevicesProtectedData = true,
        hasExternalData = true,
        hasObbData = true,
        hasMediaData = true,
        compressionType = "zst",
        cipherType = "aes-256-gcm",
        iv = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),
        cpuArch = "aarch64",
        permissions = emptyList(),
        size = 12345,
        persistent = false,
        note = "",
    )

    val backup_with_note = backup.copy(note = note)

    Column(modifier = Modifier.width(250.dp)) {
        FlowRow {
            ActionButton("short") {
                note = "note text"
                maxLines = 1
            }
            ActionButton("middle") {
                note = "a longer note text"
                maxLines = 1
            }
            ActionButton("long") {
                note = "a very very very long note text"
                maxLines = 1
            }
            ActionButton("multiline") {
                note = "a very very very long note text\nmultiple\nlines"
                maxLines = 2
            }
            ActionButton("icon") {
                useIcon = !useIcon
                pref_useNoteIcon.value = useIcon
            }
        }
        Text("\ntext:\n")
        Row(modifier = Modifier.padding(4.dp)) {
            Text("Backup: ")
            NoteTagItem(useIcon = false, item = backup_with_note, maxLines = maxLines, onNote = {})
        }
        Row(modifier = Modifier.padding(4.dp)) {
            Text("   empty: ")
            NoteTagItem(useIcon = false, item = backup, maxLines = maxLines, onNote = {})
        }
        Row(modifier = Modifier.padding(4.dp)) {
            Text("Restore: ")
            NoteTagItem(useIcon = false, item = backup_with_note, maxLines = maxLines)
        }
        Row(modifier = Modifier.padding(4.dp)) {
            Text("   empty: ")
            NoteTagItem(useIcon = false, item = backup, maxLines = maxLines)
        }
        Text("\nicon:\n")
        Row(modifier = Modifier.padding(4.dp)) {
            Text("Backup: ")
            NoteTagItem(useIcon = true, item = backup_with_note, maxLines = maxLines, onNote = {})
        }
        Row(modifier = Modifier.padding(4.dp)) {
            Text("   empty: ")
            NoteTagItem(useIcon = true, item = backup, maxLines = maxLines, onNote = {})
        }
        Row(modifier = Modifier.padding(4.dp)) {
            Text("Restore: ")
            NoteTagItem(useIcon = true, item = backup_with_note, maxLines = maxLines)
        }
        Row(modifier = Modifier.padding(4.dp)) {
            Text("   empty: ")
            NoteTagItem(useIcon = true, item = backup, maxLines = maxLines)
        }
    }
}

