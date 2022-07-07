package com.machiav3lli.backup.items

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_ALLOWSHADOWINGDEFAULT
import com.machiav3lli.backup.PREFS_CACHEURIS
import com.machiav3lli.backup.PREFS_COLUMNNAMESAF
import com.machiav3lli.backup.PREFS_INVALIDATESELECTIVE
import com.machiav3lli.backup.PREFS_SHADOWROOTFILE
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.utils.exists
import com.machiav3lli.backup.utils.getName
import com.machiav3lli.backup.utils.isDirectory
import com.machiav3lli.backup.utils.isFile
import com.machiav3lli.backup.utils.length
import com.machiav3lli.backup.utils.suRecursiveCopyFilesToDocument
import org.apache.commons.io.FileUtils.listFiles
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Long.max
import java.lang.Long.min
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

// TODO MAYBE migrate at some point to FuckSAF

open class StorageFile {

    var parent: StorageFile? = null
    var context: Context? = null

    private var _uri: Uri? = null
    val uri: Uri?
        get() = _uri ?: file?.let { file ->
            context?.let {
                FileProvider.getUriForFile(
                    it, "${it.applicationContext.packageName}.provider", file
                )
            }
        } ?: Uri.fromFile(file?.absoluteFile)
    private var file: RootFile? = null
    private var parentFile: RootFile? = null

    constructor(
        parent: StorageFile?,
        context: Context?,
        uri: Uri?,
        name: String? = null,
        allowShadowing: Boolean = OABX.prefFlag(
            PREFS_ALLOWSHADOWINGDEFAULT,
            false
        ) // Storage files that should be shadowable should be explicitly decalred as such
    ) {
        this.parent = parent
        this.context = context
        this._uri = uri
        name?.let { this.name = it }
        if (OABX.prefFlag(PREFS_SHADOWROOTFILE, false) && allowShadowing) {
            fun isValidPath(file: RootFile?): Boolean =
                file?.let { file.exists() && file.canRead() && file.canWrite() } ?: false
            parent ?: run {
                file ?: run {
                    uri?.let { uri ->
                        val last = uri.lastPathSegment!!
                        try {
                            Timber.i("SAF: last=$last uri=$uri")
                            if (last.startsWith('/')) {
                                val checkFile = RootFile(last)
                                if (isValidPath(checkFile)) {
                                    Timber.i("found direct RootFile shadow at $checkFile")
                                    file = checkFile
                                } else
                                    throw Exception("cannot use RootFile shadow at $last")
                            } else {
                                var (storage, subpath) = last.split(":")
                                val user = ShellCommands.currentUser
                                if (storage == "primary")
                                    storage = "emulated/$user"
                                // NOTE: lockups occur in emulator (or A12?) for certain paths
                                // e.g. /storage/emulated/$user
                                val possiblePaths = listOf(
                                    "/mnt/media_rw/$storage/$subpath",
                                    "/mnt/pass_through/$user/$storage/$subpath",
                                    "/mnt/runtime/full/$storage/$subpath",
                                    "/mnt/runtime/default/$storage/$subpath",

                                    // lockups! primary links to /storage/emulated/$user and all self etc.
                                    //"/storage/$storage/$subpath",
                                    //"/storage/self/$storage/$subpath",
                                    //"/mnt/runtime/default/self/$storage/$subpath"
                                    //"/mnt/user/$user/$storage/$subpath",
                                    //"/mnt/user/$user/self/$storage/$subpath",
                                    //"/mnt/androidwritable/$user/self/$storage/$subpath",
                                )
                                var checkFile: RootFile? = null
                                for (path in possiblePaths) {
                                    checkFile = RootFile(path)
                                    if (isValidPath(checkFile)) {   //TODO hg42 check with timeout in case of lockups
                                        Timber.i("found storage RootFile shadow at $checkFile")
                                        file = checkFile
                                        break
                                    }
                                    checkFile = null
                                }
                                if (checkFile == null)
                                    throw Exception(
                                        "cannot use RootFile shadow at one of ${
                                            possiblePaths.joinToString(
                                                " "
                                            )
                                        }"
                                    )
                            }
                        } catch (e: Throwable) {
                            file = null
                            Timber.i("using access via SAF")
                        }
                    }
                }
            }
        }
    }

    constructor(file: RootFile) {
        this.file = file
    }

    constructor(file: File) {
        this.file = RootFile(file)
    }

    constructor(parent: StorageFile, file: RootFile) {
        this.parent = parent
        this.file = file
    }

    constructor(parent: StorageFile, path: String) {
        this.parent = parent
        file = RootFile(parent.file, path)
    }

    var name: String? = null
        get() {
            if (field == null) {
                field = file?.name ?: let {
                    context?.let { context -> _uri?.getName(context) }
                }
            }
            return field
        }
        private set(name) {
            field = name
        }

    val path: String?
        get() = file?.path ?: _uri?.path

    override fun toString(): String {
        return path ?: "null"
    }

    val isFile: Boolean
        get() = file?.isFile ?: context?.let { context -> _uri?.isFile(context) } ?: false

    val isDirectory: Boolean
        get() = file?.isDirectory ?: context?.let { context -> _uri?.isDirectory(context) } ?: false

    val isPropertyFile: Boolean
        get() = name?.endsWith(".properties") ?: false

    fun exists(): Boolean =
        file?.exists() ?: context?.let { context -> _uri?.exists(context) } ?: false

    val size: Long
        get() = (
            if (file != null)
                (file?.length() ?: 0L)
            else
                (context?.let { max(0L, uri?.length(it) ?: 0L) } ?: 0L)
            ) + listFiles().sumOf { it.size }

    fun inputStream(): InputStream? {
        return file?.inputStream() ?: _uri?.let { uri ->
            context?.contentResolver?.openInputStream(uri)
        }
    }

    fun outputStream(): OutputStream? {
        return file?.outputStream() ?: _uri?.let { uri ->
            context?.contentResolver?.openOutputStream(uri, "w")
        }
    }

    fun createDirectory(displayName: String): StorageFile {
        val newFile =
            file?.let {
                val newDir = RootFile(it, displayName)
                newDir.mkdirs()
                StorageFile(this, newDir)
            } ?: run {
                StorageFile(
                    this, context!!,
                    createFile(
                        context!!, _uri!!,
                        DocumentsContract.Document.MIME_TYPE_DIR, displayName
                    )
                )
            }
        path?.let { cacheFilesAdd(it, newFile) }
        return newFile
    }

    fun createFile(mimeType: String, displayName: String): StorageFile {
        val newFile =
            file?.let {
                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    val newDir = RootFile(it, displayName)
                    newDir.mkdirs()
                    StorageFile(this, newDir)
                } else {
                    val newFile = RootFile(it, displayName)
                    newFile.createNewFile()
                    StorageFile(this, newFile)
                }
            } ?: run {
                StorageFile(
                    this, context,
                    createFile(context!!, _uri!!, mimeType, displayName)
                )
            }
        path?.let { cacheFilesAdd(it, newFile) }
        return newFile
    }

    fun delete(): Boolean {
        path?.let { cacheFilesRemove(it, this) }
        return try {
            file?.deleteRecursive()
                ?: DocumentsContract.deleteDocument(context!!.contentResolver, _uri!!)
        } catch (e: FileNotFoundException) {
            false
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
            false
        }
    }

    fun renameTo(displayName: String): Boolean {
        path?.let { cacheFilesRemove(it, this) }
        var ok = false
        file?.let { oldFile ->
            val newFile = RootFile(oldFile.parent, displayName)
            ok = oldFile.renameTo(newFile)
            file = newFile
        } ?: try {
            val result =
                context?.let { context ->
                    _uri?.let { uri ->
                        DocumentsContract.renameDocument(
                            context.contentResolver, uri, displayName
                        )
                    }
                }
            if (result != null) {
                _uri = result
                ok = true
            }
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
            ok = false
        }
        path?.let { cacheFilesAdd(it, this) }
        return ok
    }

    fun findUri(displayName: String): Uri? {
        try {
            for (file in listFiles()) {
                if (displayName == file.name) {
                    return file._uri
                }
            }
        } catch (e: FileNotFoundException) {
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
        }
        return null
    }

    fun findFile(displayName: String): StorageFile? {
        try {
            file?.let {
                val found = StorageFile(this, displayName)
                return if (found.exists()) found else null
            }
            for (file in listFiles()) {
                if (displayName == file.name) {
                    return file
                }
            }
        } catch (e: FileNotFoundException) {
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
        }
        return null
    }

    fun recursiveCopyFiles(files: List<ShellHandler.FileInfo>) {
        suRecursiveCopyFilesToDocument(context!!, files, _uri!!)
    }

    @Throws(FileNotFoundException::class)
    fun listFiles(): List<StorageFile> {

        try {
            exists()
        } catch (e: Throwable) {
            throw FileNotFoundException("File $_uri does not exist")
        }

        path?.let { path ->

            cacheGetFiles(path) ?: run {
                file?.let { dir ->
                    cacheSetFiles(
                        path,
                        dir.listFiles()?.map { child ->
                            StorageFile(this, child)
                        }?.toMutableList() ?: mutableListOf()
                    )
                } ?: run {
                    context?.contentResolver?.let { resolver ->
                        val childrenUri = try {
                            DocumentsContract.buildChildDocumentsUriUsingTree(
                                this._uri,
                                DocumentsContract.getDocumentId(this._uri)
                            )
                        } catch (e: IllegalArgumentException) {
                            return listOf()
                        }
                        val results = mutableListOf<StorageFile>()
                        var cursor: Cursor? = null
                        try {
                            cursor = resolver.query(
                                // "For type TreeDocumentFile, getName() is implemented as
                                // a query to DocumentsProvider which is.... slow."
                                // so avoid getName by using COLUMN_DISPLAY_NAME
                                // (also add name to constructor and allow setting the name field)
                                childrenUri, arrayOf(
                                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                                ),
                                null, null, null
                            )
                            var documentUri: Uri
                            while (cursor?.moveToNext() == true) {
                                documentUri =
                                    DocumentsContract.buildDocumentUriUsingTree(
                                        this._uri,
                                        cursor.getString(0)
                                    )
                                results.add(
                                    if (OABX.prefFlag(PREFS_COLUMNNAMESAF, true))
                                        StorageFile(this, context, documentUri, cursor.getString(1))
                                    else
                                        StorageFile(this, context, documentUri)
                                )
                            }
                        } catch (e: Throwable) {
                            LogsHandler.unhandledException(e, _uri)
                        } finally {
                            closeQuietly(cursor)
                        }
                        cacheSetFiles(path, results)
                    }
                }
            }
            return cacheGetFiles(path) ?: listOf()
        } ?: return listOf()
    }

    fun ensureDirectory(dirName: String): StorageFile {
        return findFile(dirName)
            ?: createDirectory(dirName)
    }

    fun deleteRecursive(): Boolean = when {
        isFile ->
            delete()
        isDirectory -> try {
            val contents = listFiles()
            var result = true
            contents.forEach { file ->
                result = result && file.deleteRecursive()
            }
            if (result)
                delete()
            else
                result
        } catch (e: FileNotFoundException) {
            false
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
            false
        }
        else -> false
    }

    companion object {
        //TODO hg42 manage file trees instead of single files and let StorageFile and caches use them
        private var fileListCache =
            mutableMapOf<String, MutableList<StorageFile>>() //TODO hg42 access should automatically checkCache
        private var uriStorageFileCache = mutableMapOf<String, StorageFile>()
        private var invalidateFilters = mutableListOf<(String) -> Boolean>()

        fun fromUri(context: Context, uri: Uri): StorageFile {
            // Todo: Figure out what's wrong with the Uris coming from the intent and why they need to be processed
            //  with DocumentsContract.buildDocumentUriUsingTree(value, DocumentsContract.getTreeDocumentId(value)) first
            if (OABX.prefFlag(PREFS_CACHEURIS, false)) {
                return StorageFile(
                    null,
                    context,
                    uri
                )
            } else {
                cacheCheck()
                val id = uri.toString()
                return cacheGetUri(id)
                    ?: StorageFile(
                        null,
                        context,
                        uri
                    ).also { cacheSetUri(id, it) }
            }
        }

        fun createFile(context: Context, uri: Uri, mimeType: String, displayName: String): Uri? {
            return try {
                DocumentsContract.createDocument(
                    context.contentResolver,
                    uri,
                    mimeType,
                    displayName
                )
            } catch (e: FileNotFoundException) {
                null
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e, uri)
                null
            }
        }

        fun invalidateCache(filter: (String) -> Boolean) {
            if (OABX.prefFlag(PREFS_INVALIDATESELECTIVE, true)) {
                invalidateFilters.add(filter)
                cacheCheck() //TODO
            } else {
                invalidateCache()
            }
        }

        fun invalidateCache() {
            invalidateFilters = mutableListOf({ true })
            cacheCheck() //TODO
        }

        fun cacheInvalidate(storageFile: StorageFile) {
            storageFile.path?.let { path -> invalidateCache { it.startsWith(path) } }
        }

        fun cacheGetFiles(id: String): List<StorageFile>? {
            cacheCheck()
            return fileListCache[id]
        }

        fun cacheSetFiles(id: String, files: List<StorageFile>) {
            fileListCache[id] = files.toMutableList()
        }

        private fun cacheGetUri(id: String): StorageFile? {
            cacheCheck()
            return uriStorageFileCache[id]
        }

        fun cacheSetUri(id: String, file: StorageFile) {
            uriStorageFileCache[id] = file
        }

        fun cacheFilesAdd(path: String, file: StorageFile) {
            fileListCache[path]?.run {
                add(file)
            } ?: run {
                fileListCache[path] = mutableListOf(file)
            }
        }

        fun cacheFilesRemove(path: String, file: StorageFile?) {
            file?.let {
                fileListCache[path]?.run {
                    remove(file)
                } ?: run {
                    fileListCache.remove(path)
                }
            } ?: fileListCache.remove(path)
        }

        private fun cacheCheck() {
            try {
                while (invalidateFilters.size > 0) {
                    invalidateFilters.removeFirst().let { isInvalid ->
                        fileListCache =
                            fileListCache.toMap().filterNot { isInvalid(it.key) }.toMutableMap()
                        uriStorageFileCache =
                            uriStorageFileCache.toMap().filterNot { isInvalid(it.key) }
                                .toMutableMap()
                    }
                }
            } catch (e: Throwable) {
                logException(e)
            }
        }

        private fun closeQuietly(closeable: AutoCloseable?) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (rethrown: RuntimeException) {
                    // noinspection ProhibitedExceptionThrown
                    throw rethrown
                } catch (e: Throwable) {
                    LogsHandler.unhandledException(e)
                }
            }
        }
    }
}
