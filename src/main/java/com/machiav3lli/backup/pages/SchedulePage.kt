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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.DialogMode
import com.machiav3lli.backup.EnabledFilter
import com.machiav3lli.backup.LatestFilter
import com.machiav3lli.backup.LaunchableFilter
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT
import com.machiav3lli.backup.MAIN_FILTER_DEFAULT_WITHOUT_SPECIAL
import com.machiav3lli.backup.MODE_UNSET
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.UpdatedFilter
import com.machiav3lli.backup.dbs.entity.Schedule
import com.machiav3lli.backup.dialogs.ActionsDialogUI
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.BlockListDialogUI
import com.machiav3lli.backup.dialogs.CustomListDialogUI
import com.machiav3lli.backup.dialogs.IntPickerDialogUI
import com.machiav3lli.backup.dialogs.StringInputDialogUI
import com.machiav3lli.backup.dialogs.TimePickerDialogUI
import com.machiav3lli.backup.enabledFilterChipItems
import com.machiav3lli.backup.entity.ChipItem
import com.machiav3lli.backup.latestFilterChipItems
import com.machiav3lli.backup.launchableFilterChipItems
import com.machiav3lli.backup.mainFilterChipItems
import com.machiav3lli.backup.preferences.traceDebug
import com.machiav3lli.backup.scheduleBackupModeChipItems
import com.machiav3lli.backup.tasks.ScheduleWork
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.backup.ui.compose.icons.phosphor.CheckCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.Clock
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.Play
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.backup.ui.compose.item.CardButton
import com.machiav3lli.backup.ui.compose.item.CheckChip
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.item.ExpandableBlock
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TitleText
import com.machiav3lli.backup.ui.compose.recycler.MultiSelectableChipGroup
import com.machiav3lli.backup.ui.compose.recycler.SelectableChipGroup
import com.machiav3lli.backup.updatedFilterChipItems
import com.machiav3lli.backup.utils.getStartScheduleMessage
import com.machiav3lli.backup.utils.specialBackupsEnabled
import com.machiav3lli.backup.utils.timeLeft
import com.machiav3lli.backup.viewmodels.ScheduleVM
import org.koin.androidx.compose.koinViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SchedulePage(
    scheduleId: Long,
    viewModel: ScheduleVM = koinViewModel(),
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    val dialogProps: MutableState<Pair<DialogMode, Schedule>> = remember {
        mutableStateOf(Pair(DialogMode.NONE, Schedule()))
    }
    val schedule by viewModel.schedule.collectAsState(null)
    val customList by viewModel.customList.collectAsState(emptySet())
    val blockList by viewModel.blockList.collectAsState(emptySet())
    val allTags by viewModel.allTags.collectAsState()

    LaunchedEffect(scheduleId) {
        viewModel.setSchedule(scheduleId)
    }

    schedule?.let { schedule ->
        val (absTime, relTime) = timeLeft(schedule)
            .collectAsState().value


        fun refresh(
            schedule: Schedule,
            rescheduleBoolean: Boolean,
        ) = viewModel.updateSchedule(schedule, rescheduleBoolean)

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                    leadingContent = {
                        TitleText(R.string.sched_name)
                    },
                    headlineContent = {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = Color.Transparent
                            ),
                            shape = MaterialTheme.shapes.large,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            onClick = {
                                dialogProps.value = Pair(DialogMode.SCHEDULE_NAME, schedule)
                                openDialog.value = true
                            }
                        ) {
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 16.dp),
                                text = schedule.name,
                                textAlign = TextAlign.Center,
                            )
                        }
                    },
                    trailingContent = {
                        RoundButton(
                            icon = Phosphor.CaretDown,
                            description = stringResource(id = R.string.dismiss),
                            onClick = { onDismiss() }
                        )
                    }
                )
                HorizontalDivider(
                    thickness = 2.dp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
            LazyColumn(
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp),
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CardButton(
                            modifier = Modifier.weight(0.5f),
                            icon = Phosphor.Clock,
                            contentColor = MaterialTheme.colorScheme.inverseSurface,
                            description = "${stringResource(id = R.string.sched_hourOfDay)} ${
                                LocalTime.of(
                                    schedule.timeHour,
                                    schedule.timeMinute
                                )
                            }",
                        ) {
                            dialogProps.value = Pair(DialogMode.TIME_PICKER, schedule)
                            openDialog.value = true
                        }
                        CardButton(
                            modifier = Modifier.weight(0.5f),
                            icon = Phosphor.ClockClockwise,
                            contentColor = MaterialTheme.colorScheme.inverseSurface,
                            description = "${stringResource(id = R.string.sched_interval)} ${schedule.interval}",
                        ) {
                            dialogProps.value = Pair(DialogMode.INTERVAL_SETTER, schedule)
                            openDialog.value = true
                        }
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CardButton(
                            modifier = Modifier.weight(0.5f),
                            icon = Phosphor.CheckCircle,
                            description = stringResource(id = R.string.customListTitle),
                            containerColor = if (customList.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.tertiaryContainer,
                        ) {
                            dialogProps.value = Pair(DialogMode.CUSTOMLIST, schedule)
                            openDialog.value = true
                        }
                        CardButton(
                            modifier = Modifier.weight(0.5f),
                            icon = Phosphor.Prohibit,
                            description = stringResource(id = R.string.sched_blocklist),
                            containerColor = if (blockList.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.tertiaryContainer,
                        ) {
                            dialogProps.value = Pair(DialogMode.BLOCKLIST, schedule)
                            openDialog.value = true
                        }
                    }
                }
                item {
                    ExpandableBlock(
                        heading = stringResource(id = R.string.filters_backup),
                        preExpanded = true,
                    ) {
                        MultiSelectableChipGroup(
                            list = scheduleBackupModeChipItems,
                            selectedFlags = schedule.mode
                        ) { flags, flag ->
                            traceDebug { "*** onClick mode ${schedule.mode} xor $flag -> $flags (${schedule.mode xor flag})" }
                            refresh(
                                schedule.copy(mode = flags),
                                false,
                            )
                        }
                    }
                }
                item {
                    ExpandableBlock(
                        heading = stringResource(id = R.string.filters_app),
                        preExpanded = schedule.filter != if (specialBackupsEnabled) MAIN_FILTER_DEFAULT
                        else MAIN_FILTER_DEFAULT_WITHOUT_SPECIAL,
                    ) {
                        MultiSelectableChipGroup(
                            list = if (specialBackupsEnabled)
                                mainFilterChipItems
                            else
                                mainFilterChipItems.minus(ChipItem.Special),
                            selectedFlags = schedule.filter
                        ) { flags, flag ->
                            refresh(
                                schedule.copy(filter = flags),
                                false,
                            )
                        }
                    }
                }
                item {
                    ExpandableBlock(
                        heading = stringResource(id = R.string.filters_launchable),
                        preExpanded = schedule.launchableFilter != LaunchableFilter.ALL.ordinal,
                    ) {
                        SelectableChipGroup(
                            list = launchableFilterChipItems,
                            selectedFlag = schedule.launchableFilter
                        ) { flag ->
                            refresh(
                                schedule.copy(launchableFilter = flag),
                                false,
                            )
                        }
                    }
                }
                item {
                    ExpandableBlock(
                        heading = stringResource(id = R.string.filters_updated),
                        preExpanded = schedule.updatedFilter != UpdatedFilter.ALL.ordinal,
                    ) {
                        SelectableChipGroup(
                            list = updatedFilterChipItems,
                            selectedFlag = schedule.updatedFilter
                        ) { flag ->
                            refresh(
                                schedule.copy(updatedFilter = flag),
                                false,
                            )
                        }
                    }
                }
                item {
                    ExpandableBlock(
                        heading = stringResource(id = R.string.filters_latest),
                        preExpanded = schedule.latestFilter != LatestFilter.ALL.ordinal,
                    ) {
                        SelectableChipGroup(
                            list = latestFilterChipItems,
                            selectedFlag = schedule.latestFilter
                        ) { flag ->
                            refresh(
                                schedule.copy(latestFilter = flag),
                                false,
                            )
                        }
                    }
                }
                item {
                    ExpandableBlock(
                        heading = stringResource(id = R.string.filters_enabled),
                        preExpanded = schedule.enabledFilter != EnabledFilter.ALL.ordinal,
                    ) {
                        SelectableChipGroup(
                            list = enabledFilterChipItems,
                            selectedFlag = schedule.enabledFilter
                        ) { flag ->
                            refresh(
                                schedule.copy(enabledFilter = flag),
                                false,
                            )
                        }
                    }
                }
                if (allTags.isNotEmpty()) item {
                    ExpandableBlock(
                        heading = stringResource(id = R.string.filters_tags),
                        preExpanded = schedule.tagsList.isNotEmpty(),
                    ) {
                        MultiSelectableChipGroup(
                            list = allTags.toSet(),
                            selected = schedule.tagsList,
                        ) { tags ->
                            refresh(
                                schedule.copy(tagsList = tags),
                                false,
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                thickness = 2.dp,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CheckChip(
                        checked = schedule.enabled,
                        textId = R.string.sched_checkbox,
                        checkedTextId = R.string.enabled,
                        onCheckedChange = { checked ->
                            refresh(
                                schedule.copy(enabled = checked),
                                true,
                            )
                        }
                    )
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        maxLines = 2
                    ) {
                        Text(text = "🕒 $absTime")
                        if (schedule.enabled) Text(text = "⏳ $relTime") // TODO replace by resource icons
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ElevatedActionButton(
                        text = stringResource(id = R.string.delete),
                        icon = Phosphor.TrashSimple,
                        positive = false,
                        fullWidth = false
                    ) {
                        viewModel.deleteSchedule()
                        ScheduleWork.cancel(context, scheduleId)
                        onDismiss()
                    }
                    ElevatedActionButton(
                        text = stringResource(id = R.string.sched_activateButton),
                        icon = Phosphor.Play,
                        fullWidth = true,
                        onClick = {
                            dialogProps.value = Pair(DialogMode.SCHEDULE_RUN, schedule)
                            openDialog.value = true
                        }
                    )
                }
            }
        }

        if (openDialog.value) BaseDialog(onDismiss = { openDialog.value = false }) {
            dialogProps.value.let { (dialogMode, schedule) ->
                when (dialogMode) {
                    DialogMode.BLOCKLIST
                        -> BlockListDialogUI(
                        schedule = schedule,
                        openDialogCustom = openDialog,
                    ) { newSet ->
                        refresh(
                            schedule.copy(blockList = newSet),
                            rescheduleBoolean = false,
                        )
                    }

                    DialogMode.CUSTOMLIST
                        -> CustomListDialogUI(
                        schedule = schedule,
                        openDialogCustom = openDialog,
                    ) { newSet ->
                        refresh(
                            schedule.copy(customList = newSet),
                            rescheduleBoolean = false,
                        )
                    }

                    DialogMode.TIME_PICKER
                        -> TimePickerDialogUI(
                        state = rememberTimePickerState(
                            initialHour = schedule.timeHour,
                            initialMinute = schedule.timeMinute,
                        ),
                        openDialogCustom = openDialog,
                    ) { hour, minute ->
                        refresh(
                            schedule.copy(timeHour = hour, timeMinute = minute),
                            rescheduleBoolean = true,
                        )
                    }

                    DialogMode.INTERVAL_SETTER
                        -> IntPickerDialogUI(
                        value = schedule.interval,
                        defaultValue = 1,
                        entries = (1..30).toList(),
                        openDialogCustom = openDialog,
                    ) {
                        refresh(
                            schedule.copy(interval = it),
                            rescheduleBoolean = true,
                        )
                    }

                    DialogMode.SCHEDULE_NAME
                        -> StringInputDialogUI(
                        titleText = stringResource(id = R.string.sched_name),
                        initValue = schedule.name,
                        openDialogCustom = openDialog
                    ) {
                        refresh(
                            schedule.copy(name = it),
                            rescheduleBoolean = false,
                        )
                    }

                    DialogMode.SCHEDULE_RUN
                        -> ActionsDialogUI(
                        titleText = "${schedule.name}: ${stringResource(R.string.sched_activateButton)}?",
                        messageText = context.getStartScheduleMessage(schedule),
                        onDismiss = { openDialog.value = false },
                        primaryText = stringResource(R.string.dialogOK),
                        primaryAction = {
                            if (schedule.mode != MODE_UNSET)
                                ScheduleWork.schedule(OABX.context, schedule, true)
                        },
                    )

                    else -> {}
                }
            }
        }
    }
}
