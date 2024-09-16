package com.machiav3lli.backup.plugins

import com.machiav3lli.backup.tracePlugin
import java.io.File

class InternalShellScriptPlugin(file: File) : TextPlugin(file) {

    init {
        tracePlugin { "${this::class.simpleName} $name <- ${file.name}" } //TODO hg42
    }

    companion object : PluginCompanion {

        override fun klass() = InternalShellScriptPlugin::class
        override fun register() = registerType(name(), Companion, listOf("internal_sh"))
        override fun create(file: File): Plugin? = InternalShellScriptPlugin(file)

        fun findScript(name: String) =
            try {
                (Plugin.getEnabled(name) as InternalShellScriptPlugin).file
            } catch (e: Throwable) {
                null
            }
    }
}
