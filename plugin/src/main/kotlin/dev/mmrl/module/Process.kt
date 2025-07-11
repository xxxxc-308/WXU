@file:Suppress("unused")

package dev.mmrl.module

import android.os.Process
import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import dev.mmrl.internal.WXUInterface

class Process(wxOptions: WXOptions) : WXUInterface(wxOptions) {
    override var name = "process"

    @JavascriptInterface
    fun myPid() = Process.myPid()

    @JavascriptInterface
    fun killProcess(pid: Int) = Process.killProcess(pid)

    @JavascriptInterface
    fun myUid() = Process.myUid()

    @get:JavascriptInterface
    val platform = options.platform.name

    @get:JavascriptInterface
    val isAlive: Boolean = options.isProviderAlive

    @get:JavascriptInterface
    val applicationLibraryDir: String? = context.applicationInfo.nativeLibraryDir
}