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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.GlobalBlockListDialogUI
import com.machiav3lli.backup.ui.compose.blockBorderBottom
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.item.MainTopBar
import com.machiav3lli.backup.ui.compose.item.RefreshButton
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.recycler.FullScreenBackground
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.ui.navigation.NeoNavigationSuiteScaffold
import com.machiav3lli.backup.ui.navigation.SlidePager
import com.machiav3lli.backup.viewmodels.MainVM
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainPage(
    navController: NavHostController,
    viewModel: MainVM = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val pages = persistentListOf(
        NavItem.Home,
        NavItem.Backup,
        NavItem.Restore,
        NavItem.Scheduler,
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPage by remember { derivedStateOf { pages[pagerState.currentPage] } }

    BackHandler {
        OABX.main?.finishAffinity()
    }

    val mainState by viewModel.homeState.collectAsState()

    FullScreenBackground {
        NeoNavigationSuiteScaffold(
            pages = pages,
            selectedPage = currentPage,
            onItemClick = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        ) {
            val openBlocklist = rememberSaveable { mutableStateOf(false) }
            val searchExpanded = remember {
                mutableStateOf(false)
            }

            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                topBar = {
                    Column {
                        MainTopBar(
                            title = stringResource(id = currentPage.title),
                            expanded = searchExpanded,
                            query = mainState.searchQuery,
                            onQueryChanged = { newQuery ->
                                viewModel.setSearchQuery(newQuery)
                            },
                            onClose = {
                                viewModel.setSearchQuery("")
                            }
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
                                    RoundButton(
                                        icon = Phosphor.MagnifyingGlass,
                                        description = stringResource(id = R.string.search),
                                        onClick = { searchExpanded.value = true }
                                    )
                                    RefreshButton { OABX.main?.refreshPackagesAndBackups() }
                                    RoundButton(
                                        description = stringResource(id = R.string.prefs_title),
                                        icon = Phosphor.GearSix
                                    ) { navController.navigate(NavItem.Prefs.destination) }
                                }
                            }
                        }
                    }
                },
            ) { paddingValues ->
                SlidePager(
                    modifier = Modifier
                        .padding(paddingValues)
                        .blockBorderBottom()
                        .fillMaxSize(),
                    pagerState = pagerState,
                    pageItems = pages,
                )
            }

            if (openBlocklist.value) BaseDialog(onDismiss = { openBlocklist.value = false }) {
                GlobalBlockListDialogUI(
                    currentBlocklist = mainState.blocklist,
                    openDialogCustom = openBlocklist,
                ) { newSet ->
                    viewModel.updateBlocklist(newSet)
                }
            }
        }
    }
}
