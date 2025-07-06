@file:Suppress("unused")

package dev.mmrl

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import dev.mmrl.module.FileSystem
import dev.mmrl.module.Module
import dev.mmrl.module.Process
import dev.mmrl.module.Reflection
import kotlin.reflect.KClass

class Global(wxOptions: WXOptions) : WXInterface(wxOptions) {
    override var name = "global"

    @JavascriptInterface
    fun require(module: String): Any? {
        if (module.startsWith("wx:")) {
            return wxRequire(module.removePrefix("wx:"))
        }

        console.error("Unknown module: $module")
        return null
    }

    private val modules: List<Pair<String, Class<out WXInterface>>> = listOf(
        "fs" to FileSystem::class.java,
        "reflect" to Reflection::class.java,
        "module" to Module::class.java,
        "process" to Process::class.java,
    )

    private fun wxRequire(module: String): Any? {
        for ((name, clazz) in modules) {
            if (module == name) {
                return clazz.getDeclaredConstructor(WXOptions::class.java).newInstance(wxOptions)
            }

            continue
        }

        console.error("Unknown wx module: $module")
        return null
    }
}