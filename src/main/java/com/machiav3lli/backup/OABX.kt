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
package com.machiav3lli.backup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.os.PowerManager
import android.os.Process
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charleskorn.kaml.Yaml
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.machiav3lli.backup.OABX.Companion.activity
import com.machiav3lli.backup.OABX.Companion.context
import com.machiav3lli.backup.OABX.Companion.isDebug
import com.machiav3lli.backup.OABX.Companion.isHg42
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.dbs.ODatabase
import com.machiav3lli.backup.dbs.entity.Backup
import com.machiav3lli.backup.handler.AssetHandler
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.WorkHandler
import com.machiav3lli.backup.handler.updateAppTables
import com.machiav3lli.backup.items.StorageFile
import com.machiav3lli.backup.plugins.Plugin
import com.machiav3lli.backup.preferences.pref_busyHitTime
import com.machiav3lli.backup.preferences.pref_prettyJson
import com.machiav3lli.backup.preferences.pref_useYamlPreferences
import com.machiav3lli.backup.preferences.pref_useYamlProperties
import com.machiav3lli.backup.preferences.pref_useYamlSchedules
import com.machiav3lli.backup.preferences.supportLog
import com.machiav3lli.backup.services.PackageUnInstalledReceiver
import com.machiav3lli.backup.services.ScheduleService
import com.machiav3lli.backup.ui.item.BooleanPref
import com.machiav3lli.backup.ui.item.IntPref
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.SystemUtils
import com.machiav3lli.backup.utils.TraceUtils
import com.machiav3lli.backup.utils.TraceUtils.beginNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.classAndId
import com.machiav3lli.backup.utils.TraceUtils.endNanoTimer
import com.machiav3lli.backup.utils.TraceUtils.formatBackups
import com.machiav3lli.backup.utils.TraceUtils.methodName
import com.machiav3lli.backup.utils.backupDirConfigured
import com.machiav3lli.backup.utils.isDynamicTheme
import com.machiav3lli.backup.utils.restartApp
import com.machiav3lli.backup.utils.scheduleAlarmsOnce
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess


//---------------------------------------- developer settings - logging

val pref_maxLogLines = IntPref(
    key = "dev-log.maxLogLines",
    summary = "maximum lines for internal logging",
    entries = ((10..90 step 10) +
            (100..450 step 50) +
            (500..1500 step 500) +
            (2000..5000 step 1000) +
            (5000..20000 step 5000)
            ).toList(),
    defaultValue = 2000
)

val pref_maxLogCount = IntPref(
    key = "dev-log.maxLogCount",
    summary = "maximum count of log files (= entries on log page)",
    entries = ((1..9 step 1) + (10..100 step 10)).toList(),
    defaultValue = 20
)

val pref_catchUncaughtException = BooleanPref(
    key = "dev-log.catchUncaughtException",
    summaryId = R.string.prefs_catchuncaughtexception_summary,
    defaultValue = false
) {
    val pref = it as BooleanPref
    enableDefaultUncaughtExceptionHandler(pref.value)
}

val pref_uncaughtExceptionsJumpToPreferences = BooleanPref(
    key = "dev-log.uncaughtExceptionsJumpToPreferences",
    summary = "in case of unexpected crashes jump to preferences (prevent loops if a preference causes this, and allows to change it, back button leaves the app)",
    defaultValue = false,
    enableIf = { pref_catchUncaughtException.value }
)

val pref_logToSystemLogcat = BooleanPref(
    key = "dev-log.logToSystemLogcat",
    summary = "log to Android logcat, otherwise only internal (internal doesn't help if the app is restarted or if you are catching logs externally, e.g. via Scoop)",
    defaultValue = true
)

val pref_autoLogExceptions = BooleanPref(
    key = "dev-log.autoLogExceptions",
    summary = "create a log for each unexpected exception (may disturb the timing of other operations, meant to detect catched but not expected exceptions, developers are probably intersted in these)",
    defaultValue = false
)

val pref_autoLogSuspicious = BooleanPref(
    key = "dev-log.autoLogSuspicious",
    summary = "create a log for some suspicious but partly expected situations, e.g. detection of duplicate schedules (don't use it regularly)",
    defaultValue = false
)

val pref_autoLogBeforeSchedule = BooleanPref(
    key = "dev-log.autoLogBeforeSchedule",
    summary = "create a log before the schedule starts it's jobs, allows to catch schedule calculations",
    defaultValue = false
)

val pref_autoLogAfterSchedule = BooleanPref(
    key = "dev-log.autoLogAfterSchedule",
    summary = "create a log after each schedule execution",
    defaultValue = false
)

val pref_autoLogUnInstallBroadcast = BooleanPref(
    key = "dev-log.autoLogUnInstallBroadcast",
    summary = "create a log when a package is installed or uninstalled",
    defaultValue = false
)

//---------------------------------------- developer settings - tracing

val pref_trace = BooleanPref(
    key = "dev-trace.trace",
    summary = "global switch for all traceXXX options",
    defaultValue = isDebug || isHg42
)

val traceSection = TraceUtils.TracePrefBold(
    name = "Section",
    summary = "trace important sections (backup, schedule, etc.)",
    default = true
)

val tracePlugin = TraceUtils.TracePref(
    name = "Plugin",
    summary = "trace plugins",
    default = true
)

val traceSchedule = TraceUtils.TracePrefBold(
    name = "Schedule",
    summary = "trace schedules",
    default = true
)

val traceInfo = TraceUtils.TracePrefBold(
    name = "Info",
    summary = "trace informational",
    default = true
)

val tracePrefs = TraceUtils.TracePref(
    name = "Prefs",
    summary = "trace preferences",
    default = true
)

val traceFlows = TraceUtils.TracePrefBold(
    name = "Flows",
    summary = "trace Kotlin Flows (reactive data streams)",
    default = true
)

val traceBusy = TraceUtils.TracePrefBold(
    name = "Busy",
    summary = "trace beginBusy/endBusy (busy indicator)",
    default = true,
)

val traceTiming = TraceUtils.TracePrefBold(
    name = "Timing",
    summary = "show code segment timers",
    default = true,
)

val traceContextMenu = TraceUtils.TracePref(
    name = "ContextMenu",
    summary = "trace context menu actions and events",
    default = true
)

val traceCompose = TraceUtils.TracePref(
    name = "Compose",
    summary = "trace recomposition of UI elements",
    default = true
)

val traceDebug = TraceUtils.TracePref(
    name = "Debug",
    summary = "trace for debugging purposes (for devs)",
    default = false
)

val traceWIP = TraceUtils.TracePrefExtreme(
    name = "WIP",
    summary = "trace for debugging purposes (for devs)",
    default = false
)

val traceAccess = TraceUtils.TracePref(
    name = "Access",
    summary = "trace access",
    default = false
)

val traceBackups = TraceUtils.TracePref(
    name = "Backups",
    summary = "trace backups",
    default = true
)

val traceBackupsScan = TraceUtils.TracePref(
    name = "BackupsScan",
    summary = "trace scanning of backup directory for properties files (for scanning with package name)",
    default = false
)

val traceBackupsScanAll = TraceUtils.TracePref(
    name = "BackupsScanAll",
    summary = "trace scanning of backup directory for properties files (for complete scan)",
    default = false
)

val traceSerialize = TraceUtils.TracePref(
    name = "Serialize",
    summary = "trace json/yaml/... conversions",
    default = false
)

val RESCUE_NAV get() = "rescue"

var previousDefaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

fun enableDefaultUncaughtExceptionHandler(enable: Boolean) {
    if (enable) {
        if (previousDefaultUncaughtExceptionHandler == null)
            previousDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                try {
                    Timber.e("\n\n" + "#".repeat(60) + " uncaughtException")
                    LogsHandler.unexpectedException(e)
                    //LogsHandler.logErrors("uncaught: ${e.message}")
                } catch (_: Throwable) {
                    // ignore
                }
                if (pref_uncaughtExceptionsJumpToPreferences.value) {
                    context.restartApp(RESCUE_NAV)
                }
                // now restart the app
                object : Thread() {
                    override fun run() {
                        Looper.prepare()
                        Looper.loop()
                    }
                }.start()
            } catch (_: Throwable) {
                // ignore
            } finally {
                activity?.finishAffinity()
                exitProcess(3)
            }
        }
    } else {
        Thread.setDefaultUncaughtExceptionHandler(previousDefaultUncaughtExceptionHandler)
    }
}

class OABX : Application() {

    var workHandler_: WorkHandler? = null

    override fun onCreate() {

        // do this early, context will be used immediately
        refApp = WeakReference(this)

        Timber.w("======================================== app create ${classAndId(this)} PID=${Process.myPid()}")

        super.onCreate()

        if (pref_catchUncaughtException.value) {
            enableDefaultUncaughtExceptionHandler(true)
        }

        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> isDynamicTheme }
                .build()
        )

        //TODO hg42 beginBusy(startupMsg)
        startup = true
        busyCountDownAtomic.set(0)
        busyLevel.value = 0
        hitBusy(60000)

        Plugin.ensureScanned()  // before ShellHandler, because plugins are used there
        initShellHandler()

        db = ODatabase.getInstance(applicationContext)

        val result = registerReceiver(
            PackageUnInstalledReceiver(),
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
        )
        Timber.d("registerReceiver: PackageUnInstalledReceiver = $result")

        workHandler_ = WorkHandler()

        MainScope().launch {
            addInfoLogText("--> click title to keep infobox open")
            addInfoLogText("--> long press title for dev tools")

            //ensureBackups()
            updateAppTables()
        }
    }

    override fun onTerminate() {

        Timber.w(
            "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ app destroy ${classAndId(this)} PID=${Process.myPid()}"
        )

        // in case the app is terminated too early
        scheduleAlarmsOnce()

        workHandler_ = workHandler_?.stop()
        refApp = WeakReference(null)
        super.onTerminate()
    }

    companion object {

        val workHandler: WorkHandler? get() = OABX.app.workHandler_

        var booting = false

        @ExperimentalSerializationApi
        val serMod = SerializersModule {
            //contextual(Boolean.serializer())
            //contextual(Int.serializer())
            //contextual(String.serializer())
            //polymorphic(Any::class) {
            //    subclass(Boolean.serializer())
            //    subclass(Int.serializer())
            //    subclass(String.serializer())
            //}
            //polymorphic(Any::class) {
            //    subclass(Boolean::class)
            //    subclass(Int::class)
            //    subclass(String::class)
            //}
            //polymorphic(Any::class) {
            //    subclass(Boolean::class, Boolean.serializer())
            //    subclass(Int::class, Int.serializer())
            //    subclass(String::class, String.serializer())
            //}
            //polymorphic(Any::class, Boolean::class, Boolean.serializer())
            //polymorphic(Any::class, Int::class, Int.serializer())
            //polymorphic(Any::class, String::class, String.serializer())
        }

        // create alternatives here and switch when used to allow dynamic prefs
        @OptIn(ExperimentalSerializationApi::class)
        val JsonDefault = Json {
            serializersModule = serMod
        }

        @OptIn(ExperimentalSerializationApi::class)
        val JsonPretty = Json {
            serializersModule = serMod
            prettyPrint = true
        }

        @OptIn(ExperimentalSerializationApi::class)
        val YamlDefault = Yaml(serMod)

        val propsSerializerDef: Pair<String, StringFormat>
            get() =
                when {
                    pref_useYamlProperties.value -> "yaml" to YamlDefault
                    pref_prettyJson.value        -> "json" to JsonPretty
                    else                         -> "json" to JsonDefault
                }
        val propsSerializer: StringFormat get() = propsSerializerDef.second
        val propsSerializerSuffix: String get() = propsSerializerDef.first

        val prefsSerializerDef: Pair<String, StringFormat>
            get() =
                when {
                    pref_useYamlPreferences.value -> "yaml" to YamlDefault
                    else                          -> "json" to JsonPretty
                }
        val prefsSerializer: StringFormat get() = prefsSerializerDef.second
        val prefsSerializerSuffix: String get() = prefsSerializerDef.first

        val schedSerializerDef: Pair<String, StringFormat>
            get() =
                when {
                    pref_useYamlSchedules.value -> "yaml" to YamlDefault
                    else                        -> "json" to JsonPretty
                }
        val schedSerializer: StringFormat get() = schedSerializerDef.second
        val schedSerializerSuffix: String get() = schedSerializerDef.first

        inline fun <reified T> toSerialized(serializer: StringFormat, value: T) =
            serializer.encodeToString(value)

        inline fun <reified T> fromSerialized(serialized: String): T {
            traceSerialize { "serialized: <-- $serialized" }
            val props: T = try {
                JsonDefault.decodeFromString(serialized)
            } catch (_: Throwable) {
                YamlDefault.decodeFromString(serialized)
            }
            traceSerialize { "    object: --> $props" }
            return props
        }

        val lastLogMessages = ConcurrentLinkedQueue<String>()
        fun addLogMessage(message: String) {
            val maxLogLines = try {
                pref_maxLogLines.value
            } catch (_: Throwable) {
                2000
            }
            lastLogMessages.add(message)
            val size = lastLogMessages.size
            val nDelete = size - maxLogLines
            if (nDelete > 0)
                repeat(nDelete) {
                    lastLogMessages.remove()
                }
        }

        var logsDirectory: StorageFile? = null
            get() {
                if (field == null) {
                    //TODO hg42 immer intern und vielleicht auch in cache ???
                    //field = context.getExternalFilesDir(null)?.let { StorageFile(it).ensureDirectory("logs") }
                    //    ?: context.filesDir.let { StorageFile(it).ensureDirectory("logs") }
                    field = context.cacheDir.let { StorageFile(it).ensureDirectory("logs") }
                }
                return field
            }

        var lastErrorPackage = ""
        var lastErrorCommands = ConcurrentLinkedQueue<String>()
        fun addErrorCommand(command: String) {
            val maxErrorCommands = 10
            lastErrorCommands.add(command)
            val size = lastErrorCommands.size
            val nDelete = size - maxErrorCommands
            if (nDelete > 0)
                repeat(nDelete) {
                    lastErrorCommands.remove()
                }
        }

        var logSections = mutableMapOf<String, Int>()
            .withDefault { 0 }     //TODO hg42 use AtomicInteger? but map is synchronized anyways

        var startup = true
        var ready = false

        init {

            Timber.plant(object : Timber.DebugTree() {

                override fun log(
                    priority: Int, tag: String?, message: String, t: Throwable?,
                ) {
                    val logToSystemLogcat = try {
                        pref_logToSystemLogcat.value
                    } catch (_: Throwable) {
                        true
                    }
                    if (logToSystemLogcat)
                        super.log(priority, "$tag", message, t)

                    val prio =
                        when (priority) {
                            Log.VERBOSE -> "V"
                            Log.ASSERT  -> "A"
                            Log.DEBUG   -> "D"
                            Log.ERROR   -> "E"
                            Log.INFO    -> "I"
                            Log.WARN    -> "W"
                            else        -> "?"
                        }
                    val now = SystemUtils.now
                    val date = ISO_DATE_TIME_FORMAT_MS.format(now)
                    try {
                        addLogMessage("$date $prio $tag : $message")
                    } catch (e: Throwable) {
                        // ignore
                        runCatching {
                            lastLogMessages.clear()
                            addLogMessage("$date E LOG : while adding or limiting log lines")
                            addLogMessage(
                                "$date E LOG : ${
                                    LogsHandler.message(
                                        e,
                                        backTrace = true
                                    )
                                }"
                            )
                        }
                    }
                }

                override fun createStackElementTag(element: StackTraceElement): String {
                    var tag = "${
                        super.createStackElementTag(element)
                    }:${
                        element.lineNumber
                    }::${
                        element.methodName
                    }"
                    if (tag.contains("TraceUtils"))
                        tag = ""
                    return "NeoBackup>$tag"
                }
            })
        }

        // app should always be created when it runs, so we can be sure it's not null
        var refApp: WeakReference<OABX> = WeakReference(null)
        val app: OABX get() = refApp.get()!!

        // lint: "Do not place Android context classes in static fields; this is a memory leak"
        // but: only if a context is assigned, and this is only used by Preview (and maybe by Tests)
        @SuppressLint("StaticFieldLeak")
        var fakeContext: Context? = null
        val context: Context get() = fakeContext ?: app.applicationContext

        var assetsRef: WeakReference<AssetHandler> = WeakReference(null)
        val assets: AssetHandler
            get() {
                if (assetsRef.get() == null)
                    assetsRef = WeakReference(AssetHandler(context))
                return assetsRef.get()!!
            }

        // service might be null
        var serviceRef: WeakReference<ScheduleService> = WeakReference(null)
        var service: ScheduleService?
            get() {
                return serviceRef.get()
            }
            set(service) {
                serviceRef = WeakReference(service)
            }

        // activity might be null
        private var activityRefs = mutableListOf<WeakReference<Activity>>()
        private var activityRef: WeakReference<Activity> = WeakReference(null)
        val activity: Activity?
            get() {
                return activityRef.get()
            }

        fun addActivity(activity: Activity) {
            activityRef = WeakReference(activity)
            synchronized(activityRefs) {
                traceDebug { "activities.add: ${classAndId(activity)}" }
                // remove activities of the same class
                //activityRef.get()?.localClassName.let { localClassName ->
                //    activityRefs.removeIf { it.get()?.localClassName == localClassName }
                //}
                activityRefs.add(activityRef)
                activityRefs.removeIf { it.get() == null }
                traceDebug { "activities(add): ${activityRefs.map { classAndId(it.get()) }}" }
            }

            scheduleAlarmsOnce()        // if any activity is started
        }

        fun resumeActivity(activity: Activity) {
            activityRef = WeakReference(activity)
            synchronized(activityRefs) {
                traceDebug { "activities.res: ${classAndId(activity)}" }
                activityRefs.removeIf { it.get() == activity }
                activityRefs.add(activityRef)
                activityRefs.removeIf { it.get() == null }
                traceDebug { "activities(res): ${activityRefs.map { classAndId(it.get()) }}" }
            }

            scheduleAlarmsOnce()        // if any activity is started
        }

        fun removeActivity(activity: Activity) {
            synchronized(activityRefs) {
                traceDebug { "activities.remove: ${classAndId(activity)}" }
                //activityRefs.removeIf { it.get()?.localClassName == activity.localClassName }
                activityRefs.removeIf { it.get() == activity }
                activityRef = WeakReference(null)
                activityRefs.removeIf { it.get() == null }
                traceDebug { "activities(remove): ${activityRefs.map { classAndId(it.get()) }}" }
            }
        }

        val activities: List<Activity>
            get() {
                synchronized(activityRefs) {
                    return activityRefs.mapNotNull { it.get() }
                }
            }

        // main might be null
        var mainRef: WeakReference<MainActivityX> = WeakReference(null)
        var main: MainActivityX?
            get() {
                return mainRef.get()
            }
            set(mainActivity) {
                mainRef = WeakReference(mainActivity)
            }
        var mainSaved: MainActivityX? = null    // just to see if activity changed
        var viewModelSaved: ViewModel? = null

        var appsSuspendedChecked = false

        var shellHandler: ShellHandler? = null
            private set

        var dbRef: WeakReference<ODatabase> = WeakReference(null)
        var db: ODatabase
            get() {
                return dbRef.get() ?: ODatabase.getInstance(context)
            }
            set(dbInstance) {
                dbRef = WeakReference(dbInstance)
            }

        fun initShellHandler(): ShellHandler? {
            return try {
                shellHandler = ShellHandler()
                shellHandler
            } catch (e: Throwable) {
                null
            }
        }

        fun getString(resId: Int) = context.getString(resId)

        fun minSDK(sdk: Int): Boolean {
            return Build.VERSION.SDK_INT >= sdk
        }

        val isRelease get() = packageName.endsWith(".backup")
        val isDebug get() = packageName.contains("debug")
        val isNeo get() = packageName.contains("neo")
        val isHg42 get() = packageName.contains("hg42")

        fun Context.getApplicationInfos(what: Int = 0): PackageInfo? {
            val packageManager: PackageManager = getPackageManager()
            return packageManager.getPackageInfo(packageName, what)
        }

        @Suppress("DEPRECATION")
        private fun Context.getApplicationIssuer(): String? {
            runCatching {
                val signatures = if (OABX.minSDK(28)) {
                    val packageInfo =
                        OABX.context.getApplicationInfos(PackageManager.GET_SIGNING_CERTIFICATES)
                    val signingInfo = packageInfo?.signingInfo
                    signingInfo?.getSigningCertificateHistory() ?: arrayOf()
                } else {
                    val packageInfo =
                        OABX.context.getApplicationInfos(PackageManager.GET_SIGNATURES)
                    packageInfo?.signatures ?: arrayOf()
                }
                if (signatures.isEmpty())
                    return null
                val signature = signatures[0]
                val signatureBytes = signature.toByteArray()
                val cf = CertificateFactory.getInstance("X509")
                val x509Certificate: X509Certificate =
                    cf.generateCertificate(ByteArrayInputStream(signatureBytes)) as X509Certificate
                val DN = x509Certificate.getIssuerDN().getName()
                val names = DN.split(",").map {
                    val (field, value) = it.split("=", limit = 2)
                    field to value
                }.toMap()
                var issuer = names["CN"]
                names["O"]?.let { if (issuer != it) issuer = "$issuer / $it" }
                return issuer ?: DN
            }
            return null
        }

        val packageName get() = com.machiav3lli.backup.BuildConfig.APPLICATION_ID
        val versionCode get() = com.machiav3lli.backup.BuildConfig.VERSION_CODE
        val versionName get() = com.machiav3lli.backup.BuildConfig.VERSION_NAME
        val updateId get() = "${OABX.context.getApplicationInfos()?.lastUpdateTime?.toString()}-${versionName}"
        val backupVersionCode get() = com.machiav3lli.backup.BuildConfig.MAJOR * 1000 + com.machiav3lli.backup.BuildConfig.MINOR

        val applicationIssuer get() = OABX.context.getApplicationIssuer() ?: "UNKNOWN ISSUER"

        //------------------------------------------------------------------------------------------ backupRoot

        var backupRoot: StorageFile? = null
            get() {
                if (field == null) {
                    val storagePath = backupDirConfigured
                    if (storagePath.isEmpty()) {
                        Timber.e("backup storage location not configured")
                        throw StorageLocationNotConfiguredException()
                    }
                    val storageDir = StorageFile.fromUri(storagePath)
                    if (!storageDir.exists()) { //TODO hg42 for now only existing directories allowed
                        Timber.e("backup storage location not accessible: $storagePath")
                        throw FileUtils.BackupLocationInAccessibleException("Cannot access the root location '$storagePath'")
                    }
                    Timber.e("backup storage location found at ${storageDir.path}")
                    field = storageDir
                }
                return field
            }

        //------------------------------------------------------------------------------------------ infoText

        var infoLogLines = mutableStateListOf<String>()

        val nInfoLogLines = 100
        var showInfoLog by mutableStateOf(false)

        fun clearInfoLogText() {
            synchronized(infoLogLines) {
                infoLogLines = mutableStateListOf()
            }
        }

        fun addInfoLogText(value: String) {
            synchronized(infoLogLines) {
                infoLogLines.add(value)
                if (infoLogLines.size > nInfoLogLines)
                    infoLogLines.drop(1)
            }
        }

        fun getInfoLogText(n: Int = nInfoLogLines, fill: String? = null): String {
            synchronized(infoLogLines) {
                val lines = infoLogLines.takeLast(n).toMutableList()
                if (fill != null)
                    while (lines.size < n)
                        lines.add(fill)
                return lines.joinToString("\n")
            }
        }

        //------------------------------------------------------------------------------------------ wakelock

        // if any background work is to be done
        private var theWakeLock: PowerManager.WakeLock? = null
        private var wakeLockNested = AtomicInteger(0)
        private const val wakeLockTag = "NeoBackup:Application"

        // count the nesting levels
        // might be difficult sometimes, because
        // the lock must be transferred from one object/function to another
        // e.g. from the receiver to the service
        fun wakelock(aquire: Boolean) {
            if (aquire) {
                traceDebug { "%%%%% $wakeLockTag wakelock aquire (before: $wakeLockNested)" }
                if (wakeLockNested.accumulateAndGet(+1, Int::plus) == 1) {
                    val pm = context.getSystemService(POWER_SERVICE) as PowerManager
                    theWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag)
                    theWakeLock?.acquire(60 * 60 * 1000L)
                    traceDebug { "%%%%% $wakeLockTag wakelock ACQUIRED" }
                }
            } else {
                traceDebug { "%%%%% $wakeLockTag wakelock release (before: $wakeLockNested)" }
                if (wakeLockNested.accumulateAndGet(-1, Int::plus) == 0) {
                    traceDebug { "%%%%% $wakeLockTag wakelock RELEASING" }
                    theWakeLock?.release()
                }
            }
        }

        //------------------------------------------------------------------------------------------ progress

        val progress = mutableStateOf(Pair(false, 0f))

        fun setProgress(now: Int = 0, max: Int = 0) {
            if (max <= 0)
                progress.value = Pair(false, 0f)
            else
                progress.value = Pair(true, 1f * now / max)
        }

        //------------------------------------------------------------------------------------------ section

        fun beginLogSection(section: String) {
            var count: Int
            synchronized(logSections) {
                count = logSections.getValue(section)
                logSections[section] = count + 1
                //if (count == 0 && xxx)  logMessages.clear()           //TODO hg42
            }
            traceSection { """*** ${"|---".repeat(count)}\ $section""" }
            beginNanoTimer("section.$section")
        }

        fun endLogSection(section: String) {    //TODO hg42 timer!
            val time = endNanoTimer("section.$section")
            var count: Int
            synchronized(logSections) {
                count = logSections.getValue(section)
                logSections[section] = count - 1
            }
            traceSection { "*** ${"|---".repeat(count - 1)}/ $section ${"%.3f".format(time / 1E9)} sec" }
            //if (count == 0 && xxx)  ->Log                             //TODO hg42
        }

        //------------------------------------------------------------------------------------------ busy

        val busyMaxLevel = 11
        var busyErrorTriggered = false
        val busyTick = 500
        var busyCountDownAtomic = AtomicInteger(0)
        var busy = mutableStateOf(false)
        var busyLevel = mutableStateOf(0)
        var busyCountDown = mutableStateOf(0)

        init {
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    delay(busyTick.toLong())            //TODO hg42 don't poll -> flow?
                    if (!ready) {
                        busyCountDown.value = 111
                        busy.value = true
                    } else
                        busyCountDownAtomic.getAndUpdate {
                            if (it > 0) {
                                val next = it - 1
                                busyCountDown.value = next
                                if (next <= 0) {
                                    busy.value = false
                                } else if (busy.value == false)
                                    busy.value = true
                                next
                            } else
                                it
                        }
                }
            }
            hitBusy(60000)
        }

        fun hitBusy(time: Int = pref_busyHitTime.value) {
            busyCountDownAtomic.set(
                time / busyTick
            )
        }

        fun beginBusy(name: String? = null) {
            var level = synchronized(busyLevel) {
                busyLevel.value += 1
                busyLevel.value
            }
            if (level > busyMaxLevel) {
                traceBusy { "*** ?????????? busyLevel > ${busyMaxLevel}" }
                if (!busyErrorTriggered) {
                    supportLog("busyLevel > ${busyMaxLevel}")
                }
                busyErrorTriggered = true
                level = synchronized(busyLevel) {
                    busyLevel.value = busyMaxLevel
                    busyMaxLevel
                }
            }
            traceBusy {
                val label = name ?: methodName(1)
                "*** $level ${"|---".repeat(level)}\\ busy $label"
            }
            hitBusy(60000)
            beginNanoTimer("busy.$name")
        }

        fun endBusy(name: String? = null): Long {
            val time = endNanoTimer("busy.$name")
            var level = synchronized(busyLevel) {
                busyLevel.value -= 1
                busyLevel.value
            }
            if (level <= 0) {
                busyCountDownAtomic.set(1)
                if (level < 0) {
                    traceBusy { "*** ?????????? busyLevel < 0" }
                    if (!busyErrorTriggered) {
                        supportLog("busyLevel < 0")
                    }
                    busyErrorTriggered = true
                    level = synchronized(busyLevel) {
                        busyLevel.value = 0
                        0
                    }
                }
            }
            traceBusy {
                val label = name ?: methodName(1)
                "*** $level ${"|---".repeat(level+1)}/ busy $label ${"%.3f".format(time / 1E9)} sec"
            }
            return time
        }

        //------------------------------------------------------------------------------------------ runningSchedules

        val runningSchedules = mutableMapOf<Long, Long>()

        //------------------------------------------------------------------------------------------ backups

        private var theBackupsMap = mutableMapOf<String, List<Backup>>()

        fun updateUI() {
            main?.viewModel?.apply {
                viewModelScope.launch {
                    backupsChanged.update.emit(getBackups())
                }
            }
        }

        var validBackups = false

        fun getBackups(): Map<String, List<Backup>> {
            synchronized(theBackupsMap) {
                return theBackupsMap.toPersistentMap()
            }
        }

        fun getBackups(packageName: String): List<Backup> {
            val backups = synchronized(theBackupsMap) {
                theBackupsMap.get(packageName) ?: emptyList()
            }
            return backups.drop(0)  // copy
            //return backups
        }

        fun clearBackups() {
            synchronized(theBackupsMap) {
                theBackupsMap.clear()
                validBackups = false
                updateUI()
            }
        }

        fun setBackups(backupsMap: Map<String, List<Backup>>) {
            synchronized(theBackupsMap) {
                backupsMap.forEach { (packageName, backups) ->
                    theBackupsMap.put(packageName, backups)
                }
                // clear no more existing packages
                (theBackupsMap.keys - backupsMap.keys).forEach {
                    theBackupsMap.remove(it)
                }
                validBackups = true
                updateUI()
            }
        }

        fun putBackups(packageName: String, backups: List<Backup>) {
            synchronized(theBackupsMap) {
                traceBackups {
                    "putBackups: $packageName \\ : ${
                        classAndId(
                            theBackupsMap.get(
                                packageName
                            )
                        )
                    } ${formatBackups(theBackupsMap.get(packageName))}"
                }
                theBackupsMap.put(packageName, backups)
                traceBackups {
                    "putBackups: $packageName / = ${
                        classAndId(
                            theBackupsMap.get(
                                packageName
                            )
                        )
                    } ${formatBackups(theBackupsMap.get(packageName))}"
                }
                updateUI()
            }
        }

        fun emptyBackupsForPackages(packageNames: List<String>) {
            synchronized(theBackupsMap) {
                packageNames.forEach {
                    theBackupsMap.put(it, emptyList())
                    updateUI()
                }
            }
        }
    }
}
