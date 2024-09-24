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
package com.machiav3lli.backup.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.machiav3lli.backup.ALT_MODE_APK
import com.machiav3lli.backup.ALT_MODE_BOTH
import com.machiav3lli.backup.ALT_MODE_DATA
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.OABX.Companion.addInfoLogText
import com.machiav3lli.backup.OABX.Companion.startup
import com.machiav3lli.backup.R
import com.machiav3lli.backup.RESCUE_NAV
import com.machiav3lli.backup.dialogs.ActionsDialogUI
import com.machiav3lli.backup.dialogs.BaseDialog
import com.machiav3lli.backup.dialogs.DialogKey
import com.machiav3lli.backup.dialogs.GlobalBlockListDialogUI
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.LogsHandler.Companion.unexpectedException
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.handler.findBackups
import com.machiav3lli.backup.handler.updateAppTables
import com.machiav3lli.backup.pages.RootMissing
import com.machiav3lli.backup.pages.SplashPage
import com.machiav3lli.backup.preferences.persist_beenWelcomed
import com.machiav3lli.backup.preferences.persist_skippedEncryptionCounter
import com.machiav3lli.backup.preferences.pref_appTheme
import com.machiav3lli.backup.tasks.AppActionWork
import com.machiav3lli.backup.ui.compose.ObservedEffect
import com.machiav3lli.backup.ui.compose.item.DevTools
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.ui.navigation.MainNavHost
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.ui.navigation.clearBackStack
import com.machiav3lli.backup.ui.navigation.safeNavigate
import com.machiav3lli.backup.utils.FileUtils.invalidateBackupLocation
import com.machiav3lli.backup.utils.SystemUtils
import com.machiav3lli.backup.utils.TraceUtils.classAndId
import com.machiav3lli.backup.utils.TraceUtils.traceBold
import com.machiav3lli.backup.utils.allPermissionsGranted
import com.machiav3lli.backup.utils.altModeToMode
import com.machiav3lli.backup.utils.isBiometricLockAvailable
import com.machiav3lli.backup.utils.isBiometricLockEnabled
import com.machiav3lli.backup.utils.isDarkTheme
import com.machiav3lli.backup.utils.isDeviceLockEnabled
import com.machiav3lli.backup.utils.isEncryptionEnabled
import com.machiav3lli.backup.utils.isLikeRoot
import com.machiav3lli.backup.viewmodels.BatchViewModel
import com.machiav3lli.backup.viewmodels.ExportsViewModel
import com.machiav3lli.backup.viewmodels.LogViewModel
import com.machiav3lli.backup.viewmodels.MainViewModel
import com.machiav3lli.backup.viewmodels.SchedulerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber


@Composable
fun Rescue() {
    AppTheme {
        val expanded = remember { mutableStateOf(true) }
        DevTools(expanded = expanded)
    }
}

class MainActivityX : BaseActivity() {

    private val mScope: CoroutineScope = MainScope()
    lateinit var navController: NavHostController
    private lateinit var powerManager: PowerManager

    private lateinit var openDialog: MutableState<Boolean>
    private lateinit var dialogKey: MutableState<DialogKey?>

    val viewModel by viewModels<MainViewModel> {
        MainViewModel.Factory(OABX.db, application)
    }
    val backupViewModel: BatchViewModel by viewModels {
        BatchViewModel.Factory(application)
    }
    val restoreViewModel: BatchViewModel by viewModels {
        BatchViewModel.Factory(application)
    }
    val schedulerViewModel: SchedulerViewModel by viewModels {
        SchedulerViewModel.Factory(OABX.db.getScheduleDao(), application)
    }
    val exportsViewModel: ExportsViewModel by viewModels {
        ExportsViewModel.Factory(OABX.db.getScheduleDao(), application)
    }
    val logsViewModel: LogViewModel by viewModels {
        LogViewModel.Factory(application)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {

        val mainChanged = (this != OABX.mainSaved)
        OABX.main = this

        var freshStart = (savedInstanceState == null)   //TODO use some lifecycle method?

        Timber.w(
            listOfNotNull(
                if (freshStart) "fresh start" else null,
                if (mainChanged && (!freshStart || (OABX.mainSaved != null)))
                    "main changed (was ${classAndId(OABX.mainSaved)})"
                else
                    null,
            ).joinToString(", ")
        )

        super.onCreate(savedInstanceState)

        Timber.d(
            "viewModel: ${
                classAndId(viewModel)
            }, was ${
                classAndId(OABX.viewModelSaved)
            }"
        )

        //TODO wech begin ??? or is this necessary with resume or similar?

        //TODO here or in MainPage? MainPage seems to be weird at least for each recomposition
        OABX.appsSuspendedChecked = false

        //if (pref_catchUncaughtException.value) {               //TODO wech ???
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

        //TODO wech Shell.getShell() // should be handled in ShellHandler

        //TODO wech end ???

        setContent {
            AppTheme {
                SplashPage()
            }
            navController = rememberNavController()
        }

        if (doIntent(intent, "beforeContent"))
            return

        if (!isLikeRoot()) {
            setContent {
                AppTheme {
                    RootMissing(this)
                }
            }
            return
        }

        powerManager = this.getSystemService(POWER_SERVICE) as PowerManager

        setContent {

            navController = rememberNavController()

            DisposableEffect(pref_appTheme.value) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDarkTheme },
                )
                onDispose {}
            }

            AppTheme {
                openDialog = remember { mutableStateOf(false) }
                dialogKey = remember { mutableStateOf(null) }
                val openBlocklist = remember { mutableStateOf(false) }

                LaunchedEffect(viewModel) {
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        if (destination.route == NavItem.Main.destination && freshStart) {
                            freshStart = false
                            traceBold { "******************** freshStart && Main ********************" }
                            mScope.launch(Dispatchers.IO) {
                                runCatching { findBackups() }
                                startup =
                                    false     // ensure backups are no more reported as empty
                                runCatching { updateAppTables() }
                                //TODO hg42 val time = OABX.endBusy(OABX.startupMsg)
                                //TODO hg42 addInfoLogText("startup: ${"%.3f".format(time / 1E9)} sec")
                            }
                            runOnUiThread { showEncryptionDialog() }
                        }
                    }
                }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    ObservedEffect {
                        resumeMain()
                    }

                    Box {
                        MainNavHost(
                            navController = navController,
                        )

                        if (openBlocklist.value) BaseDialog(openDialogCustom = openBlocklist) {
                            GlobalBlockListDialogUI(
                                currentBlocklist = viewModel.getBlocklist().toSet(),
                                openDialogCustom = openBlocklist,
                            ) { newSet ->
                                viewModel.setBlocklist(newSet)
                            }
                        }
                    }
                }

                if (openDialog.value) {
                    BaseDialog(openDialogCustom = openDialog) {
                        when (dialogKey.value) {
                            is DialogKey.Encryption -> {
                                ActionsDialogUI(
                                    titleText = stringResource(id = R.string.enable_encryption_title),
                                    messageText = stringResource(id = R.string.enable_encryption_message),
                                    openDialogCustom = openDialog,
                                    primaryText = stringResource(id = R.string.dialog_approve),
                                    primaryAction = {
                                        openDialog.value = false
                                        moveTo("${NavItem.Prefs.destination}?page=1")
                                    }
                                )
                            }

                            is DialogKey.Error      -> {
                                val message = (dialogKey.value as DialogKey.Error).message
                                ActionsDialogUI(
                                    titleText = stringResource(id = R.string.errorDialogTitle),
                                    messageText = message,
                                    openDialogCustom = openDialog,
                                    primaryText = stringResource(id = R.string.dialogSave),
                                    primaryAction = { LogsHandler.logErrors(message) },
                                    secondaryText = stringResource(id = R.string.dialogOK)
                                )
                            }

                            else                    -> {}
                        }
                    }
                }
            }

            LaunchedEffect(true) {
                withTimeoutOrNull(5000) {
                    while (navController.graph.nodes.size() < 2)
                        delay(100)
                }
                doIntent(intent, "afterContent")
            }
        }
    }

    override fun onResume() {
        OABX.main = this
        super.onResume()
    }

    override fun onDestroy() {
        OABX.viewModelSaved = viewModel
        OABX.mainSaved = OABX.main
        OABX.main = null
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        doIntent(intent, "newIntent")
        super.onNewIntent(intent)
    }

    private fun doIntent(intent: Intent?, at: String): Boolean {
        if (intent == null) return false
        val command = intent.action
        val data = intent.data
        Timber.i("Main: command $command -> $data")
        when (at) {

            "beforeContent"             -> {
                when (command) {
                    null                         -> {}

                    "android.intent.action.MAIN" -> {
                        if (data == null) return false
                        when (data.toString()) {
                            RESCUE_NAV -> {
                                setContent {
                                    Rescue()
                                }
                                return true
                            }
                        }
                    }

                    else                         -> {}
                }
            }

            "afterContent", "newIntent" -> {
                when (command) {
                    null                         -> {}
                    "android.intent.action.MAIN" -> {
                        if (data == null) return false
                        moveTo(data.toString())
                    }

                    else                         -> {
                        addInfoLogText("Main: command '$command'")
                    }
                }
            }

        }
        return false
    }

    private fun showEncryptionDialog() {
        val dontShowAgain = isEncryptionEnabled()
        if (dontShowAgain) return
        val dontShowCounter = persist_skippedEncryptionCounter.value
        if (dontShowCounter > 30) return    // don't increment further (useless touching file)
        persist_skippedEncryptionCounter.value = dontShowCounter + 1
        if (dontShowCounter % 10 == 0) {
            showDialog(DialogKey.Encryption())
        }
    }

    fun updatePackage(packageName: String) {
        viewModel.updatePackage(packageName)
    }

    fun refreshPackagesAndBackups() {
        CoroutineScope(Dispatchers.IO).launch {
            invalidateBackupLocation()
        }
    }

    fun showSnackBar(message: String) { // TODO reimplement this?
    }

    fun dismissSnackBar() {
    }

    fun showDialog(key: DialogKey) {
        dialogKey.value = key
        openDialog.value = true
    }

    fun showError(message: String?) {
        message?.let {
            showDialog(DialogKey.Error(it))
        }
    }

    fun moveTo(destination: String) {
        try {
            persist_beenWelcomed.value = destination != NavItem.Welcome.destination
            navController.navigate(destination)
        } catch (e: IllegalArgumentException) {
            Timber.e("cannot navigate to '$destination'")
        } catch (e: Throwable) {
            unexpectedException(e)
        }
    }

    fun whileShowingSnackBar(message: String, todo: () -> Unit) {
        runOnUiThread {
            showSnackBar(message)
        }
        todo()
        runOnUiThread {
            dismissSnackBar()
        }
    }

    fun startBatchAction(
        backupBoolean: Boolean,
        selectedPackageNames: List<String?>,
        selectedModes: List<Int>,
    ) {
        val now = SystemUtils.now
        val notificationId = now.toInt()
        val batchType = getString(if (backupBoolean) R.string.backup else R.string.restore)
        val batchName = WorkHandler.getBatchName(batchType, now)

        val selectedItems = selectedPackageNames
            .mapIndexed { i, packageName ->
                if (packageName.isNullOrEmpty()) null
                else Pair(packageName, selectedModes[i])
            }
            .filterNotNull()

        var errors = ""
        var resultsSuccess = true
        var counter = 0
        val worksList: MutableList<OneTimeWorkRequest> = mutableListOf()
        OABX.work.beginBatch(batchName)
        selectedItems.forEach { (packageName, mode) ->

            val oneTimeWorkRequest =
                AppActionWork.Request(
                    packageName = packageName,
                    mode = mode,
                    backupBoolean = backupBoolean,
                    notificationId = notificationId,
                    batchName = batchName,
                    immediate = true
                )
            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(OABX.context)
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(
                object : Observer<WorkInfo?> {    //TODO WECH hg42
                    override fun onChanged(value: WorkInfo?) {
                        when (value?.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                counter += 1

                                val (succeeded, packageLabel, error) = AppActionWork.getOutput(value)
                                if (error.isNotEmpty()) errors =
                                    "$errors$packageLabel: ${      //TODO hg42 add to WorkHandler
                                        LogsHandler.handleErrorMessages(
                                            OABX.context,
                                            error
                                        )
                                    }\n"

                                resultsSuccess = resultsSuccess and succeeded
                                oneTimeWorkLiveData.removeObserver(this)
                            }

                            else                     -> {}
                        }
                    }
                }
            )
        }

        if (worksList.isNotEmpty()) {
            WorkManager.getInstance(OABX.context)
                .beginWith(worksList)
                .enqueue()
        }
    }

    fun startBatchRestoreAction(
        selectedPackageNames: List<String>,
        selectedApk: Map<String, Int>,
        selectedData: Map<String, Int>,
    ) {
        val now = SystemUtils.now
        val notificationId = now.toInt()
        val batchType = getString(R.string.restore)
        val batchName = WorkHandler.getBatchName(batchType, now)

        val selectedItems = buildList {
            selectedPackageNames.forEach { pn ->
                when {
                    selectedApk[pn] == selectedData[pn] && selectedApk[pn] != null -> add(
                        Triple(pn, selectedApk[pn]!!, altModeToMode(ALT_MODE_BOTH, false))
                    )

                    else                                                           -> {
                        if ((selectedApk[pn] ?: -1) != -1) add(
                            Triple(pn, selectedApk[pn]!!, altModeToMode(ALT_MODE_APK, false))
                        )
                        if ((selectedData[pn] ?: -1) != -1) add(
                            Triple(pn, selectedData[pn]!!, altModeToMode(ALT_MODE_DATA, false))
                        )
                    }
                }
            }
        }

        var errors = ""
        var resultsSuccess = true
        var counter = 0
        val worksList: MutableList<OneTimeWorkRequest> = mutableListOf()
        OABX.work.beginBatch(batchName)
        selectedItems.forEach { (packageName, bi, mode) ->
            val oneTimeWorkRequest = AppActionWork.Request(
                packageName = packageName,
                mode = mode,
                backupBoolean = false,
                backupIndex = bi,
                notificationId = notificationId,
                batchName = batchName,
                immediate = true,
            )
            worksList.add(oneTimeWorkRequest)

            val oneTimeWorkLiveData = WorkManager.getInstance(OABX.context)
                .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            oneTimeWorkLiveData.observeForever(
                object : Observer<WorkInfo?> {
                    override fun onChanged(value: WorkInfo?) {
                        when (value?.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                counter += 1

                                val (succeeded, packageLabel, error) = AppActionWork.getOutput(value)
                                if (error.isNotEmpty()) errors =
                                    "$errors$packageLabel: ${
                                        LogsHandler.handleErrorMessages(
                                            OABX.context,
                                            error
                                        )
                                    }\n"

                                resultsSuccess = resultsSuccess and succeeded
                                oneTimeWorkLiveData.removeObserver(this)
                            }

                            else                     -> {}
                        }
                    }
                }
            )
        }

        if (worksList.isNotEmpty()) {
            WorkManager.getInstance(OABX.context)
                .beginWith(worksList)
                .enqueue()
        }
    }

    fun resumeMain() {
        when {
            !persist_beenWelcomed.value
                 -> if (!navController.currentDestination?.route?.equals(NavItem.Welcome.destination)!!) {
                navController.clearBackStack()
                navController.safeNavigate(NavItem.Welcome.destination)
            }

            allPermissionsGranted && this::navController.isInitialized
                 -> launchMain()

            else -> navController.safeNavigate(NavItem.Permissions.destination)
        }
    }

    private fun launchMain() {
        when {
            isBiometricLockAvailable() && isDeviceLockEnabled() -> {
                val currentDestination =
                    navController.currentDestination?.route ?: NavItem.Main.destination
                val wasInited = !listOf(
                    NavItem.Welcome.destination,
                    NavItem.Permissions.destination,
                    NavItem.Lock.destination
                ).contains(currentDestination)
                navController.safeNavigate(NavItem.Lock.destination)
                launchBiometricPrompt(
                    if (wasInited) currentDestination
                    else NavItem.Main.destination
                )
            }

            listOf(
                NavItem.Welcome.destination,
                NavItem.Permissions.destination,
                NavItem.Lock.destination
            ).contains(navController.currentDestination?.route) -> {
                navController.safeNavigate(NavItem.Main.destination)
            }
        }
    }

    private fun launchBiometricPrompt(destination: String) {
        try {
            val biometricPrompt = createBiometricPrompt(destination)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.prefs_biometriclock))
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL or (if (isBiometricLockEnabled()) BiometricManager.Authenticators.BIOMETRIC_WEAK else 0))
                .build()
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Throwable) {
            navController.safeNavigate(destination)
        }
    }

    private fun createBiometricPrompt(destination: String): BiometricPrompt {
        return BiometricPrompt(this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    navController.safeNavigate(destination)
                }
            })
    }
}
