package com.machiav3lli.backup.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.isDebug
import com.machiav3lli.backup.OABX.Companion.isNeo
import com.machiav3lli.backup.R
import com.machiav3lli.backup.pages.BatchPage
import com.machiav3lli.backup.pages.HomePage
import com.machiav3lli.backup.pages.SchedulesPage
import com.machiav3lli.backup.preferences.AdvancedPrefsPage
import com.machiav3lli.backup.preferences.ServicePrefsPage
import com.machiav3lli.backup.preferences.ToolsPrefsPage
import com.machiav3lli.backup.preferences.UserPrefsPage
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArchiveTray
import com.machiav3lli.backup.ui.compose.icons.phosphor.Bug
import com.machiav3lli.backup.ui.compose.icons.phosphor.CalendarX
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.Detective
import com.machiav3lli.backup.ui.compose.icons.phosphor.Flask
import com.machiav3lli.backup.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.backup.ui.compose.icons.phosphor.House
import com.machiav3lli.backup.ui.compose.icons.phosphor.Infinity
import com.machiav3lli.backup.ui.compose.icons.phosphor.Lock
import com.machiav3lli.backup.ui.compose.icons.phosphor.SlidersHorizontal
import com.machiav3lli.backup.ui.compose.icons.phosphor.UserGear
import com.machiav3lli.backup.ui.compose.icons.phosphor.Warning
import com.machiav3lli.backup.ui.compose.icons.phosphor.Wrench

sealed class NavItem(
    val title: Int,
    val icon: ImageVector,
    val destination: String,
    val content: @Composable () -> Unit = {}
) {

    data object Welcome :
        NavItem(R.string.welcome_to_oabx, Phosphor.House, "intro_welcome")

    data object Permissions :
        NavItem(R.string.permission_not_granted, Phosphor.Warning, "intro_permissions")

    data object Lock :
        NavItem(R.string.prefs_devicelock, Phosphor.Lock, "intro_lock")

    data object Home :
        NavItem(
            R.string.home,
            when {
                isNeo   -> Phosphor.Infinity
                isDebug -> Phosphor.Detective
                //isDebug -> Phosphor.Bug
                //isHg42  -> Phosphor.Detective
                else    -> Phosphor.House
            },
            "home",
            { HomePage() }
        )

    data object Backup :
        NavItem(R.string.backup, Phosphor.ArchiveTray, "batch_backup", {
            OABX.main?.backupViewModel?.let {
                BatchPage(viewModel = it, backupBoolean = true)
            }
        })

    data object Restore :
        NavItem(R.string.restore, Phosphor.ClockCounterClockwise, "batch_restore", {
            OABX.main?.restoreViewModel?.let {
                BatchPage(viewModel = it, backupBoolean = false)
            }
        })

    data object Scheduler :
        NavItem(R.string.sched_title, Phosphor.CalendarX, "scheduler", {
            OABX.main?.schedulesViewModel?.let { viewModel ->
                SchedulesPage(viewModel)
            }
        })

    data object Main :
        NavItem(R.string.main, Phosphor.House, "main")

    data object Prefs :
        NavItem(R.string.prefs_title, Phosphor.GearSix, "settings")

    data object UserPrefs :
        NavItem(R.string.prefs_user_short, Phosphor.UserGear, "prefs_user", {
            UserPrefsPage()
        })

    data object ServicePrefs :
        NavItem(R.string.prefs_service_short, Phosphor.SlidersHorizontal, "prefs_service",{
            ServicePrefsPage()
        })

    data object AdvancedPrefs :
        NavItem(R.string.prefs_advanced_short, Phosphor.Flask, "prefs_advanced",{
            AdvancedPrefsPage()
        })

    data object ToolsPrefs : NavItem(R.string.prefs_tools_short, Phosphor.Wrench, "prefs_tools", {
            ToolsPrefsPage()
        })

    data object Terminal :
        NavItem(R.string.prefs_tools_terminal, Phosphor.Bug, "prefs_tools/terminal")

    data object Exports : NavItem(
        R.string.prefs_schedulesexportimport,
        Phosphor.CalendarX,
        "prefs_tools/exports"
    )

    data object Logs : NavItem(
        R.string.prefs_logviewer,
        Phosphor.Bug,
        "prefs_tools/logs"
    )
}
