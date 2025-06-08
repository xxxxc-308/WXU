package dev.mmrl

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions

class Module(wxOptions: WXOptions) : WXInterface(wxOptions) {

    @get:JavascriptInterface
    val id get() = modId.toString()

}