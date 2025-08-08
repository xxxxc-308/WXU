package dev.mmrl.module

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenPackageManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenUserManager
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.moshi
import dev.mmrl.internal.WXUInterface
import dev.mmrl.model.WXApplicationInfo
import dev.mmrl.model.WXApplicationInfo.Companion.toWXApplicationInfo
import dev.mmrl.util.getDrawableBase64
import dev.mmrl.util.toJsonString

class PackageManager(wxOptions: WXOptions) : WXUInterface(wxOptions) {
    override var name = "pm"

    private val pm get(): HiddenPackageManager = PlatformManager.packageManager
    private val um get(): HiddenUserManager = PlatformManager.userManager

    @JavascriptInterface
    fun getApplicationInfo(packageName: String): String = getApplicationInfo(packageName, 0)

    @JavascriptInterface
    fun getApplicationInfo(packageName: String, flags: Int): String =
        getApplicationInfo(packageName, flags, um.myUserId)

    @JavascriptInterface
    fun getApplicationInfo(packageName: String, flags: Int, userId: Int): String {
        try {
            val ai = pm.getPackageInfo(packageName, flags, userId)
            val info = ai.toWXApplicationInfo(context)
            val adapter = moshi.adapter(WXApplicationInfo::class.java)
            return adapter.toJson(info) ?: "{}"
        } catch (e: Exception) {
            console.error(e)
            return "{}"
        }
    }

    // ####

    @JavascriptInterface
    fun getInstalledPackages(): String = getInstalledPackages(0)

    @JavascriptInterface
    fun getInstalledPackages(flags: Int): String = getInstalledPackages(flags, um.myUserId)

    @JavascriptInterface
    fun getInstalledPackages(flags: Int, userId: Int): String {
        try {
            val ip = pm.getInstalledPackages(flags, userId)
            val list = ip.map { it.packageName }
            return list.toJsonString()
        } catch (e: Exception) {
            console.error(e)
            return "{}"
        }
    }

    // ####

    @JavascriptInterface
    fun getApplicationIcon(
        packageName: String,
    ): String? = getApplicationIcon(packageName, 0)

    @JavascriptInterface
    fun getApplicationIcon(
        packageName: String,
        flags: Int,
    ): String? = getApplicationIcon(packageName, flags, um.myUserId)

    @JavascriptInterface
    fun getApplicationIcon(
        packageName: String,
        flags: Int,
        userId: Int,
    ): String? {
        try {
            val info = pm.getApplicationInfo(packageName, flags, userId)

            return getDrawableBase64(
                drawable = info.loadIcon(context.packageManager)
            )
        } catch (e: Exception) {
            console.error(e)
            return null
        }
    }
}