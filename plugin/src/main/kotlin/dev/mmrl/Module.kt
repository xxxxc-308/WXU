package dev.mmrl

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

class Module(wxOptions: WXOptions) : WXInterface(wxOptions) {
    override var name: String = "module"

    @get:JavascriptInterface
    val id: String = modId.toString()

    @get:JavascriptInterface
    val adbDir: String = modId.adbDir.path

    @get:JavascriptInterface
    val configDir: String=modId.configDir.path

    @JavascriptInterface
    fun getModuleConfigDir(): String =modId.moduleConfigDir.path

//    @JavascriptInterface
//    fun getModuleConfigDir(vararg paths: String?): String {
//        val file = SuFile(moduleConfigDir, paths)
//        return file.getPath()
//    }

    @get:JavascriptInterface
    val modulesDir: String=modId.modulesDir.path

    @JavascriptInterface
    fun getModuleDir(): String = modId.moduleDir.path

//    @JavascriptInterface
//    fun getModuleDir(vararg paths: String?): String {
//        val file = SuFile(moduleDir, paths)
//        return file.getPath()
//    }

    @get:JavascriptInterface
    val webRootDir: String = modId.webrootDir.path

//    @JavascriptInterface
//    fun getWebRootDir(vararg paths: String?): String {
//        val file = SuFile(webrootDir, paths)
//        return file.getPath()
//    }

    @JavascriptInterface
    fun getSystemDir(): String = modId.systemDir.path

//    @JavascriptInterface
//    fun getSystemDir(vararg paths: String?): String {
//        val file = SuFile(systemDir, paths)
//        return file.getPath()
//    }

    @get:JavascriptInterface
    val propFile: String = modId.propFile.path
}
