@file:Suppress("unused")

package dev.mmrl.module

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.platform.model.ModId.Companion.adbDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.configDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleConfigDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.moduleDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.modulesDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.propFile
import com.dergoogler.mmrl.platform.model.ModId.Companion.systemDir
import com.dergoogler.mmrl.platform.model.ModId.Companion.webrootDir
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import dev.mmrl.internal.WXUInterface

class Module(wxOptions: WXOptions) : WXUInterface(wxOptions) {
    override var name = "module"

    @get:JavascriptInterface
    val id: String = modId.toString()

    @get:JavascriptInterface
    val adbDir: String = modId.adbDir.path

    @get:JavascriptInterface
    val configDir: String = modId.configDir.path

    @get:JavascriptInterface
    val moduleConfigDir: String = modId.moduleConfigDir.path

    @get:JavascriptInterface
    val modulesDir: String = modId.modulesDir.path

    @get:JavascriptInterface
    val moduleDir: String = modId.moduleDir.path

    @get:JavascriptInterface
    val webRootDir: String = modId.webrootDir.path

    @get:JavascriptInterface
    val systemDir: String = modId.systemDir.path

    @get:JavascriptInterface
    val propFile: String = modId.propFile.path
}
