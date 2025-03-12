package com.machiav3lli.backup.ui.compose.item

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import coil.ImageLoader
import com.machiav3lli.backup.MODE_ALL
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.beginBusy
import com.machiav3lli.backup.OABX.Companion.endBusy
import com.machiav3lli.backup.OABX.Companion.isDebug
import com.machiav3lli.backup.SELECTIONS_FOLDER_NAME
import com.machiav3lli.backup.batchModes
import com.machiav3lli.backup.dbs.entity.SpecialInfo
import com.machiav3lli.backup.handler.BackupRestoreHelper
import com.machiav3lli.backup.handler.LogsHandler.Companion.unexpectedException
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellCommands.Companion.currentProfile
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.traceContextMenu
import com.machiav3lli.backup.traceTiming
import com.machiav3lli.backup.ui.compose.Confirmation
import com.machiav3lli.backup.ui.compose.ShowIf
import com.machiav3lli.backup.ui.compose.TextInputMenuItem
import com.machiav3lli.backup.ui.compose.closeSubMenu
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.Check
import com.machiav3lli.backup.ui.compose.icons.phosphor.Play
import com.machiav3lli.backup.ui.compose.icons.phosphor.X
import com.machiav3lli.backup.ui.compose.menuPool
import com.machiav3lli.backup.ui.compose.menuScope
import com.machiav3lli.backup.ui.compose.openSubMenu
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.utils.SystemUtils.runParallel
import com.machiav3lli.backup.utils.TraceUtils.beginNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.endNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.logNanoTiming
import com.machiav3lli.backup.utils.TraceUtils.nanoTiming
import com.machiav3lli.backup.utils.getFormattedDate
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.math.roundToInt

const val logEachN = 1000L

@Composable
fun DataPartsSelector(
    result: MutableIntState,
) {
    //TODO hg42 add sizes

    batchModes.forEach { (mode, name) ->
        DropdownMenuItem(
            leadingIcon = {
                if ((result.intValue and mode) != 0)
                    Icon(Phosphor.Check, null, tint = Color.Green)
                else
                    Icon(Phosphor.X, null, tint = Color.Red)
            },
            text = { Text(name) },
            onClick = {
                result.intValue = result.intValue xor mode
            }
        )
    }
}

val persist_batchMode = IntPref(
    key = "persist.batchMode",
    entries = (MODE_UNSET..MODE_ALL).toList(),
    defaultValue = MODE_ALL
)

@Composable
fun SelectDataParts(
    expanded: MutableState<Boolean>,
    onAction: (mode: Int) -> Unit = {},
) {
    val mode = remember { mutableIntStateOf(persist_batchMode.value) }

    DataPartsSelector(mode)

    HorizontalDivider() //--------------------------------------------------------------------------

    DropdownMenuItem(
        leadingIcon = { Icon(Phosphor.Play, null) },
        text = { Text("doit!") },
        onClick = {
            expanded.value = false
            persist_batchMode.value = mode.intValue
            onAction(mode.intValue)
        }
    )
}

@Composable
fun Selections(
    action: String,
    selection: List<String> = emptyList(),
    onAction: (List<String>) -> Unit = {},
) {
    val backupRoot = OABX.backupRoot
    val selectionsDir = backupRoot?.findFile(SELECTIONS_FOLDER_NAME)
        ?: backupRoot?.createDirectory(SELECTIONS_FOLDER_NAME)
    val files = selectionsDir?.listFiles() ?: emptyList()

    if (files.isEmpty())
        DropdownMenuItem(
            text = { Text("--- no saved selections ---") },
            onClick = {}
        )
    else {
        DropdownMenuItem(
            enabled = false, onClick = {},
            text = { Text("--- selections saved ---") }
        )
        files.forEach { file ->
            file.name?.let { name ->
                DropdownMenuItem(
                    text = { Text("  $name") },
                    onClick = {
                        when (action) {
                            "get" -> {
                                val newSelection = file.readText().lines()
                                onAction(newSelection)
                            }

                            "put" -> {
                                file.writeText(selection.joinToString("\n"))
                                onAction(selection)
                            }

                            "del" -> {
                                file.delete()
                                onAction(selection)
                            }
                        }
                    }
                )
            }
        }
    }

    if (action in listOf("get", "put")) {
        val scheduleDao = OABX.db.getScheduleDao()
        val schedules by OABX.data.schedulesDb.collectAsState()
        if (schedules.isEmpty())
            DropdownMenuItem(
                text = { Text("--- no schedules ---") },
                onClick = {}
            )
        else {
            DropdownMenuItem(
                enabled = false, onClick = {},
                text = { Text("--- schedules include ---") }
            )
            schedules.forEach { schedule ->
                schedule.name.let { name ->
                    DropdownMenuItem(
                        text = { Text("  $name") },
                        onClick = {
                            when (action) {
                                "get" -> {
                                    val newSelection = schedule.customList.toList()
                                    onAction(newSelection)
                                }

                                "put" -> {
                                    Thread {
                                        scheduleDao.update(
                                            schedule.copy(customList = selection.toSet())
                                        )
                                    }.start()
                                    onAction(selection)
                                }
                            }
                        }
                    )
                }
            }
            DropdownMenuItem(
                enabled = false, onClick = {},
                text = { Text("--- schedules exclude ---") }
            )
            schedules.forEach { schedule ->
                schedule.name.let { name ->
                    DropdownMenuItem(
                        text = { Text("  $name") },
                        onClick = {
                            when (action) {
                                "get" -> {
                                    val newSelection = schedule.blockList.toList()
                                    onAction(newSelection)
                                }

                                "put" -> {
                                    Thread {
                                        scheduleDao.update(
                                            schedule.copy(blockList = selection.toSet())
                                        )
                                    }.start()
                                    onAction(selection)
                                }
                            }
                        }
                    )
                }
            }
        }
        DropdownMenuItem(
            enabled = false, onClick = {},
            text = { Text("--- global ---") }
        )
        DropdownMenuItem(
            text = { Text("  blocklist") },
            onClick = {
                when (action) {
                    "get" -> {
                        val newSelection =
                            OABX.data.getBlocklist()
                        onAction(newSelection)
                    }

                    "put" -> {
                        OABX.data.setBlocklist(selection.toSet())
                        onAction(selection)
                    }
                }
            }
        )
    }
}

@Composable
fun SelectionGetMenu(
    onAction: (List<String>) -> Unit = {},
) {
    Selections(action = "get", onAction = onAction)
}

@Composable
fun SelectionPutMenu(
    selection: List<String>,
    onAction: () -> Unit = {},
) {
    val name = remember { mutableStateOf("") }

    OABX.backupRoot?.let { backupRoot ->
        TextInputMenuItem(
            text = name.value,
            placeholder = "new selection name",
            trailingIcon = Phosphor.ArchiveTray,
        ) {
            name.value = it
            val selectionsDir = backupRoot.ensureDirectory(SELECTIONS_FOLDER_NAME)
            selectionsDir.createFile(name.value)
                .writeText(selection.joinToString("\n"))
            onAction()
        }
    }

    Selections(action = "put", selection = selection) { onAction() }
}

@Composable
fun SelectionRemoveMenu(
    onAction: () -> Unit = {},
) {
    Selections(action = "del") { onAction() }
}

fun List<Package>.withBackups() = filter { it.hasBackups }
fun List<Package>.installed() = filter { it.isInstalled }

fun launchPackagesAction(
    action: String,
    todo: suspend () -> Unit,
) {
    menuScope.launch(menuPool) {
        val name = "menu.$action"
        try {
            beginBusy(name)
            todo()
        } catch (e: Throwable) {
            unexpectedException(e)
        } finally {
            val time = endBusy(name, info = true)
        }
    }
}

suspend fun forEachPackage(
    packages: List<Package>,
    action: String,
    selection: SnapshotStateMap<String, Boolean>,
    select: Boolean? = true,
    parallel: Boolean = true,
    todo: (p: Package) -> Unit = {},
) {
    if (parallel) {
        runParallel(packages, scope = menuScope, pool = menuPool) {
            if (select == true) selection[it.packageName] = false
            traceContextMenu { "$action ${it.packageName}" }
            todo(it)
            select?.let { selected -> selection[it.packageName] = selected }
        }
    } else {
        packages.forEach {
            if (select == true) selection[it.packageName] = false
            traceContextMenu { "$action ${it.packageName}" }
            todo(it)
            yield()
            select?.let { selected -> selection[it.packageName] = selected }
        }
    }
}

fun launchEachPackage(
    packages: List<Package>,
    action: String,
    select: Boolean? = true,
    parallel: Boolean = true,
    todo: (p: Package) -> Unit = {},
) {
    launchPackagesAction(action) {
        forEachPackage(
            packages = packages,
            action = action,
            selection = OABX.data.selection,
            select = select,
            parallel = parallel,
            todo = todo
        )
    }
}

fun launchBackup(packages: List<Package>, mode: Int) {
    val selectedAndInstalled = packages.installed()
    OABX.main?.startBatchAction(
        true,
        selectedAndInstalled.map { it.packageName },
        selectedAndInstalled.map { mode }
    )
}

fun launchRestore(packages: List<Package>, mode: Int) {
    val packagesWithBackups = packages.withBackups()
    OABX.main?.startBatchAction(
        false,
        packagesWithBackups.map { it.packageName },
        packagesWithBackups.map { mode }
    )
}

fun launchEnable(packages: List<Package>) {
    launchEachPackage(packages, "enable", parallel = false) {
        val users = listOf(currentProfile.toString())
        //runAsRoot("pm enable ${it.packageName}")
        ShellCommands.enableDisable(users, it.packageName, true)
        Package.invalidateCacheForPackage(it.packageName)
    }
}

fun launchDisable(packages: List<Package>) {
    launchEachPackage(packages, "disable", parallel = false) {
        val users = listOf(currentProfile.toString())
        //runAsRoot("pm disable ${it.packageName}")
        ShellCommands.enableDisable(users, it.packageName, false)
        Package.invalidateCacheForPackage(it.packageName)
    }
}

fun launchUninstall(packages: List<Package>) {
    launchEachPackage(packages, "uninstall", parallel = false) {
        val users = listOf(currentProfile.toString())
        //runAsRoot("pm uninstall ${it.packageName}")
        ShellCommands.uninstall(users, it.packageName, it.apkPath, it.dataPath, it.isSystem)
        Package.invalidateCacheForPackage(it.packageName)
    }
}

fun launchDeleteBackups(packages: List<Package>) {
    launchEachPackage(packages.withBackups(), "delete backups") {
        it.deleteAllBackups()
        Package.invalidateCacheForPackage(it.packageName)
    }
}

fun launchLimitBackups(packages: List<Package>) {
    launchEachPackage(packages.withBackups(), "limit backups") {
        BackupRestoreHelper.housekeepingPackageBackups(it)
        Package.invalidateCacheForPackage(it.packageName)
    }
}

@Composable
fun MainPackageContextMenu(
    expanded: MutableState<Boolean>,
    packageItem: Package?,
    productsList: List<Package>,
    selection: SnapshotStateMap<String, Boolean>,
    openSheet: (Package) -> Unit = {},
) {
    val visible = productsList

    fun List<Package>.selected() = filter { selection[it.packageName] == true }

    val selectedVisible by remember { mutableStateOf(visible.selected()) }   // freeze selection

    val subMenu = remember {                                    //TODO hg42 var/by ???
        mutableStateOf<(@Composable () -> Unit)?>(null)
    }
    subMenu.value?.let { it() }

    if (!expanded.value)
        closeSubMenu(subMenu)

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    DropdownMenu(
        expanded = expanded.value,
        //offset = DpOffset(20.dp, 0.dp),
        offset = with(LocalDensity.current) {
            DpOffset(
                offsetX.roundToInt().toDp(),
                offsetY.roundToInt().toDp()
            )
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            },
        onDismissRequest = { expanded.value = false }
    ) {

        if (isDebug) {
            val number = remember { mutableIntStateOf(0) }
            DropdownMenuItem(
                text = { Text("test = ${number.intValue}") },
                onClick = {
                    openSubMenu(subMenu) {
                        TextInputMenuItem(
                            text = number.intValue.toString(),
                            onAction = {
                                number.intValue = it.toInt()
                                closeSubMenu(subMenu)
                                //expanded.value = false
                            }
                        )
                    }
                }
            )
        }

        packageItem?.let {

            DropdownMenuItem(
                enabled = false, onClick = {},
                text = { Text(packageItem.packageName) }
            )

            DropdownMenuItem(
                text = { Text("Open App Sheet") },
                onClick = {
                    expanded.value = false
                    openSheet(packageItem)
                }
            )

            HorizontalDivider() //------------------------------------------------------------------
        }

        DropdownMenuItem(
            enabled = false, onClick = {},
            text = { Text("selection:") }
        )

        DropdownMenuItem(
            text = { Text("Select Visible") },
            onClick = {
                expanded.value = false
                visible.forEach { selection[it.packageName] = true }
            }
        )

        DropdownMenuItem(
            text = { Text("Deselect Visible") },
            onClick = {
                expanded.value = false
                visible.forEach { selection[it.packageName] = false }
            }
        )

        DropdownMenuItem(
            text = { Text("Deselect Not Visible") },
            onClick = {
                expanded.value = false
                (selection.keys - visible.map { it.packageName }.toSet()).forEach {
                    selection[it] = false
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Deselect All") },
            onClick = {
                expanded.value = false
                selection.clear()   //TODO hg42 ???
            }
        )

        DropdownMenuItem(
            text = { Text("Get...") },
            onClick = {
                openSubMenu(subMenu) {
                    SelectionGetMenu { selectionLoaded ->
                        expanded.value = false
                        selection.clear()
                        selectionLoaded.forEach { selection[it] = true }
                    }
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Put...") },
            onClick = {
                openSubMenu(subMenu) {
                    SelectionPutMenu(
                        selection = selection.filter { it.value }.map { it.key }
                    ) {
                        expanded.value = false
                        //launchSelect(selectedVisible)
                    }
                }
            }
        )

        DropdownMenuItem(
            text = { Text("Remove...") },
            onClick = {
                openSubMenu(subMenu) {
                    SelectionRemoveMenu {
                        expanded.value = false
                        //launchSelect(selectedVisible)
                    }
                }
            }
        )

        if (selection.count { it.value } > 0) {

            HorizontalDivider() //------------------------------------------------------------------

            DropdownMenuItem(
                enabled = false, onClick = {},
                text = { Text("${selectedVisible.count()} selected and visible items:") }
            )

            DropdownMenuItem(
                text = { Text("Backup...") },
                onClick = {
                    openSubMenu(subMenu) {
                        SelectDataParts(expanded) { mode ->
                            expanded.value = false
                            launchBackup(selectedVisible, mode)
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Restore...") },
                onClick = {
                    openSubMenu(subMenu) {
                        SelectDataParts(expanded) { mode ->
                            expanded.value = false
                            launchRestore(selectedVisible, mode)
                        }
                    }
                }
            )

            HorizontalDivider() //------------------------------------------------------------------

            DropdownMenuItem(
                text = { Text("Enable") },
                onClick = {
                    expanded.value = false
                    launchEnable(selectedVisible)
                }
            )

            DropdownMenuItem(
                text = { Text("Disable...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation(expanded) {
                            launchDisable(selectedVisible)
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Uninstall...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation(expanded) {
                            launchUninstall(selectedVisible)
                        }
                    }
                }
            )

            HorizontalDivider() //------------------------------------------------------------------

            DropdownMenuItem(
                text = { Text("Delete All Backups...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation(expanded) {
                            launchDeleteBackups(selectedVisible)
                        }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("Limit Backups...") },
                onClick = {
                    openSubMenu(subMenu) {
                        Confirmation(expanded) {
                            launchLimitBackups(selectedVisible)
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPackageItem(
    pkg: Package,
    selected: Boolean,
    imageLoader: ImageLoader,
    onLongClick: (Package) -> Unit = {},
    onAction: (Package) -> Unit = {},
) {
    beginNanoTimer("item")

    //traceCompose { "<${pkg.packageName}> MainPackageItemX ${pkg.packageInfo.icon} ${imageData.hashCode()}" }
    //traceCompose { "<${pkg.packageName}> MainPackageItemX" }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = { onAction(pkg) },
                onLongClick = { onLongClick(pkg) }
            ),
        colors = ListItemDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            else Color.Transparent,
        ),
        leadingContent = {
            PackageIcon(
                modifier = Modifier.alpha(if (pkg.isSpecial && pkg.packageInfo !is SpecialInfo) 0.4f else 1f),  //TODO hg42 pkg.cannotHandle or similar
                item = pkg,
                imageData = pkg.iconData,
                imageLoader = imageLoader,
            )
        },
        headlineContent = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = pkg.packageLabel,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                beginNanoTimer("item.labels")
                PackageLabels(item = pkg)
                endNanoTimer("item.labels")
            }

        },
        supportingContent = {
            Row(modifier = Modifier.fillMaxWidth()) {

                val hasBackups = pkg.hasBackups
                val latestBackup = pkg.latestBackup
                val nBackups = pkg.numberOfBackups

                beginNanoTimer("item.package")
                Text(
                    text = pkg.packageName,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                )
                endNanoTimer("item.package")

                beginNanoTimer("item.backups")
                ShowIf(hasBackups) {
                    Text(
                        text = (latestBackup?.backupDate?.getFormattedDate(
                            false
                        ) ?: "") + " • $nBackups",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                endNanoTimer("item.backups")
            }
        },
    )

    endNanoTimer("item")

    if (traceTiming.pref.value)
        nanoTiming["item.package"]?.let {
            if (it.second > 0 && it.second % logEachN == 0L) {
                logNanoTiming()
                //clearNanoTiming("item")
            }
        }
}
