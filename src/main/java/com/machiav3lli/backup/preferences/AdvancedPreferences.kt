package com.machiav3lli.backup.preferences

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.machiav3lli.backup.BACKUP_DIRECTORY_INTENT
import com.machiav3lli.backup.COMPRESSION_TYPES
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.busyTick
import com.machiav3lli.backup.OABX.Companion.isDebug
import com.machiav3lli.backup.OABX.Companion.isHg42
import com.machiav3lli.backup.PREFS_LANGUAGES_SYSTEM
import com.machiav3lli.backup.R
import com.machiav3lli.backup.THEME_DYNAMIC
import com.machiav3lli.backup.THEME_SYSTEM
import com.machiav3lli.backup.accentColorItems
import com.machiav3lli.backup.handler.ShellHandler.Companion.findSuCommand
import com.machiav3lli.backup.handler.ShellHandler.Companion.isLikeRoot
import com.machiav3lli.backup.handler.ShellHandler.Companion.suCommand
import com.machiav3lli.backup.handler.ShellHandler.Companion.validateSuCommand
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.preferences.ui.PrefsGroup
import com.machiav3lli.backup.preferences.ui.PrefsGroupCollapsed
import com.machiav3lli.backup.preferences.ui.PrefsGroupHeading
import com.machiav3lli.backup.secondaryColorItems
import com.machiav3lli.backup.themeItems
import com.machiav3lli.backup.traceDebug
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.AndroidLogo
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowsOutLineVertical
import com.machiav3lli.backup.ui.compose.icons.phosphor.AsteriskSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.CalendarX
import com.machiav3lli.backup.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.backup.ui.compose.icons.phosphor.Clock
import com.machiav3lli.backup.ui.compose.icons.phosphor.ClockCounterClockwise
import com.machiav3lli.backup.ui.compose.icons.phosphor.EyedropperSample
import com.machiav3lli.backup.ui.compose.icons.phosphor.FileZip
import com.machiav3lli.backup.ui.compose.icons.phosphor.FingerprintSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.FloppyDisk
import com.machiav3lli.backup.ui.compose.icons.phosphor.FolderNotch
import com.machiav3lli.backup.ui.compose.icons.phosphor.GameController
import com.machiav3lli.backup.ui.compose.icons.phosphor.Hash
import com.machiav3lli.backup.ui.compose.icons.phosphor.Key
import com.machiav3lli.backup.ui.compose.icons.phosphor.List
import com.machiav3lli.backup.ui.compose.icons.phosphor.Lock
import com.machiav3lli.backup.ui.compose.icons.phosphor.Password
import com.machiav3lli.backup.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.backup.ui.compose.icons.phosphor.Prohibit
import com.machiav3lli.backup.ui.compose.icons.phosphor.ProhibitInset
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldCheckered
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.backup.ui.compose.icons.phosphor.Spinner
import com.machiav3lli.backup.ui.compose.icons.phosphor.Swatches
import com.machiav3lli.backup.ui.compose.icons.phosphor.TagSimple
import com.machiav3lli.backup.ui.compose.icons.phosphor.TextAa
import com.machiav3lli.backup.ui.compose.icons.phosphor.Textbox
import com.machiav3lli.backup.ui.compose.icons.phosphor.Translate
import com.machiav3lli.backup.ui.compose.item.BasePreference
import com.machiav3lli.backup.ui.compose.item.StringEditPreference
import com.machiav3lli.backup.ui.compose.item.TextInput
import com.machiav3lli.backup.ui.compose.mix
import com.machiav3lli.backup.ui.compose.recycler.InnerBackground
import com.machiav3lli.backup.ui.compose.theme.ColorAPK
import com.machiav3lli.backup.ui.compose.theme.ColorData
import com.machiav3lli.backup.ui.compose.theme.ColorDeData
import com.machiav3lli.backup.ui.compose.theme.ColorExodus
import com.machiav3lli.backup.ui.compose.theme.ColorExtDATA
import com.machiav3lli.backup.ui.compose.theme.ColorMedia
import com.machiav3lli.backup.ui.compose.theme.ColorOBB
import com.machiav3lli.backup.ui.compose.theme.ColorSpecial
import com.machiav3lli.backup.ui.compose.theme.ColorSystem
import com.machiav3lli.backup.ui.compose.theme.ColorUpdated
import com.machiav3lli.backup.ui.item.BooleanPref
import com.machiav3lli.backup.ui.item.EnumPref
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.ui.item.LaunchPref
import com.machiav3lli.backup.ui.item.ListPref
import com.machiav3lli.backup.ui.item.PasswordPref
import com.machiav3lli.backup.ui.item.Pref
import com.machiav3lli.backup.ui.item.PrefUI
import com.machiav3lli.backup.ui.item.StringEditPref
import com.machiav3lli.backup.ui.item.StringPref
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.SystemUtils.numCores
import com.machiav3lli.backup.utils.backupDirConfigured
import com.machiav3lli.backup.utils.backupFolderExists
import com.machiav3lli.backup.utils.getLanguageList
import com.machiav3lli.backup.utils.isBiometricLockAvailable
import com.machiav3lli.backup.utils.isDeviceLockAvailable
import com.machiav3lli.backup.utils.isDeviceLockEnabled
import com.machiav3lli.backup.utils.recreateActivities
import com.machiav3lli.backup.utils.restartApp
import com.machiav3lli.backup.utils.scheduleAlarms
import com.machiav3lli.backup.utils.setBackupDir
import com.machiav3lli.backup.utils.setCustomTheme
import timber.log.Timber


@Composable
fun DevPrefGroups() {
    val devFileOptions = Pref.prefGroups["dev-file"]?: listOf()
    val devLogOptions = Pref.prefGroups["dev-log"]?: listOf()
    val devTraceOptions = Pref.prefGroups["dev-trace"]?: listOf()
    val devHackOptions = Pref.prefGroups["dev-hack"]?: listOf()
    val devAltOptions = Pref.prefGroups["dev-alt"]?: listOf()
    val devNewOptions = Pref.prefGroups["dev-new"]?: listOf()
    val devFakeOptions = Pref.prefGroups["dev-fake"]?: listOf()

    PrefsGroupCollapsed(prefs = devAltOptions, heading = "alternative implementations/tests")
    PrefsGroupCollapsed(prefs = devLogOptions, heading = "logging")
    PrefsGroupCollapsed(prefs = devTraceOptions, heading = "tracing")
    PrefsGroupCollapsed(prefs = devFileOptions, heading = "file handling")
    PrefsGroupCollapsed(prefs = devHackOptions, heading = "workarounds (hacks)")
    PrefsGroupCollapsed(prefs = devFakeOptions, heading = "faking (for testing)")
    PrefsGroupCollapsed(prefs = devNewOptions, heading = "new experimental (for devs)")
}

@Composable
fun UserPrefGroups() {
    val userOptions = Pref.prefGroups["user"]?: listOf()
    val srvOptions = Pref.prefGroups["srv"]?: listOf()
    val srvBkpOptions = Pref.prefGroups["srv-bkp"]?: listOf()
    val srvRstOptions = Pref.prefGroups["srv-rst"]?: listOf()
    val advOptions = Pref.prefGroups["adv"]?: listOf()
    //val toolOptions = Pref.prefGroups["tool"]?: listOf()

    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        PrefsGroupCollapsed(prefs = userOptions, heading = "user")
        PrefsGroupCollapsed(prefs = srvOptions, heading = "service")
        PrefsGroupCollapsed(prefs = srvBkpOptions, heading = "backup")
        PrefsGroupCollapsed(prefs = srvRstOptions, heading = "restore")
        PrefsGroupCollapsed(prefs = advOptions, heading = "advanced")
        //PrefsGroupCollapsed(prefs = toolOptions, heading = "tools")
    }
}

@Composable
fun AdvancedPrefsPage() {
    val context = LocalContext.current
    val (expanded, expand) = remember { mutableStateOf(false) }

    val prefs = Pref.prefGroups["adv"]?: listOf()

    InnerBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                PrefsGroup(prefs = prefs)
            }
            item {
                PrefsGroupHeading(
                    heading = "Developer Settings",
                )
            }
            item {
                DevPrefGroups()
            }
        }
    }
}


val debug = if (isDebug)
    "dev"
else
    "debug"

val hg42 = if (isHg42)
    "dev"
else
    "hg42"


//-------------------------------------------------------------------------------------------------- adv
// developer settings - advanced users

@Composable
fun SuCommandPreference(
    modifier: Modifier = Modifier,
    pref: StringPref,
    dirty: Boolean = pref.dirty.value,
    index: Int = 0,
    groupSize: Int = 1,
) {
    //traceCompose { "SuCommandPreference: $pref" }
    BasePreference(
        modifier = modifier,
        pref = pref,
        dirty = dirty,
        titleId = pref.titleId,
        summaryId = pref.summaryId,
        summary = pref.summary,
        index = index,
        groupSize = groupSize,
        bottomWidget = {
            LaunchedEffect(true) {
                pref.onChanged?.invoke(pref)
            }
            if (pref.value != suCommand) {
                Text(
                    "=> $suCommand",
                    color = Color.Cyan.mix(MaterialTheme.colorScheme.onSurface),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            TextInput(
                pref.value,
                modifier = Modifier.fillMaxWidth(),
            ) {
                pref.value = it
            }
        },
    )
}

class SuCommandPref(
    key: String,
    private: Boolean = true,
    defaultValue: String,
    @StringRes titleId: Int = -1,
    @StringRes summaryId: Int = -1,
    summary: String? = null,
    UI: PrefUI? = null,
    icon: ImageVector? = null,
    iconTint: ((Pref) -> Color)? = null,
    enableIf: (() -> Boolean)? = null,
    onChanged: ((Pref) -> Unit)? = null,
) : StringPref(
    key = key,
    private = private,
    defaultValue = defaultValue,
    titleId = titleId,
    summaryId = summaryId,
    summary = summary,
    UI = UI ?: { pref, onDialogUI ->
        SuCommandPreference(pref = pref as SuCommandPref)
    },
    icon = icon,
    iconTint = iconTint,
    enableIf = enableIf,
    onChanged = onChanged
)

val suCommand_summary
    get() = """
        the command used to elevate the shell to a 'root' shell (in our sense),
        the whole command must be a shell, reading commands from stdin and executing them,
        there are also builtin fallback commands
        """.trimIndent().replace("\n", " ").trim()

val suCommand_default = "su -c 'nsenter --mount=/proc/1/ns/mnt sh'"

val pref_suCommand = SuCommandPref(
    key = "adv.suCommand",
    //TODO hg42 pref description is not shown currently for StringPrefs, because a hack uses it to show the value
    summary = suCommand_summary,
    icon = Phosphor.Hash,
    iconTint = {
        val pref = it as SuCommandPref
        if (isLikeRoot == true) {
            if (pref.value == suCommand)
                Color.Green
            else
                Color.Green.copy(alpha = 0.5f)      //TODO hg42 because here is not @Ccomposable
        } else {
            Color.Red
        }
    },
    defaultValue = "",
) {
    val pref = it as SuCommandPref
    var test = pref.value
    if (test == "")
        test = suCommand_default
    if (test != suCommand) {
        if (!validateSuCommand(test)) {
            findSuCommand()
            traceDebug { "findSuCommand: suCommand = $suCommand" }
        }
    }
    if (pref.value == "" && test.isNotEmpty()) {
        pref.value = test
    }
    pref.summary = suCommand_summary
    traceDebug { "summary: ${pref.summary}" }
    traceDebug { "pref: ${pref.dirty} ${pref.key} -> ${pref.icon?.name} ${pref.iconTint} (launch)" }
    pref.dirty.value = true
}

val pref_libsuUseRootShell = BooleanPref(
    key = "adv.libsuUseRootShell",
    summary = """
        start libsu shell as 'su' instead of 'sh' before suCommand elevates it
        (as a paranoid fallback, in case 'sh' might not allow elevating for an unknown reason)
        [needs restart]
        """.trimIndent().replace("\n", " ").trim(),
    defaultValue = true
)

val pref_libsuTimeout = IntPref(
    key = "adv.libsuTimeout",
    summary = "[seconds] timeout for libsu commands (does not affect the tar commands)",
    entries = ((10..90 step 10) + (100..300 step 50)).toList(),
    defaultValue = 60
)

val pref_maxJobs = IntPref(
    key = "adv.maxJobs",
    summary = "maximum number of jobs run concurrently (0 = default = numCores)[needs restart]",
    entries = (0..1 * numCores).toList(),
    defaultValue = 0
)

val pref_cancelJobsAtBoot = BooleanPref(
    key = "adv.cancelJobsAtBoot",
    summary = "cancel all remaining jobs on device boot",
    defaultValue = true
)

val pref_menuButtonAlwaysVisible = BooleanPref(
    key = "adv.menuButtonAlwaysVisible",
    summary = "also show context menu button when selection is empty",
    defaultValue = true
)

val pref_busyIconTurnTime = IntPref(
    key = "adv.busyIconTurnTime",
    summary = "time for one rotation of busy icon (ms)",
    entries = (1000..10000 step 500).toList(),
    defaultValue = 4000
)

val pref_busyIconScale = IntPref(
    key = "adv.busyIconScale",
    summary = "busy icon scaling (%)",
    entries = (100..200 step 10).toList(),
    defaultValue = 150
)

val pref_busyFadeTime = IntPref(
    key = "adv.busyFadeTime",
    summary = "time to fade busy color (ms)",
    entries = (0..5000 step 250).toList(),
    defaultValue = 2000
)

val pref_showInfoLogBar = BooleanPref(
    key = "adv.showInfoLogBar",
    summaryId = R.string.prefs_showinfologbar_summary,
    defaultValue = false
)

val pref_useAlarmClock = BooleanPref(
    key = "adv.useAlarmClock",
    summaryId = R.string.prefs_usealarmclock_summary,
    defaultValue = false
)

val pref_useExactAlarm = BooleanPref(
    key = "adv.useExactAlarm",
    summaryId = R.string.prefs_useexactalarm_summary,
    defaultValue = false
)

val pref_backupPauseApps = BooleanPref(
    key = "adv.backupPauseApps",
    summary = """
        pause apps during backups to avoid inconsistencies caused
        by ongoing file changes or other conflicts (doesn't seem to have big benefits)
        """.trimIndent().replace("\n", " ").trim(),
    defaultValue = false
)

val pref_backupSuspendApps = BooleanPref(
    key = "adv.backupSuspendApps",
    summary = "additionally use pm suspend command to pause apps (unfortunately not very useful, some disadvantages)",
    defaultValue = false,
    enableIf = { pref_backupPauseApps.value }
)

val pref_restoreKillApps = BooleanPref(
    key = "adv.restoreKillApps",
    summary = "kill apps before restores",
    defaultValue = true
)

val pref_strictHardLinks = BooleanPref(
    key = "adv.strictHardLinks",
    summaryId = R.string.prefs_stricthardlinks_summary,
    defaultValue = false
)

val pref_shareAsFile = BooleanPref(
    key = "adv.shareAsFile",
    summary = "share logs as file, otherwise as text",
    defaultValue = true
)

val pref_maxRetriesPerPackage = IntPref(
    key = "adv.maxRetriesPerPackage",
    summaryId = R.string.prefs_maxretriesperpackage_summary,
    entries = (0..10).toList(),
    defaultValue = 1
)

val pref_backupTarCmd = BooleanPref(
    key = "adv.backupTarCmd",
    summaryId = R.string.prefs_backuptarcmd_summary,
    defaultValue = true
)

val pref_restoreTarCmd = BooleanPref(
    key = "adv.restoreTarCmd",
    summaryId = R.string.prefs_restoretarcmd_summary,
    defaultValue = true
)


//-------------------------------------------------------------------------------------------------- dev-file
// developer settings - file handling

val pref_shadowRootFile = BooleanPref(
    key = "dev-file.shadowRootFile",
    summaryId = R.string.prefs_shadowrootfile_summary,
    defaultValue = false,
) {
    StorageFile.invalidateCache()
    pref_pathBackupFolder.value = pref_pathBackupFolder.value
}

val pref_cacheUris = BooleanPref(
    key = "dev-file.cacheUris",
    summaryId = R.string.prefs_cacheuris_summary,
    defaultValue = true
)

val pref_cacheFileLists = BooleanPref(
    key = "dev-file.cacheFileLists",
    summaryId = R.string.prefs_cachefilelists_summary,
    defaultValue = true
)


//-------------------------------------------------------------------------------------------------- dev-alt
// developer settings - alternatives

val pref_propsFromFilename = BooleanPref(
    key = "dev-alt.propsFromFilename",
    summary = "get backup properties from backup filename (which is incomplete but fast, good for remote)",
    defaultValue = false
)

val pref_preferencesOnOnePage = BooleanPref(
    key = "dev-alt.preferencesOnOnePage",
    summary = "use a single page for preferences",
    defaultValue = false
)

val pref_useNoteIcon = BooleanPref(
    key = "dev-alt.useNoteIcon",
    summary = """
        use icon instead of 'edit note' button and color note background
        to emphasize the note instead of the always existent edit button
        """.trimIndent().replace("\n", " ").trim(),
    defaultValue = true
)

val pref_paranoidBackupLists = BooleanPref(
    key = "dev-alt.paranoidBackupLists",
    summary = "verify file system after adding or deleting backups (slower, especially remote)",
    defaultValue = false
)

val pref_paranoidHousekeeping = BooleanPref(
    key = "dev-alt.paranoidHousekeeping",
    summary = "verify file system before housekeeping (slower, especially remote)",
    defaultValue = false
)

val pref_ignoreLockedInHousekeeping = BooleanPref(
    key = "dev-alt.ignoreLockedInHousekeeping",
    summary = "keep the configured number of unlocked backups, instead of also counting locked backups",
    defaultValue = true
)

val pref_fullScreenBackground = BooleanPref(
    key = "dev-alt.fullScreenBackground",
    summary = "extend background (laser, version) to fullscreen",
    defaultValue = true
)

val pref_restartAppOnLanguageChange = BooleanPref(
    key = "dev-alt.restartAppOnLanguageChange",
    summary = "restart app, if the language changes",
    defaultValue = false
)

val pref_prettyJson = BooleanPref(  //TODO hg42 to be removed
    key = "dev-alt.prettyJson",
    summary = "create human readable json files. Note: NB can read all variants",
    defaultValue = true
)

val pref_useYamlPreferences = BooleanPref(
    key = "$debug-alt.useYamlPreferences",
    summary = "create human readable yaml format for preferences. Note: NB can read all variants [do not use, experimental]",
    defaultValue = false
)

val pref_useYamlSchedules = BooleanPref(
    key = "$debug-alt.useYamlSchedules",
    summary = "create human readable yaml format for schedules. Note: NB can read all variants [do not use, experimental]",
    defaultValue = false
)

val pref_useYamlProperties = BooleanPref(
    key = "$debug-alt.useYamlProperties",
    summary = "create human readable yaml format for backup properties. Note: NB can read all variants [do not use, experimental]",
    defaultValue = false
)

val pref_busyTurnTime = IntPref(
    key = "dev-alt.busyTurnTime",
    summary = "time the animated busy bars need for one rotation (ms)",
    entries = (500..50000 step 500).toList(),
    defaultValue = 50000
)

val pref_versionOpacity = IntPref(
    key = "dev-alt.versionOpacity",
    summary = "opacity of version [percent]",
    entries = ((0..9 step 1) + (10..100 step 5)).toList(),
    defaultValue = 75
    // small values are invisible but can be seen with image processing
)

val pref_busyHitTime = IntPref(
    key = "dev-alt.busyHitTime",
    summary = "time being busy after hitting the watchdog (ms)",
    entries = (busyTick..4000 step busyTick).toList(),
    defaultValue = 2000
)

val pref_lookForEmptyBackups = BooleanPref(
    key = "dev-alt.lookForEmptyBackups",
    summary = "scan for empty backups (slower refresh, especially remote)",
    defaultValue = false
)

val pref_earlyEmptyBackups = BooleanPref(   //TODO hg42 to be removed
    key = "dev-alt.earlyEmptyBackups",
    summary = "empty backup lists for installed packages early, to prevent single scanning",
    defaultValue = true
)

val pref_flatStructure = BooleanPref(
    key = "dev-alt.flatStructure",
    summary = "use a flat directory structure (theoretically this should be faster, less directory reads)",
    defaultValue = false
)

val pref_propertiesInDir = BooleanPref(
    key = "$debug-alt.propertiesInDir",         //TODO hg42 currently not working in scanner (hmm, I think it works now)
    summary = "store the properties inside the backup directory",
    defaultValue = false
)

val pref_restoreAvoidTemporaryCopy = BooleanPref(
    key = "dev-alt.restoreAvoidTemporaryCopy",
    summaryId = R.string.prefs_restoreavoidtempcopy_summary,
    defaultValue = false
)

val pref_useWorkManagerForSingleManualJob = BooleanPref(
    key = "dev-alt.useWorkManagerForSingleManualJob",
    summary = "also queue single manual jobs from app sheet (note they are added at the end of the queue for now)",
    defaultValue = false
)

val pref_useForegroundInService = BooleanPref(
    key = "dev-alt.useForegroundInService",
    summary = "use foreground notification in service",
    defaultValue = true
)

val pref_useForegroundInJob = BooleanPref(
    key = "dev-alt.useForegroundInJob",
    summary = "sue foreground notification in each job (per package)",
    defaultValue = false
)

val pref_useExpedited = BooleanPref(
    key = "dev-alt.useExpedited",
    summaryId = R.string.prefs_useexpedited_summary,
    defaultValue = true
)


//-------------------------------------------------------------------------------------------------- dev-hack
// developer settings - workarounds

val pref_fixNavBarOverlap = IntPref( //TODO wech
    key = "dev-hack.fixNavBarOverlap",
    summary = "fix UI overlapping system navbars [in 'dp', usually needs something like 42]",
    entries = (0..64).toList(),
    defaultValue = if (OABX.minSDK(Build.VERSION_CODES.R)) 0 else 42
)

val pref_delayBeforeRefreshAppInfo = IntPref(
    key = "dev-hack.delayBeforeRefreshAppInfo",
    summaryId = R.string.prefs_delaybeforerefreshappinfo_summary,
    entries = (0..30).toList(),
    defaultValue = 0
)

val pref_refreshAppInfoTimeout = IntPref(
    key = "dev-hack.refreshAppInfoTimeout",
    summaryId = R.string.prefs_refreshappinfotimeout_summary,
    entries = ((0..9 step 1) + (10..120 step 10)).toList(),
    defaultValue = 30
)


//-------------------------------------------------------------------------------------------------- dev-fake
// developer settings - faking

val pref_killThisApp = LaunchPref(
    key = "dev-fake.killThisApp",
    summary = """
        terminate app, service and process, but leave the schedules(=alarms) intact
        (in contrast to force-close, where alarms are removed from the system)
        """.trimIndent().replace("\n", " ").trim(),
) {
    OABX.activity?.let { ActivityCompat.finishAffinity(it) }
    System.exit(0)
}

val pref_fakeBackupSeconds = IntPref(
    key = "dev-fake.fakeBackupSeconds",
    summary = "[seconds] time for faked backups, 0 = do not fake [for testing only]",
    entries = ((0..9 step 1) + (10..55 step 5) + (60..1200 step 60)).toList(),
    defaultValue = 0
)

val pref_fakeScheduleMin = IntPref(
    key = "dev-fake.fakeScheduleMin",
    summary = "[minutes] run enabled schedules every x min, using configured hours as seconds [for testing only]",
    entries = ((0..9 step 1) + (10..55 step 5) + (60..6*60 step 60)).toList(),
    defaultValue = 0
) {
    scheduleAlarms(scheduleNext = true)
}

val pref_forceCrash = LaunchPref(
    key = "dev-fake.forceCrash",
    summary = "crash the app [for testing only]"
) {
    throw Exception("forceCrash")
}


//-------------------------------------------------------------------------------------------------- adv
// advanced preferences

val pref_enableSpecialBackups = BooleanPref(
    key = "adv.enableSpecialBackups",
    titleId = R.string.prefs_enablespecial,
    summaryId = R.string.prefs_enablespecial_summary,
    icon = Phosphor.AsteriskSimple,
    iconTint = { ColorSpecial },
    defaultValue = true,
    onChanged = {
        // TODO hg42
        //NeoPrefs.getInstance().let {
        //    it.mainFilterHome.value = it.mainFilterHome.value and MAIN_FILTER_DEFAULT
        //}
    }
)

val pref_disableVerification = BooleanPref(
    key = "adv.disableVerification",
    titleId = R.string.prefs_disableverification,
    summaryId = R.string.prefs_disableverification_summary,
    icon = Phosphor.AndroidLogo,
    iconTint = { ColorUpdated },
    defaultValue = true
)

val pref_giveAllPermissions = BooleanPref(
    key = "adv.giveAllPermissions",
    titleId = R.string.prefs_restoreallpermissions,
    summaryId = R.string.prefs_restoreallpermissions_summary,
    icon = Phosphor.ShieldStar,
    iconTint = { ColorDeData },
    defaultValue = false
)

val pref_allowDowngrade = BooleanPref(
    key = "adv.allowDowngrade",
    titleId = R.string.prefs_allowdowngrade,
    summaryId = R.string.prefs_allowdowngrade_summary,
    icon = Phosphor.ClockCounterClockwise,
    defaultValue = false
)


//-------------------------------------------------------------------------------------------------- persist
// values that should persist for internal purposes (no UI)

val persist_firstLaunch = BooleanPref(
    key = "persist.firstLaunch",
    defaultValue = false
)

val persist_beenWelcomed = BooleanPref(
    key = "persist.beenWelcomed",
    defaultValue = false
)

val persist_ignoreBatteryOptimization = BooleanPref(
    key = "persist.ignoreBatteryOptimization",
    defaultValue = false
)

val persist_sortFilter = StringPref(
    key = "persist.sortFilter",
    defaultValue = ""
)

val persist_specialFilters = StringPref(
    key = "persist.specialFilters",
    defaultValue = ""
)

val persist_salt = StringPref(
    key = "persist.salt",
    defaultValue = ""
)

val persist_skippedEncryptionCounter = IntPref(
    key = "persist.skippedEncryptionCounter",
    entries = (0..100).toList(),
    defaultValue = 0
)

//-------------------------------------------------------------------------------------------------- persist
// persistent values, not shown in UI

fun publicPreferences(persist: Boolean = false) =
    Pref.prefGroups.flatMap {
        val (group, prefs) = it
        prefs.mapNotNull { pref ->
            if (pref.private ||
                pref is LaunchPref ||
                pref.group == "kill" ||
                (persist && pref.group == "persist")
            )
                null
            else
                pref
        }
    }

//-------------------------------------------------------------------------------------------------- srv
// service

@Composable
fun ServicePrefsPage() {

    InnerBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ServicePrefGroups()
            }
        }
    }
}

@Composable
fun ServicePrefGroups() {
    val generalServicePrefs = Pref.prefGroups["srv"]?: listOf()
    val backupServicePrefs = Pref.prefGroups["srv-bkp"]?: listOf()
    val restoreServicePrefs = Pref.prefGroups["srv-rst"]?: listOf()

    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        PrefsGroup(prefs = generalServicePrefs)
        PrefsGroup(prefs = backupServicePrefs)
        PrefsGroup(prefs = restoreServicePrefs)
    }
}


val pref_encryption = BooleanPref(
    key = "srv.encryption",
    titleId = R.string.prefs_encryption,
    summaryId = R.string.prefs_encryption_summary,
    icon = Phosphor.Key,
    iconTint = { ColorUpdated },
    defaultValue = false
)

val pref_password = PasswordPref(
    key = "srv.password",
    titleId = R.string.prefs_password,
    summaryId = R.string.prefs_password_summary,
    icon = Phosphor.Password,
    iconTint = {
        val pref = it as PasswordPref
        if (pref.value.isNotEmpty()) Color.Green else Color.Gray
    },
    defaultValue = "",
)

val kill_password = PasswordPref(   // make sure password is never saved in non-encrypted prefs
    key = "kill.password",
    private = false,
    defaultValue = ""
)

val kill_password_set = run { kill_password.value = "" }

val pref_backupDeviceProtectedData = BooleanPref(
    key = "srv-bkp.backupDeviceProtectedData",
    titleId = R.string.prefs_deviceprotecteddata,
    summaryId = R.string.prefs_deviceprotecteddata_summary,
    icon = Phosphor.ShieldCheckered,
    iconTint = { ColorDeData },
    defaultValue = true
)

val pref_backupExternalData = BooleanPref(
    key = "srv-bkp.backupExternalData",
    titleId = R.string.prefs_externaldata,
    summaryId = R.string.prefs_externaldata_summary,
    icon = Phosphor.FloppyDisk,
    iconTint = { ColorExtDATA },
    defaultValue = true
)

val pref_backupObbData = BooleanPref(
    key = "srv-bkp.backupObbData",
    titleId = R.string.prefs_obbdata,
    summaryId = R.string.prefs_obbdata_summary,
    icon = Phosphor.GameController,
    iconTint = { ColorOBB },
    defaultValue = true
)

val pref_backupMediaData = BooleanPref(
    key = "srv-bkp.backupMediaData",
    titleId = R.string.prefs_mediadata,
    summaryId = R.string.prefs_mediadata_summary,
    icon = Phosphor.PlayCircle,
    iconTint = { ColorMedia },
    defaultValue = true
)

val pref_backupNoBackupData = BooleanPref(
    key = "srv-bkp.backupNoBackupData",
    titleId = R.string.prefs_nobackupdata,
    summaryId = R.string.prefs_nobackupdata_summary,
    icon = Phosphor.ProhibitInset,
    iconTint = { ColorData },
    defaultValue = false,
    onChanged = { OABX.assets.updateExcludeFiles() },
)

val pref_backupCache = BooleanPref(
    key = "srv-bkp.backupCache",
    titleId = R.string.prefs_backupcache,
    summaryId = R.string.prefs_backupcache_summary,
    icon = Phosphor.Prohibit,
    defaultValue = false
)

val pref_restoreDeviceProtectedData = BooleanPref(
    key = "srv-rst.restoreDeviceProtectedData",
    titleId = R.string.prefs_deviceprotecteddata_rst,
    summaryId = R.string.prefs_deviceprotecteddata_rst_summary,
    icon = Phosphor.ShieldCheckered,
    iconTint = { ColorDeData },
    defaultValue = true
)

val pref_restoreExternalData = BooleanPref(
    key = "srv-rst.restoreExternalData",
    titleId = R.string.prefs_externaldata_rst,
    summaryId = R.string.prefs_externaldata_rst_summary,
    icon = Phosphor.FloppyDisk,
    iconTint = { ColorExtDATA },
    defaultValue = true
)

val pref_restoreObbData = BooleanPref(
    key = "srv-rst.restoreObbData",
    titleId = R.string.prefs_obbdata_rst,
    summaryId = R.string.prefs_obbdata_rst_summary,
    icon = Phosphor.GameController,
    iconTint = { ColorOBB },
    defaultValue = true
)

val pref_restoreMediaData = BooleanPref(
    key = "srv-rst.restoreMediaData",
    titleId = R.string.prefs_mediadata_rst,
    summaryId = R.string.prefs_mediadata_rst_summary,
    icon = Phosphor.PlayCircle,
    iconTint = { ColorMedia },
    defaultValue = true
)

val pref_restoreNoBackupData = BooleanPref(
    key = "srv-rst.restoreNoBackupData",
    titleId = R.string.prefs_nobackupdata_rst,
    summaryId = R.string.prefs_nobackupdata_rst_summary,
    icon = Phosphor.ProhibitInset,
    iconTint = { ColorData },
    defaultValue = false,
    onChanged = { OABX.assets.updateExcludeFiles() },
)

val pref_restoreCache = BooleanPref(
    key = "srv-rst.restoreCache",
    titleId = R.string.prefs_restorecache,
    summaryId = R.string.prefs_restorecache_summary,
    icon = Phosphor.Prohibit,
    defaultValue = false
)

val pref_restorePermissions = BooleanPref(
    key = "srv.restorePermissions",
    titleId = R.string.prefs_restorepermissions,
    summaryId = R.string.prefs_restorepermissions_summary,
    icon = Phosphor.ShieldStar,
    iconTint = { ColorAPK },
    defaultValue = true
)

val pref_numBackupRevisions = IntPref(
    key = "srv.numBackupRevisions",
    titleId = R.string.prefs_numBackupRevisions,
    summaryId = R.string.prefs_numBackupRevisions_summary,
    icon = Phosphor.Hash,
    iconTint = { ColorSpecial },
    entries = ((0..9) + (10..20 step 2) + (50..200 step 50)).toList(),
    defaultValue = 2
)

val pref_compressionType = ListPref(
    key = "srv.compressionType",
    titleId = R.string.prefs_compression_type,
    summaryId = R.string.prefs_compression_type_summary,
    icon = Phosphor.FileZip,
    iconTint = { ColorExodus },
    entries = COMPRESSION_TYPES,
    defaultValue = "zst"
)

val pref_compressionLevel = IntPref(
    key = "srv.compressionLevel",
    titleId = R.string.prefs_compression_level,
    summaryId = R.string.prefs_compression_level_summary,
    icon = Phosphor.FileZip,
    iconTint = { ColorExodus },
    entries = (0..9).toList(),
    defaultValue = 2
)

val pref_enableSessionInstaller = BooleanPref(
    key = "srv.enableSessionInstaller",
    titleId = R.string.prefs_sessionIinstaller,
    summaryId = R.string.prefs_sessionIinstaller_summary,
    icon = Phosphor.TagSimple,
    defaultValue = true
)

val pref_installationPackage = StringPref(
    key = "srv.installationPackage",
    titleId = R.string.prefs_installerpackagename,
    icon = Phosphor.Textbox,
    iconTint = { ColorOBB },
    defaultValue = OABX.packageName
)

//-------------------------------------------------------------------------------------------------- user
// user


@Composable
fun UserPrefsPage() {

    val prefs = Pref.prefGroups["user"]?: listOf()

    InnerBackground(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PrefsGroup(prefs = prefs)
            }
        }
    }
}

fun onThemeChanged(pref: Pref) {
    OABX.context.setCustomTheme()
    OABX.context.recreateActivities()
}


val pref_languages = ListPref(
    key = "user.languages",
    titleId = R.string.prefs_languages,
    icon = Phosphor.Translate,
    iconTint = { ColorOBB },
    entries = OABX.context.getLanguageList(),
    defaultValue = PREFS_LANGUAGES_SYSTEM,
    onChanged = {
        val pref = it as ListPref
        // does not work as expected, because restartApp doesn't really restart the whole app
        //if (pref.value == PREFS_LANGUAGES_SYSTEM)
        if (pref_restartAppOnLanguageChange.value)
            OABX.context.restartApp()   // does not really restart the app, only recreates
        else
            OABX.context.recreateActivities()
    },
)

val pref_appTheme = EnumPref(
    key = "user.appTheme",
    titleId = R.string.prefs_theme,
    icon = Phosphor.Swatches,
    iconTint = { ColorSpecial },
    entries = themeItems,
    defaultValue = if (OABX.minSDK(31)) THEME_DYNAMIC
    else THEME_SYSTEM,
    onChanged = ::onThemeChanged,
)

val pref_appAccentColor = EnumPref(
    key = ".appAccentColor", //TODO restore in future
    titleId = R.string.prefs_accent_color,
    icon = Phosphor.EyedropperSample,
    //iconTint = { MaterialTheme.colorScheme.primary },
    entries = accentColorItems,
    defaultValue = with(OABX.packageName) {
        when {
            contains("hg42")  -> 8
            contains("debug") -> 4
            else              -> 0
        }
    },
    onChanged = ::onThemeChanged,
)

val pref_appSecondaryColor = EnumPref(
    key = ".appSecondaryColor", //TODO restore in future
    titleId = R.string.prefs_secondary_color,
    icon = Phosphor.EyedropperSample,
    //iconTint = { MaterialTheme.colorScheme.secondary },
    entries = secondaryColorItems,
    defaultValue = with(OABX.packageName) {
        when {
            contains(".rel")  -> 0
            contains("debug") -> 4
            else              -> 3
        }
    },
    onChanged = ::onThemeChanged,
)

val pref_pathBackupFolder = StringEditPref(
    key = "user.pathBackupFolder",
    titleId = R.string.prefs_pathbackupfolder,
    icon = Phosphor.FolderNotch,
    iconTint = {
        val pref = it as StringEditPref
        val alpha = if (pref.value == runCatching { backupDirConfigured }.getOrNull()) 1f else 0.3f
        if (pref.value.isEmpty()) Color.Gray
        else if (backupFolderExists(pref.value)) Color.Green.copy(alpha = alpha)
        else Color.Red.copy(alpha = alpha)
    },
    UI = { it, onDialogUI ->
        val pref = it as StringEditPref
        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.data != null && result.resultCode == Activity.RESULT_OK) {
                    result.data?.let {
                        val uri = it.data ?: return@let
                        val oldDir = try {
                            backupDirConfigured
                        } catch (e: StorageLocationNotConfiguredException) {
                            "" // Can be ignored, this is about to set the path
                        }
                        if (oldDir != uri.toString()) {
                            val flags = it.flags and (
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    )
                            //TODO hg42 check and remember if flags are read only and implement appropriate actions elsewhere
                            OABX.context.contentResolver.takePersistableUriPermission(uri, flags)
                            Timber.i("setting uri $uri")
                            setBackupDir(uri)
                        }
                    }
                }
            }
        val onClick = {
            launcher.launch(BACKUP_DIRECTORY_INTENT)
        }
        StringEditPreference(
            pref = pref,
            onClick = onClick,
        )
    },
    defaultValue = "",
) {
    val pref = it as StringPref
    if (pref.value != "") {
        setBackupDir(Uri.parse(pref.value))
    }
}

val pref_deviceLock = BooleanPref(
    key = "user.deviceLock",
    titleId = R.string.prefs_devicelock,
    summaryId = R.string.prefs_devicelock_summary,
    icon = Phosphor.Lock,
    iconTint = { ColorUpdated },
    defaultValue = false,
    enableIf = { OABX.context.isDeviceLockAvailable() }
)

val pref_biometricLock = BooleanPref(
    key = "user.biometricLock",
    titleId = R.string.prefs_biometriclock,
    summaryId = R.string.prefs_biometriclock_summary,
    icon = Phosphor.FingerprintSimple,
    iconTint = { ColorDeData },
    defaultValue = false,
    enableIf = { OABX.context.isBiometricLockAvailable() && isDeviceLockEnabled() }
)

val pref_multilineInfoChips = BooleanPref(
    key = "user.multilineInfoChips",
    titleId = R.string.prefs_multilineinfochips,
    summaryId = R.string.prefs_multilineinfochips_summary,
    icon = Phosphor.ArrowsOutLineVertical,
    iconTint = { ColorSystem },
    defaultValue = true
)

val pref_singularBackupRestore = BooleanPref(
    key = "user.singularBackupRestore",
    titleId = R.string.prefs_singularbackuprestore,
    summaryId = R.string.prefs_singularbackuprestore_summary,
    icon = Phosphor.List,
    iconTint = { ColorSpecial },
    defaultValue = true
)

val pref_newAndUpdatedNotification = BooleanPref(
    key = "user.newAndUppdatedNotification",
    titleId = R.string.prefs_newandupdatednotification,
    summaryId = R.string.prefs_newandupdatednotification_summary,
    icon = Phosphor.CircleWavyWarning,
    defaultValue = false
)

val pref_squeezeNavText = BooleanPref(
    key = "user.squeezeNavText",
    titleId = R.string.prefs_squeezenavtext,
    summaryId = R.string.prefs_squeezenavtext_summary,
    icon = Phosphor.TextAa,
    iconTint = { ColorOBB },
    defaultValue = true
)

val pref_altNavBarItem = BooleanPref(
    key = "user.altNavBarItem",
    titleId = R.string.prefs_altnavbaritem,
    summaryId = R.string.prefs_altnavbaritem_summary,
    icon = Phosphor.TagSimple,
    defaultValue = true
)

val pref_altBackupDate = BooleanPref(
    key = "user.altBackupDate",
    titleId = R.string.prefs_altbackupdate,
    summaryId = R.string.prefs_altbackupdate_summary,
    icon = Phosphor.CalendarX,
    defaultValue = false
)

val pref_altBlockLayout = BooleanPref(
    key = "user.altBlockLayout",
    titleId = R.string.prefs_altblocklayout,
    summaryId = R.string.prefs_altblocklayout_summary,
    icon = Phosphor.Swatches,
    defaultValue = false
)

val pref_busyLaserBackground = BooleanPref(
    key = "user.busyLaserBackground",
    titleId = R.string.prefs_laserbackground,
    summaryId = R.string.prefs_laserbackground_summary,
    icon = Phosphor.Spinner,
    defaultValue = true
)

val pref_oldBackups = IntPref(
    key = "user.oldBackups",
    titleId = R.string.prefs_oldbackups,
    summaryId = R.string.prefs_oldbackups_summary,
    icon = Phosphor.Clock,
    iconTint = { ColorExodus },
    entries = (1..30).toList(),
    defaultValue = 2
)