package dev.mmrl.internal

import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions

open class WXUInterface(wxOptions: WXOptions) : WXInterface(wxOptions) {
    open val names: List<String> by lazy { listOf("wx:$name", name) }
    open val minVersion: Long = -1L
}