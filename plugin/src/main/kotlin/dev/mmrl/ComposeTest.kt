package dev.mmrl

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.window.DialogProperties
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions

class ComposeTest(wxOptions: WXOptions) : WXInterface(wxOptions) {
    override var name = "stub"

    private fun init() {
        val act = activity as? ComponentActivity ?: return

        // Delay execution until the view is fully initialized
        post {
            // Remove the WebView from its parent
            (parent as? ViewGroup)?.removeView(this)

            // Add a full-screen ComposeView instead
            act.addContentView(
                ComposeView(act).apply {
                    setContent {
                        var showDialog by remember { mutableStateOf(true) }

                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                confirmButton = {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("OK")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("Cancel")
                                    }
                                },
                                title = {
                                    Text("Dialog Title")
                                },
                                text = {
                                    Text("This is a simple Material 3 alert dialog.")
                                },
                                properties = DialogProperties(dismissOnClickOutside = true)
                            )
                        }
                    }
                },
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    init {
        init()
    }
}
