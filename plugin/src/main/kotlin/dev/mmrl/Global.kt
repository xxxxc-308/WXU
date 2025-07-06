@file:Suppress("unused")

package dev.mmrl

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import dev.mmrl.module.FileSystem
import dev.mmrl.module.Module
import dev.mmrl.module.Process
import dev.mmrl.module.Reflection

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

    private fun wxRequire(module: String): Any? {
        when (module) {
            "fs" -> return FileSystem(wxOptions)
            "reflect" -> return Reflection(wxOptions)
            "module" -> return Module(wxOptions)
            "process" -> return Process(wxOptions)
        }

        console.error("Unknown wx module: $module")
        return null
    }
}