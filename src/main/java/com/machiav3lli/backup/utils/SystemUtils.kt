package com.machiav3lli.backup.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.SystemClock
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.items.RootFile
import com.machiav3lli.backup.items.StorageFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate


object SystemUtils {

    fun Context.getApplicationInfos(what: Int = 0): PackageInfo? {
        val packageManager: PackageManager = getPackageManager()
        return packageManager.getPackageInfo(packageName, what)
    }

    @Suppress("DEPRECATION")
    private fun Context.getApplicationIssuer() : String? {
        runCatching {
            val signatures = if (OABX.minSDK(28)) {
                val packageInfo = OABX.context.getApplicationInfos(PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = packageInfo?.signingInfo
                signingInfo?.getSigningCertificateHistory() ?: arrayOf()
            } else {
                val packageInfo = OABX.context.getApplicationInfos(PackageManager.GET_SIGNATURES)
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
            names["O"]?.let { if (issuer != it) issuer = "$issuer / $it"}
            return issuer ?: DN
        }
        return null
    }

    val packageName get() = com.machiav3lli.backup.BuildConfig.APPLICATION_ID
    @Suppress("DEPRECATION")
    //val versionCode get() = if (OABX.minSDK(28)) {
    //    OABX.context.getApplicationInfos()?.longVersionCode
    //} else {
    //    OABX.context.getApplicationInfos()?.versionCode
    //} ?: com.machiav3lli.backup.BuildConfig.VERSION_CODE
    val versionCode get() = com.machiav3lli.backup.BuildConfig.VERSION_CODE
    //val versionName get() = OABX.context.getApplicationInfos()?.versionName ?: com.machiav3lli.backup.BuildConfig.VERSION_NAME
    val versionName get() = com.machiav3lli.backup.BuildConfig.VERSION_NAME
    val updateId get() = "${OABX.context.getApplicationInfos()?.lastUpdateTime?.toString()}-${versionName}"

    val applicationIssuer get() = OABX.context.getApplicationIssuer() ?: "UNKNOWN ISSUER"

    val numCores get() = Runtime.getRuntime().availableProcessors()

    suspend fun <T> runParallel(
        items: List<T>,
        scope: CoroutineScope = MainScope(),
        pool: CoroutineDispatcher = Dispatchers.IO,
        todo: (item: T) -> Unit
    ) {
        val list = items.toList()
        when (1) {

            // best,  8 threads, may hang with recursion
            0 -> list.stream().parallel().forEach { todo(it) }

            // slow,  7 threads with IO, most used once, one used 900 times
            0 -> runBlocking { list.asFlow().onEach { todo(it) }.flowOn(pool).collect {} }

            // slow,  1 thread with IO
            0 -> list.asFlow().onEach { todo(it) }.collect {}

            // slow, 19 threads with IO
            0 -> list.asFlow().map { scope.launch(pool) { todo(it) } }.collect { it.join() }

            // best, 63 threads with IO
            0 -> runBlocking { list.asFlow().collect { launch(pool) { todo(it) } } }

            // best, 66 threads with IO
            0 -> list.map { scope.launch(pool) { todo(it) } }.joinAll()

            // best, 63 threads with IO
            1 -> runBlocking { list.forEach { launch(pool) { todo(it) } } }
        }
    }

    fun share(text: String, subject: String? = null) {
        MainScope().launch(Dispatchers.IO) {
            try {
                if (text.isEmpty())
                    throw Exception("text is empty")
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    if (subject.isNullOrEmpty())
                        putExtra(Intent.EXTRA_SUBJECT, "[NeoBackup]")
                    else
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                val shareIntent = Intent.createChooser(sendIntent, subject ?: "NeoBackup")
                OABX.activity?.startActivity(shareIntent)
            } catch (e: Throwable) {
                LogsHandler.unexpectedException(e)
            }
        }
    }

    fun share(file: StorageFile, asFile: Boolean = true) {
        MainScope().launch(Dispatchers.IO) {
            try {
                val text = if (asFile) "" else file.readText()
                if (!asFile and text.isEmpty())
                    throw Exception("${file.name} is empty or cannot be read")
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    putExtra(Intent.EXTRA_SUBJECT, "[NeoBackup] ${file.name}")
                    if (asFile)
                        putExtra(Intent.EXTRA_STREAM, file.uri)  // send as file
                    else
                        putExtra(Intent.EXTRA_TEXT, text)       // send as text
                }
                val shareIntent = Intent.createChooser(sendIntent, file.name)
                OABX.activity?.startActivity(shareIntent)
            } catch (e: Throwable) {
                LogsHandler.unexpectedException(e)
            }
        }
    }

    fun isWritablePath(file: RootFile?): Boolean =
        file?.let { file.exists() && file.canRead() && file.canWrite() } ?: false

    fun isReadablePath(file: RootFile?): Boolean =
        file?.let { file.exists() && file.canRead() } ?: false

    val storagePath = mutableMapOf<String, RootFile?>()

    fun getShadowPath(
        user: String,
        storage: String,
        subPath: String,
        isUseablePath: (file: RootFile?) -> Boolean = ::isWritablePath
    ): RootFile? {
        // check final path for shadow
        val key = "shadow:$user:$storage:$subPath"
        return storagePath.getOrElse(key) {
            val possiblePaths = listOf(
                "/mnt/media_rw/$storage/$subPath",
                "/mnt/pass_through/$user/$storage/$subPath",
                "/mnt/runtime/full/$storage/$subPath",
                "/mnt/runtime/default/$storage/$subPath",

                // NOTE: lockups occur in emulator (or A12?) for certain paths
                // e.g. /storage/emulated/$user
                //
                // lockups! primary links to /storage/emulated/$user and all self etc.
                //"/storage/$storage/$subpath",
                //"/storage/self/$storage/$subpath",
                //"/mnt/runtime/default/self/$storage/$subpath"
                //"/mnt/user/$user/$storage/$subpath",
                //"/mnt/user/$user/self/$storage/$subpath",
                //"/mnt/androidwritable/$user/self/$storage/$subpath",
            )
            possiblePaths.forEach { path ->
                val file = RootFile(path)
                if (isUseablePath(file)) {   //TODO hg42 check with timeout in case of lockups
                    Timber.i("found $key at $file")
                    storagePath.put(path, file)
                    return file
                }
            }
            return null
        }
    }

    fun getAndroidFolder(
        subPath: String,
        user: String = ShellCommands.currentProfile.toString(),
        isUseablePath: (file: RootFile?) -> Boolean = ::isWritablePath
    ): RootFile? {
        // only check access to Android folder and add subFolder even if it does not exist
        val key = "Android:$user:$subPath"
        return storagePath.getOrElse(key) {
            val baseKey = "Android:$user:"
            storagePath.getOrElse(baseKey) {
                val possiblePaths = listOf(
                    "/data/media/$user/Android",
                    "/mnt/pass_through/$user/emulated/$user/Android",
                    "/mnt/user/$user/emulated/$user/Android",
                )
                possiblePaths.forEach { path ->
                    val file = RootFile(path)
                    if (isUseablePath(file)) {   //TODO hg42 check with timeout in case of lockups
                        Timber.i("found $key at$file")
                        storagePath.put(baseKey, file)
                        val targetFile = RootFile(file, subPath)
                        storagePath.put(key, targetFile)
                        return targetFile
                    }
                }
                return null
            }?.let {
                val targetFile = RootFile(it, subPath)
                storagePath.put(key, targetFile)
                return targetFile
            }
            return null
        }
    }

    val msSinceBoot get() = SystemClock.elapsedRealtime()
    val now get() = System.currentTimeMillis()
}
