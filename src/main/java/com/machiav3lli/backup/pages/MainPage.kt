/*
 * Neo Backup: open-source apps backup and restore app.
 * Copyright (C) 2023  Antonios Hazim
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.GlobalBlockListDialogUI
import com.machiav3lli.backup.sheets.SortFilterSheet
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.item.ActionChip
import com.machiav3lli.backup.ui.compose.item.ExpandableSearchAction
import com.machiav3lli.backup.ui.compose.item.RefreshButton
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.compose.recycler.FullScreenBackground
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.ui.navigation.PagerNavBar
import com.machiav3lli.backup.ui.navigation.SlidePager
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pages = persistentListOf(
        NavItem.Home,
        NavItem.Backup,
        NavItem.Restore,
        NavItem.Scheduler,
    )
    val scaffoldState = rememberBottomSheetScaffoldState()

    //TODO wech begin ??? or is this necessary with resume or similar?
    //TODO would this not need SideEffect or LaunchEffect? what about life cycles?

    //TODO why check again if this is recomposed?
    //OABX.appsSuspendedChecked = false

    //TODO isn't it enough to set this in OABX? (hg42: I added it there now)
    //if (pref_catchUncaughtException.value) {
    //    Thread.setDefaultUncaughtExceptionHandler { _, e ->
    //        try {
    //            Timber.i("\n\n" + "=".repeat(60))
    //            LogsHandler.unexpectedException(e)
    //            LogsHandler.logErrors("uncaught: ${e.message}")
    //            if (pref_uncaughtExceptionsJumpToPreferences.value) {
    //                context.restartApp(RESCUE_NAV)
    //            }
    //            object : Thread() {
    //                override fun run() {
    //                    Looper.prepare()
    //                    Looper.loop()
    //                }
    //            }.start()
    //        } catch (_: Throwable) {
    //            // ignore
    //        } finally {
    //            exitProcess(2)
    //        }
    //    }
    //}

    //Shell.getShell()

    //TODO wech end ???


    BackHandler {
        OABX.main?.finishAffinity()
    }

    var query by rememberSaveable {
        mutableStateOf(
            OABX.main?.viewModel?.searchQuery?.value ?: ""
        )
    }

    FullScreenBackground {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            sheetContent = {
                // bottom sheets are also recomposited when hidden
                if (scaffoldState.bottomSheetState.currentValue != SheetValue.Hidden) {
                    SortFilterSheet(
                        onDismiss = {
                            scope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))    // placeholder
                }
            }
        ) {
            val pagerState = rememberPagerState(pageCount = { pages.size })
            val currentPage by remember { derivedStateOf { pages[pagerState.currentPage] } }
            val openBlocklist = rememberSaveable { mutableStateOf(false) }
            val searchExpanded = remember {
                mutableStateOf(false)
            }

            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                topBar = {
                    Column {
                        TopBar(
                            title = stringResource(id = currentPage.title)
                        ) {
                            when (currentPage.destination) {
                                NavItem.Scheduler.destination -> {
                                    RoundButton(
                                        icon = Phosphor.Prohibit,
                                        description = stringResource(id = R.string.sched_blocklist)
                                    ) { openBlocklist.value = true }
                                    RoundButton(
                                        description = stringResource(id = R.string.prefs_title),
                                        icon = Phosphor.GearSix
                                    ) { navController.navigate(NavItem.Prefs.destination) }
                                }

                                else                          -> {
                                    ExpandableSearchAction(
                                        expanded = searchExpanded,
                                        query = query,
                                        onQueryChanged = { newQuery ->
                                            //if (newQuery != query)  // empty string doesn't work...
                                            query = newQuery
                                            OABX.main?.viewModel?.searchQuery?.value = query
                                        },
                                        onClose = {
                                            query = ""
                                            OABX.main?.viewModel?.searchQuery?.value = ""
                                        }
                                    )
                                    AnimatedVisibility(!searchExpanded.value) {
                                        RefreshButton { OABX.main?.refreshPackagesAndBackups() }
                                    }
                                    AnimatedVisibility(!searchExpanded.value) {
                                        RoundButton(
                                            description = stringResource(id = R.string.prefs_title),
                                            icon = Phosphor.GearSix
                                        ) { navController.navigate(NavItem.Prefs.destination) }
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(currentPage.destination != NavItem.Scheduler.destination) {
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
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    PagerNavBar(pageItems = pages, pagerState = pagerState)
                }
            ) { paddingValues ->
                SlidePager(
                    modifier = Modifier
                        .padding(paddingValues)
                        .blockBorder()
                        .fillMaxSize(),
                    pagerState = pagerState,
                    pageItems = pages,
                )
            }

            if (openBlocklist.value) BaseDialog(openDialogCustom = openBlocklist) {
                GlobalBlockListDialogUI(
                    currentBlocklist = OABX.main?.viewModel?.getBlocklist()?.toSet()
                        ?: emptySet(),
                    openDialogCustom = openBlocklist,
                ) { newSet ->
                    OABX.main?.viewModel?.setBlocklist(newSet)
                }
            }
        }
    }
}
