package dev.mmrl

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions

class Process(wxOptions: WXOptions) : WXInterface(wxOptions) {
    override var name: String = "process"

    @JavascriptInterface
    fun platform(): String {
        return options.platform.name
    }

    @get:JavascriptInterface
    val isAlive: Boolean
        get() = options.isProviderAlive
}