package dev.mmrl.module.pty

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions
import dev.mmrl.internal.pty.PtyImpl

class Instance(
    private val impl: PtyImpl,
    private var currentCols: Int,
    private var currentRows: Int,
    wxOptions: WXOptions,
) : WXInterface(wxOptions) {
    @JavascriptInterface
    fun resize(cols: Int, rows: Int) {
        currentCols = cols
        currentRows = rows
        impl.resize(cols, rows)
    }

    @JavascriptInterface
    fun write(data: ByteArray) {
        try {
            impl.write(data)
        } catch (e: Exception) {
            console.trace("Error writing to shell")
            console.error(e)
        }
    }

    @JavascriptInterface
    fun write(data: String) {
        this.write(data.toByteArray())
    }

    @JavascriptInterface
    fun kill() {
        try {
            impl.kill()
        } catch (e: Exception) {
            console.trace("Error killing shell")
            console.error(e)
        }
    }
}