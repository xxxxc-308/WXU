@file:Suppress("unused")

package dev.mmrl.module

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions

class Process(wxOptions: WXOptions) : WXInterface(wxOptions) {
    @get:JavascriptInterface
    val getPlatform = options.platform.name

    @get:JavascriptInterface
    val isAlive: Boolean = options.isProviderAlive

    @get:JavascriptInterface
    val applicationLibraryDir: String? = context.applicationInfo.nativeLibraryDir
}