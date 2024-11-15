package com.machiav3lli.backup.ui.compose.recycler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.imageLoader
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.entity.ChipItem
import com.machiav3lli.backup.entity.InfoChipItem
import com.machiav3lli.backup.entity.Log
import com.machiav3lli.backup.entity.Package
import com.machiav3lli.backup.entity.StorageFile
import com.machiav3lli.backup.preferences.pref_multilineInfoChips
import com.machiav3lli.backup.preferences.pref_singularBackupRestore
import com.machiav3lli.backup.ui.compose.item.BatchPackageItem
import com.machiav3lli.backup.ui.compose.item.ExportedScheduleItem
import com.machiav3lli.backup.ui.compose.item.InfoChip
import com.machiav3lli.backup.ui.compose.item.LogItem
import com.machiav3lli.backup.ui.compose.item.MainPackageItem
import com.machiav3lli.backup.ui.compose.item.RestorePackageItem
import com.machiav3lli.backup.ui.compose.item.ScheduleItem
import com.machiav3lli.backup.ui.compose.item.SelectionChip
import com.machiav3lli.backup.ui.compose.item.UpdatedPackageItem

@Composable
fun HomePackageRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Package>,
    selection: Set<String>,
    onLongClick: (Package) -> Unit = {},
    onClick: (Package) -> Unit = {},
) {
    val imageLoader = LocalContext.current.imageLoader

    InnerBackground(modifier) {
        VerticalItemList(
            list = productsList,
            itemKey = { it.packageName }
        ) {
            MainPackageItem(
                it,
                selection.contains(it.packageName),
                imageLoader,
                onLongClick,
                onClick
            )
        }
    }
}

@Composable
fun UpdatedPackageRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Package>?,
    onClick: (Package) -> Unit = {},
) {
    HorizontalItemList(
        modifier = modifier,
        list = productsList,
        itemKey = { it.packageName }
    ) {
        UpdatedPackageItem(it, onClick)
    }
}

@Composable
fun BatchPackageRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Package>?,
    restore: Boolean = false,
    apkBackupCheckedList: SnapshotStateMap<String, Int>,
    dataBackupCheckedList: SnapshotStateMap<String, Int>,
    onBackupApkClick: (String, Boolean, Int) -> Unit = { _: String, _: Boolean, _: Int -> },
    onBackupDataClick: (String, Boolean, Int) -> Unit = { _: String, _: Boolean, _: Int -> },
    onClick: (Package, Boolean, Boolean) -> Unit = { _: Package, _: Boolean, _: Boolean -> },
) {
    InnerBackground(modifier) {
        VerticalItemList(
            list = productsList,
            itemKey = { it.packageName }
        ) {
            val apkBackupChecked = remember(apkBackupCheckedList[it.packageName]) {
                mutableStateOf(apkBackupCheckedList[it.packageName])
            }
            val dataBackupChecked = remember(dataBackupCheckedList[it.packageName]) {
                mutableStateOf(dataBackupCheckedList[it.packageName])
            }

            if (restore && pref_singularBackupRestore.value) RestorePackageItem(
                it,
                apkBackupChecked,
                dataBackupChecked,
                onClick,
                onBackupApkClick,
                onBackupDataClick,
            )
            else BatchPackageItem(
                it,
                restore,
                apkBackupChecked.value == 0,
                dataBackupChecked.value == 0,
                onClick,
                onApkClick = { p, b ->
                    onBackupApkClick(p.packageName, b, 0)
                },
                onDataClick = { p, b ->
                    onBackupDataClick(p.packageName, b, 0)
                }
            )
        }
    }
}

@Composable
fun ScheduleRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Schedule>?,
    onClick: (Schedule) -> Unit = {},
    onRun: (Schedule) -> Unit = {},
    onCheckChanged: (Schedule, Boolean) -> Unit = { _: Schedule, _: Boolean -> },
) {
    InnerBackground(modifier) {
        VerticalItemList(
            list = productsList
        ) {
            ScheduleItem(it, onClick, onRun, onCheckChanged)
        }
    }
}

@Composable
fun ExportedScheduleRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Pair<Schedule, StorageFile>>?,
    onImport: (Schedule) -> Unit = {},
    onDelete: (StorageFile) -> Unit = {},
) {
    InnerBackground(modifier) {
        VerticalItemList(
            list = productsList
        ) {
            ExportedScheduleItem(it.first, it.second, onImport, onDelete)
        }
    }
}

@Composable
fun LogRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Log>?,
    onShare: (Log) -> Unit = {},
    onDelete: (Log) -> Unit = {},
) {
    InnerBackground(modifier) {
        VerticalItemList(
            list = productsList
        ) {
            LogItem(it, onShare, onDelete)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoChipsBlock(
    modifier: Modifier = Modifier,
    list: List<InfoChipItem>,
) {
    if (pref_multilineInfoChips.value)
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            list.forEach { chip ->
                InfoChip(item = chip)
            }
        }
    else LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        items(list) { chip ->
            InfoChip(item = chip)
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectableChipGroup(
    //TODO hg42 move to item/Components.kt ?
    modifier: Modifier = Modifier,
    list: List<ChipItem>,
    selectedFlag: Int,
    onClick: (Int) -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        list.forEach { item ->
            SelectionChip(
                item = item,
                isSelected = item.flag == selectedFlag,
            ) {
                onClick(item.flag)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectableChipGroup(
    //TODO hg42 move to item/Components.kt ?
    modifier: Modifier = Modifier,
    list: List<ChipItem>,
    selectedFlags: Int,
    onClick: (Int, Int) -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        list.forEach { item ->
            SelectionChip(
                item = item,
                isSelected = item.flag and selectedFlags != 0,
            ) {
                onClick(selectedFlags xor item.flag, item.flag)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectableChipGroup(
    modifier: Modifier = Modifier,
    list: Set<String>,
    selected: Set<String>,
    onClick: (Set<String>) -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        list.forEach { item ->
            SelectionChip(
                label = item,
                isSelected = item in selected,
            ) {
                onClick(if (item in selected) selected - item else selected + item)
            }
        }
    }
}
