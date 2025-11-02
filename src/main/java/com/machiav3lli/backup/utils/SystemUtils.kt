package com.machiav3lli.backup.utils

import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.FileUriExposedException
import android.os.SystemClock
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.handler.LogsHandler
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
import java.net.URLDecoder


object SystemUtils {

    // using reflection to get id of calling user since method getCallingUserId of UserHandle is hidden
    // https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/UserHandle.java#L123
    val currentProfile: Int
        get() {
            //TODO hg42 another possibility RootFile.cmd("echo \$USER_ID").toInt()
            try {
                // using reflection to get id of calling user since method getCallingUserId of UserHandle is hidden
                // https://github.com/android/platform_frameworks_base/blob/master/core/java/android/os/UserHandle.java#L123
                val userHandle = Class.forName("android.os.UserHandle")
                val muEnabled = userHandle.getField("MU_ENABLED").getBoolean(null)
                val range = userHandle.getField("PER_USER_RANGE").getInt(null)
                if (muEnabled) return Binder.getCallingUid() / range
            } catch (ignored: ClassNotFoundException) {
            } catch (ignored: NoSuchFieldException) {
            } catch (ignored: IllegalAccessException) {
            }
            return 0
        }

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

    fun share(text: String, subject: String? = null, asFile: Boolean = true) {
        if (asFile) {
            OABX.context.getExternalFilesDir(DIRECTORY_DOWNLOADS)
                ?.resolve("NeoBackup-share.txt")    // TODO hg42 use subject.replace(illegal, "_").truncate(n)
                ?.also { it.writeText(text) }
                ?.let { SystemUtils.share(StorageFile(it)) }
            return
        }
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
            } catch (e: FileUriExposedException) {
                OABX.context.getExternalFilesDir(DIRECTORY_DOWNLOADS)
                    ?.resolve(file.name ?: "NeoBackup-share.txt")
                    ?.also { it.writeText(file.readText()) }
                    ?.let {
                        share(StorageFile(it))
                    }
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

    fun getLocalFile(
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

    fun getLocalFile(
        uri: Uri,
        isUseablePath: (file: RootFile?) -> Boolean = ::isWritablePath
    ): RootFile? {
        var file : RootFile? = null
        try {
            if (uri.scheme == "file" || uri.scheme == null) {
                val checkFile = RootFile(
                    uri.path // should normally be there, even for file paths
                        ?: Uri.decode(uri.toString())  // paranoid fallback in case it is not
                )
                if (isUseablePath(checkFile)) {
                    Timber.i("found direct RootFile shadow at '$checkFile'")
                    file = checkFile
                } else
                    throw Exception("cannot use RootFile '$checkFile'")
            } else {
                val last =
                //uri.lastPathSegment // docs say: last segment of the decoded(!) path, not the encoded one
                    // because this is not correct = not reliable, we make it explicit:
                    URLDecoder.decode(uri.encodedPath?.split("/")?.last() ?: "", "UTF-8")
                Timber.i("StorageFile: last=$last uri=$uri")
                var (storage, subPath) = last.split(":", limit = 2)
                //val user = currentProfile
                val user_provider = (uri.authority ?: "").split("@", limit = 2)
                val user =
                    if (user_provider.size > 1)
                        user_provider[0]
                    else
                        currentProfile.toString()
                if (storage == "primary")
                    storage = "emulated/$user"
                file = getLocalFile(
                    user,
                    storage,
                    subPath,
                    isUseablePath
                )
                if (file == null)
                    throw Exception("cannot find RootFile shadow at $last")
            }
        } catch (e: Throwable) {
            file = null
            Timber.i("using access via SAF")
        }
        return file
    }

    fun getAndroidFolder(
        subPath: String,
        user: String = currentProfile.toString(),
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
