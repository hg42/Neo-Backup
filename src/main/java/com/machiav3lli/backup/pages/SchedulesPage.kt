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

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ICON_SIZE_SMALL
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.sheets.ScheduleSheet
import com.machiav3lli.backup.traceCompose
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.CalendarPlus
import com.machiav3lli.backup.ui.compose.recycler.ScheduleRecycler
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.utils.specialBackupsEnabled
import com.machiav3lli.backup.viewmodels.ScheduleViewModel
import com.machiav3lli.backup.viewmodels.SchedulesViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesPage(viewModel: SchedulesViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val schedules by viewModel.schedules.collectAsState(emptyList())
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scheduleSheetId = remember { mutableLongStateOf(-1L) }

    LaunchedEffect(scheduleSheetId.longValue) {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
            if (scheduleSheetId.longValue < 0)
                scaffoldState.bottomSheetState.partialExpand()
        } else {
            if (scheduleSheetId.longValue >= 0)
                scaffoldState.bottomSheetState.expand()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        sheetContent = {

            traceDebug { "SchedulerPage sheetContent ${scaffoldState.bottomSheetState.currentValue}" }

            //if (scheduleSheetId.longValue >= 0) {
            //if(scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded && scheduleSheetId.longValue >= 0) {
            if (scaffoldState.bottomSheetState.isVisible && scheduleSheetId.longValue >= 0) {

                // BackHandler needs to be conditional, because all pages may be composed (sliding)
                // and sheets are also composed when hidden
                BackHandler {
                    traceCompose { "SchedulerPage sheet BackHandler" }
                    scheduleSheetId.longValue = -1L
                }

                ScheduleSheet(
                    //viewModel = scheduleSheetVM,
                    viewModel = ScheduleViewModel(
                        scheduleSheetId.longValue,
                        OABX.db.getScheduleDao()
                    ),
                    scheduleId = scheduleSheetId.longValue,
                    onDismiss = {
                        scheduleSheetId.longValue = -1L
                    }
                )
            } else {
                // inexpensive and small placeholder while hidden,
                // because bottom sheets even recomposite when hidden,
                // which is bad when they contain live content,
                // spacer is necessary because empty sheets never unhide
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ExtendedFloatingActionButton(
                        text = { Text("ex/import") },
                        icon = {
                            Icon(
                                modifier = Modifier.size(ICON_SIZE_SMALL),
                                imageVector = Phosphor.ArchiveTray,
                                contentDescription = stringResource(id = R.string.prefs_schedulesexportimport_summary)
                            )
                        },
                        onClick = {
                            OABX.main?.moveTo(NavItem.Exports.destination)
                        }
                    )
                    ExtendedFloatingActionButton(
                        text = { Text("add") },
                        icon = {
                            Icon(
                                modifier = Modifier.size(ICON_SIZE_SMALL),
                                imageVector = Phosphor.CalendarPlus,
                                contentDescription = stringResource(id = R.string.sched_add)
                            )
                        },
                        onClick = { viewModel.addSchedule(specialBackupsEnabled) }
                    )
                }
            }
        ) {
            ScheduleRecycler(
                productsList = schedules,
                onClick = { item ->
                    scheduleSheetId.longValue = item.id
                },
                onCheckChanged = { item: Schedule, b: Boolean ->
                    viewModel.updateSchedule(
                        item.copy(enabled = b),
                        false,
                    )
                }
            )
        }
    }
}
