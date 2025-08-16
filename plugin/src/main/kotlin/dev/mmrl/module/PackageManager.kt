package dev.mmrl.module

import android.R.attr.mimeType
import android.graphics.Bitmap
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebResourceResponse
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.PlatformManager.context
import com.dergoogler.mmrl.platform.hiddenApi.HiddenPackageManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenUserManager
import com.dergoogler.mmrl.webui.PathHandler
import com.dergoogler.mmrl.webui.ResponseStatus
import com.dergoogler.mmrl.webui.forbiddenResponse
import com.dergoogler.mmrl.webui.headers
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import com.dergoogler.mmrl.webui.moshi
import com.dergoogler.mmrl.webui.notFoundResponse
import dev.mmrl.Global.Companion.packageManagerConfig
import dev.mmrl.internal.WXUInterface
import dev.mmrl.model.WXApplicationInfo
import dev.mmrl.model.WXApplicationInfo.Companion.toWXApplicationInfo
import dev.mmrl.util.getDrawableBase64
import dev.mmrl.util.getProp
import dev.mmrl.util.toBitmap
import dev.mmrl.util.toJsonString
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.System.console

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
    fun getApplicationInfo(packageName: String, flags: Int, userId: Int): String =
        getApplicationInfo0(pm, packageName, flags, userId)

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

    companion object {
        fun WXInterface.packagePathHandler(): PathHandler {
            val packageNameRegex = Regex("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+")
            return handler@{ path ->

                if (path.matches(Regex("test.txt"))) {
                    return@handler forbiddenResponse
                }

                val match = packageNameRegex.find(path)
                if (match != null) {
                    try {
                        val packageName = match.value
                        val subPath = path.removePrefix(packageName)
                        return@handler packageNamePathHandler(packageName)(subPath)
                    } catch (e: IOException) {
                        Log.e("packagePathHandler", "Error opening mmrl asset path: $path", e)
                    }
                }
                notFoundResponse
            }
        }

        private val WXInterface.quality: Int
            get() {
                val q = packageManagerConfig.getProp<Int?>("quality", 100)
                if (q == null) return 100
                if (q < 0 || q > 100) return 100
                return q
            }

        private fun WXInterface.packageNamePathHandler(packageName: String): PathHandler {
            val pm = PlatformManager.packageManager
            val um = PlatformManager.userManager
            val iconRegex = Regex("^/icon\\.(png|jpe?g|webp)$")
            val infoRegex = Regex("^/info\\.json$")

            return handler@{ path ->
                try {
                    if (iconRegex.matches(path)) {
                        val info = pm.getApplicationInfo(packageName, 0, um.myUserId)
                        val drawable = info.loadIcon(context.packageManager)

                        val bitmap = drawable.toBitmap()

                        val extension = path.substringAfterLast('.', "png").lowercase()
                        val mimeType = "image/$extension"

                        val format = when (extension) {
                            "png" -> Bitmap.CompressFormat.PNG
                            "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
                            "webp" -> Bitmap.CompressFormat.WEBP
                            else -> Bitmap.CompressFormat.PNG
                        }

                        val stream = ByteArrayOutputStream()
                        bitmap.compress(format, quality, stream)
                        val inputStream = ByteArrayInputStream(stream.toByteArray())

                        return@handler WebResourceResponse(
                            mimeType,
                            null,
                            ResponseStatus.OK.code,
                            ResponseStatus.OK.reasonPhrase,
                            headers,
                            inputStream
                        )
                    }

                    if (infoRegex.matches(path)) {
                        val info = getApplicationInfo0(pm, packageName, 0, um.myUserId)
                        val `is` = ByteArrayInputStream(info.toByteArray())

                        return@handler WebResourceResponse(
                            "application/json",
                            "UTF-8",
                            ResponseStatus.OK.code,
                            ResponseStatus.OK.reasonPhrase,
                            headers,
                            `is`
                        )
                    }

                    notFoundResponse
                } catch (e: Exception) {
                    Log.e("packageNamePathHandler", "Error opening mmrl asset path: $path", e)
                    notFoundResponse
                }
            }
        }

        private fun WXInterface.getApplicationInfo0(
            packageManager: HiddenPackageManager,
            packageName: String,
            flags: Int,
            userId: Int,
        ): String {
            try {
                val ai = packageManager.getPackageInfo(packageName, flags, userId)
                val info = ai.toWXApplicationInfo(context)
                val adapter = moshi.adapter(WXApplicationInfo::class.java)

                val prettyPrint = packageManagerConfig.getProp("prettyPrintJson", false)

                val json = if (prettyPrint) {
                    adapter.indent(" ".repeat(2)).toJson(info)
                } else {
                    adapter.toJson(info)
                }

                return json ?: "{}"
            } catch (e: Exception) {
                console.error(e)
                return "{}"
            }
        }
    }
}