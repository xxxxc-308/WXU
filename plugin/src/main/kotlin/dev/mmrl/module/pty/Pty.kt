@file:Suppress("unused")

package dev.mmrl.module.pty

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import dev.mmrl.internal.WXUInterface
import dev.mmrl.internal.pty.PtyImpl
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class Pty(wxOptions: WXOptions) : WXUInterface(wxOptions) {
    private var instance: Instance? = null
    private var currentCols = 80
    private var currentRows = 24

    override var name = "pty"
    override val minVersion: Long = 213

    @JavascriptInterface
    fun start(sh: String, argsJson: String?, envJson: String?): Instance? {
        return start(sh, argsJson ?: "[]", envJson ?: "{}", currentCols, currentRows)
    }

    @JavascriptInterface
    fun start(sh: String, argsJson: String, envJson: String, cols: Int, rows: Int): Instance? {
        var args = emptyArray<String?>()
        var env = emptyArray<String?>()

        if (isJsonArray(argsJson)) {
            args = parseArgs(argsJson)
        } else {
            console.error("Invalid args JSON: $argsJson")
        }

        if (isJsonObject(envJson)) {
            env = parseEnv(envJson)
        } else {
            console.error("Invalid env JSON: $envJson")
        }

        val ins: PtyImpl? = PtyImpl.start(sh, args, env, cols, rows)

        if (ins == null) {
            console.error("Failed to start pty")
            return null
        }

        val listener = object : PtyImpl.EventListener {
            override fun onData(data: ByteArray?) {
                activity?.runOnUiThread {
                    try {
                        val str = data?.decodeToString()
                        webView.postWXEvent(EVENT_NAME_DATA, str)
                    } catch (e: Exception) {
                        webView.postWXEvent(EVENT_NAME_DATA, data?.toHexString())
                    }
                }
            }

            override fun onExit(exitCode: Int) {
                activity?.runOnUiThread {
                    webView.postWXEvent(EVENT_NAME_EXIT, exitCode)
                }
            }
        }

        ins.setEventListener(listener)

        instance = Instance(
            impl = ins,
            currentCols = cols,
            currentRows = rows,
            wxOptions = wxOptions
        )

        return instance
    }

    override fun onActivityDestroy() {
        super.onActivityDestroy()
        instance?.kill()
    }

    // Helper function for binary data fallback
    private fun ByteArray.toHexString(): String {
        return this.joinToString("") { "%02x".format(it) }
    }

    private fun isJsonArray(str: String): Boolean {
        return try {
            JSONArray(str)
            true
        } catch (_: JSONException) {
            false
        }
    }

    private fun isJsonObject(str: String): Boolean {
        return try {
            JSONObject(str)
            true
        } catch (_: JSONException) {
            false
        }
    }

    private fun parseArgs(json: String): Array<String?> {
        val jsonArray = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list.toTypedArray()
    }

    private fun parseEnv(json: String): Array<String?> {
        val jsonObj = JSONObject(json)
        val list = mutableListOf<String>()
        val keys = jsonObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObj.getString(key)
            list.add("$key=$value")
        }
        return list.toTypedArray()
    }

    companion object {
        const val EVENT_NAME_DATA = "pty-data"
        const val EVENT_NAME_EXIT = "pty-exit"
        const val DEFAULT_COLS = 80
        const val DEFAULT_ROWS = 24
    }
}