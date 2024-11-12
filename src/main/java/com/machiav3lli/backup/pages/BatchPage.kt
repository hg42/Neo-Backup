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
package com.machiav3lli.backup.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ALT_MODE_APK
import com.machiav3lli.backup.ALT_MODE_BOTH
import com.machiav3lli.backup.ALT_MODE_DATA
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.BatchActionDialogUI
import com.machiav3lli.backup.dialogs.GlobalBlockListDialogUI
import com.machiav3lli.backup.entity.Package
import com.machiav3lli.backup.preferences.pref_singularBackupRestore
import com.machiav3lli.backup.sheets.BatchPrefsSheet
import com.machiav3lli.backup.sheets.SortFilterSheet
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.DiamondsFour
import com.machiav3lli.backup.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.HardDrives
import com.machiav3lli.backup.ui.compose.icons.phosphor.Nut
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.item.ActionChip
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.StateChip
import com.machiav3lli.backup.ui.compose.recycler.BatchPackageRecycler
import com.machiav3lli.backup.ui.compose.theme.ColorAPK
import com.machiav3lli.backup.ui.compose.theme.ColorData
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.utils.altModeToMode
import com.machiav3lli.backup.viewmodels.BatchVM
import com.machiav3lli.backup.viewmodels.MainVM
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchPage(
    viewModel: BatchVM,
    mainVM: MainVM = koinViewModel(),
    backupBoolean: Boolean
) {
    val main = OABX.main!!
    val scope = rememberCoroutineScope()
    val filteredList by if (backupBoolean) mainVM.backupFilteredList.collectAsState(emptyList())
    else mainVM.restoreFilteredList.collectAsState(emptyList())
    val scaffoldState = rememberBottomSheetScaffoldState()
    val openBatchDialog = remember { mutableStateOf(false) }
    val openBlocklist = rememberSaveable { mutableStateOf(false) }
    val prefsNotFilter = remember { mutableStateOf(false) }

    val filterPredicate = { item: Package ->
        if (backupBoolean) item.isInstalled else item.hasBackups
    }
    val workList = filteredList.filter(filterPredicate)

    val allApkChecked by remember(workList, viewModel.apkBackupCheckedList) {
        derivedStateOf {
            viewModel.apkBackupCheckedList.filterValues { it != -1 }.size == workList
                .filter { !it.isSpecial && (backupBoolean || it.latestBackup?.hasApk == true) }
                .size
        }
    }
    val allDataChecked by remember(workList, viewModel.dataBackupCheckedList) {
        derivedStateOf {
            viewModel.dataBackupCheckedList.filterValues { it != -1 }.size == workList
                .filter { backupBoolean || it.latestBackup?.hasData == true }
                .size
        }
    }

    val selection = remember { mutableStateMapOf<Package, Boolean>() }
    filteredList.forEach {
        selection.putIfAbsent(it, false)
    }

    BackHandler(scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        sheetShape = MaterialTheme.shapes.extraSmall,
        sheetContent = {
            // bottom sheets even recomposit when hidden
            // which is bad when they contain live content
            if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) {
                if (prefsNotFilter.value) BatchPrefsSheet(backupBoolean)
                else SortFilterSheet(
                    sourcePage = if (backupBoolean) NavItem.Backup
                    else NavItem.Restore,
                    onDismiss = {
                        scope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    },
                )
            } else {
                // inexpensive and small placeholder while hidden
                // necessary because empty sheets are kept hidden
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        topBar = {
            Column {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionChip(
                        modifier = Modifier.weight(1f),
                        icon = Phosphor.Prohibit,
                        text = stringResource(id = R.string.sched_blocklist),
                        positive = false,
                        fullWidth = true,
                    ) {
                        openBlocklist.value = true
                    }
                    ActionChip(
                        modifier = Modifier.weight(1f),
                        icon = Phosphor.FunnelSimple,
                        text = stringResource(id = R.string.sort_and_filter),
                        positive = true,
                        fullWidth = true,
                    ) {
                        scope.launch {
                            prefsNotFilter.value = false
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                }
                HorizontalDivider(
                    thickness = 2.dp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            BatchPackageRecycler(
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxSize(),
                productsList = workList,
                restore = !backupBoolean,
                apkBackupCheckedList = viewModel.apkBackupCheckedList,
                dataBackupCheckedList = viewModel.dataBackupCheckedList,
                onBackupApkClick = { packageName: String, b: Boolean, i: Int ->
                    if (b) viewModel.apkBackupCheckedList[packageName] = i
                    else if (viewModel.apkBackupCheckedList[packageName] == i)
                        viewModel.apkBackupCheckedList[packageName] = -1
                },
                onBackupDataClick = { packageName: String, b: Boolean, i: Int ->
                    if (b) viewModel.dataBackupCheckedList[packageName] = i
                    else if (viewModel.dataBackupCheckedList[packageName] == i)
                        viewModel.dataBackupCheckedList[packageName] = -1
                },
            ) { item, checkApk, checkData ->
                when (checkApk) {
                    true -> viewModel.apkBackupCheckedList[item.packageName] = 0
                    else -> viewModel.apkBackupCheckedList[item.packageName] = -1
                }
                when (checkData) {
                    true -> viewModel.dataBackupCheckedList[item.packageName] = 0
                    else -> viewModel.dataBackupCheckedList[item.packageName] = -1
                }
            }
            HorizontalDivider(
                thickness = 2.dp,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StateChip(
                    icon = Phosphor.DiamondsFour,
                    text = stringResource(id = R.string.all_apk),
                    checked = allApkChecked,
                    color = ColorAPK,
                    index = 0,
                    count = 2,
                ) {
                    val checkBoolean = !allApkChecked
                    when {
                        checkBoolean -> workList
                            .filter { (backupBoolean && !it.isSpecial) || it.latestBackup?.hasApk == true }
                            .map(Package::packageName)
                            .forEach {
                                viewModel.apkBackupCheckedList[it] = 0
                            }

                        else         -> workList
                            .filter { backupBoolean || it.latestBackup?.hasApk == true }
                            .map(Package::packageName)
                            .forEach {
                                viewModel.apkBackupCheckedList[it] = -1
                            }
                    }
                }
                StateChip(
                    icon = Phosphor.HardDrives,
                    text = stringResource(id = R.string.all_data),
                    checked = allDataChecked,
                    color = ColorData,
                    index = 1,
                    count = 2,
                ) {
                    val checkBoolean = !allDataChecked
                    when {
                        checkBoolean -> workList
                            .filter { backupBoolean || it.latestBackup?.hasData == true }
                            .map(Package::packageName)
                            .forEach {
                                viewModel.dataBackupCheckedList[it] = 0
                            }

                        else         -> workList
                            .filter { backupBoolean || it.latestBackup?.hasData == true }
                            .map(Package::packageName)
                            .forEach {
                                viewModel.dataBackupCheckedList[it] = -1
                            }
                    }
                }
                RoundButton(icon = Phosphor.Nut) {
                    scope.launch {
                        prefsNotFilter.value = true
                        scaffoldState.bottomSheetState.expand()
                    }
                }
                ElevatedActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = if (backupBoolean) R.string.backup else R.string.restore),
                    positive = true
                ) {
                    if (viewModel.apkBackupCheckedList.filterValues { it != -1 }.isNotEmpty()
                        || viewModel.dataBackupCheckedList.filterValues { it != -1 }.isNotEmpty()
                    ) openBatchDialog.value = true
                }
            }
        }

        if (openBlocklist.value) BaseDialog(onDismiss = { openBlocklist.value = false }) {
            GlobalBlockListDialogUI(
                currentBlocklist = mainVM.getBlocklist().toSet(),
                openDialogCustom = openBlocklist,
            ) { newSet ->
                mainVM.setBlocklist(newSet)
            }
        }
        if (openBatchDialog.value) BaseDialog(onDismiss = { openBatchDialog.value = false }) {
            val selectedApk = viewModel.apkBackupCheckedList.filterValues { it != -1 }
            val selectedData = viewModel.dataBackupCheckedList.filterValues { it != -1 }
            val selectedPackageNames = selectedApk.keys.plus(selectedData.keys).distinct()

            BatchActionDialogUI(
                backupBoolean = backupBoolean,
                selectedPackageInfos = filteredList
                    .filter { it.packageName in selectedPackageNames }
                    .map(Package::packageInfo),
                selectedApk = selectedApk,
                selectedData = selectedData,
                onDismiss = { openBatchDialog.value = false },
            ) {
                if (pref_singularBackupRestore.value && !backupBoolean) main.startBatchRestoreAction(
                    selectedPackageNames = selectedPackageNames,
                    selectedApk = selectedApk,
                    selectedData = selectedData,
                )
                else main.startBatchAction(
                    backupBoolean,
                    selectedPackageNames = selectedPackageNames,
                    selectedModes = selectedPackageNames.map { pn ->
                        altModeToMode(
                            when {
                                selectedData[pn] == 0 && selectedApk[pn] == 0 -> ALT_MODE_BOTH
                                selectedData[pn] == 0                         -> ALT_MODE_DATA
                                else                                          -> ALT_MODE_APK
                            }, backupBoolean
                        )
                    }
                )
            }
        }
    }
}