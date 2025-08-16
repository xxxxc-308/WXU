@file:Suppress("unused")

package dev.mmrl

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.platform.file.SuFile.Companion.toSuFile
import com.dergoogler.mmrl.webui.handler.suPathHandler
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import dev.mmrl.internal.WXUInterface
import dev.mmrl.module.Dialog
import dev.mmrl.module.FileSystem
import dev.mmrl.module.Module
import dev.mmrl.module.PackageManager
import dev.mmrl.module.PackageManager.Companion.packagePathHandler
import dev.mmrl.module.Process
import dev.mmrl.module.Reflection
import dev.mmrl.util.getProp

class Global(wxOptions: WXOptions) : WXInterface(wxOptions) {
    override var name = "global"

    private val modules: List<Class<out WXUInterface>> = listOf(
        FileSystem::class.java,
        Reflection::class.java,
        Module::class.java,
        Process::class.java,
        Dialog::class.java,
        PackageManager::class.java
    )

    @JavascriptInterface
    fun require(module: String): Any? {
        for (clazz in modules) {
            val instance =
                clazz.getDeclaredConstructor(WXOptions::class.java).newInstance(wxOptions)
            if (instance.names.any { it == module }) {
                return instance
            }
            continue
        }

        console.error("Unknown module: $module")
        return null
    }

    val isAllowUrlPackageFetchEnabled = packageManagerConfig.getProp("allowUrlPackageFetch", false)

    override val assetHandlers = buildList {
        if (isAllowUrlPackageFetchEnabled) {
            add("/.package/" to packagePathHandler())
        }
    }

    companion object {
        val WXInterface.packageManagerConfig
            get() = config.extra.getProp<Map<String, Any?>?>(
                "pm",
                null
            )
    }
}