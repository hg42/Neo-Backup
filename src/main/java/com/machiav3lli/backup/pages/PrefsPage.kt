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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.machiav3lli.backup.R
import com.machiav3lli.backup.sheets.HelpSheet
import com.machiav3lli.backup.ui.compose.blockBorderBottom
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.Info
import com.machiav3lli.backup.ui.compose.item.RoundButton
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.compose.recycler.FullScreenBackground
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.ui.navigation.NeoNavigationSuiteScaffold
import com.machiav3lli.backup.ui.navigation.SlidePager
import com.topjohnwu.superuser.Shell
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrefsPage(
    navController: NavHostController,
    pageIndex: Int = 0,
) {
    val scope = rememberCoroutineScope()
    val pages = persistentListOf(
        NavItem.UserPrefs,
        NavItem.ServicePrefs,
        NavItem.AdvancedPrefs,
        NavItem.ToolsPrefs,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPage by remember { derivedStateOf { pages[pagerState.currentPage] } }
    val scaffoldState = rememberBottomSheetScaffoldState()

    Shell.getShell()

    val onDismiss: () -> Unit = {
        scope.launch {
            scaffoldState.bottomSheetState.partialExpand()
        }
    }

    BackHandler {
        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) onDismiss()
        else navController.navigateUp()
    }

    FullScreenBackground {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            sheetContent = {
                HelpSheet(onDismiss)
            }
        ) {
            NeoNavigationSuiteScaffold(
                pages = pages,
                selectedPage = currentPage,
                onItemClick = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    topBar = {
                        TopBar(title = stringResource(id = currentPage.title)) {
                            RoundButton(
                                icon = Phosphor.Info,
                                description = stringResource(id = R.string.help),
                            ) {
                                scope.launch {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }
                        }
                    },
                ) { paddingValues ->
                    SlidePager(
                        modifier = Modifier
                            .padding(paddingValues)
                            .blockBorderBottom(),
                        pagerState = pagerState,
                        pageItems = pages,
                    )
                }
            }
        }
    }
}
