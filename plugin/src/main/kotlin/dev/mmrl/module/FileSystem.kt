@file:Suppress("unused")

package dev.mmrl.module

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions

import dev.mmrl.internal.FileSystemInstance
import dev.mmrl.internal.WXUInterface


class FileSystem(wxOptions: WXOptions) : WXUInterface(wxOptions) {
    override var name = "fs"

    @JavascriptInterface
    fun newInstance(path: String): FileSystemInstance? {
        try {
            val file = SuFile(path)
            return FileSystemInstance(file, wxOptions)
        } catch (e: Exception) {
            console.error(e)
            return null
        }
    }

    @JavascriptInterface
    fun readTextSync(path: String): String? = newInstance(path)?.readTextSync()

    @JavascriptInterface
    fun writeTextSync(path: String, text: String): Unit? = newInstance(path)?.writeTextSync(text)

    @JavascriptInterface
    fun listSync(path: String): String? = newInstance(path)?.listSync()

    @JavascriptInterface
    fun listSync(path: String, delimiter: String): String? = newInstance(path)?.listSync(delimiter)

    @JavascriptInterface
    fun existsSync(path: String): Boolean = newInstance(path)?.existsSync() ?: false

    @JavascriptInterface
    fun isFileSync(path: String): Boolean = newInstance(path)?.isFileSync() ?: false

    @JavascriptInterface
    fun isDirectorySync(path: String): Boolean = newInstance(path)?.isDirectorySync() ?: false

    @JavascriptInterface
    fun isSymlinkSync(path: String): Boolean = newInstance(path)?.isSymlinkSync() ?: false

    @JavascriptInterface
    fun isBlockSync(path: String): Boolean = newInstance(path)?.isBlockSync() ?: false

    @JavascriptInterface
    fun isNamedPipeSync(path: String): Boolean = newInstance(path)?.isNamedPipeSync() ?: false

    @JavascriptInterface
    fun isCharacterSync(path: String): Boolean = newInstance(path)?.isCharacterSync() ?: false

    @JavascriptInterface
    fun isHiddenSync(path: String): Boolean = newInstance(path)?.isHiddenSync() ?: false

    @JavascriptInterface
    fun isSocketSync(path: String): Boolean = newInstance(path)?.isSocketSync() ?: false

    @JavascriptInterface
    fun mkdirSync(path: String): Boolean = newInstance(path)?.mkdirSync() ?: false

    @JavascriptInterface
    fun mkdirsSync(path: String): Boolean = newInstance(path)?.mkdirsSync() ?: false

    @JavascriptInterface
    fun rmSync(path: String): Boolean = newInstance(path)?.rmSync() ?: false

    @JavascriptInterface
    fun sizeSync(path: String): Long? = newInstance(path)?.sizeSync()

    @JavascriptInterface
    fun renameSync(path: String, newPath: String): Boolean =
        newInstance(path)?.renameSync(newPath) ?: false

    @JavascriptInterface
    fun copyFileSync(path: String, dest: String) = newInstance(path)?.copyFileSync(dest)

    @JavascriptInterface
    fun copyFileSync(path: String, dest: String, overwrite: Boolean) =
        newInstance(path)?.copyFileSync(dest, overwrite)

    @JavascriptInterface
    fun statSync(path: String): Long? = newInstance(path)?.statSync()

    @JavascriptInterface
    fun createNewFileSync(path: String): Boolean = newInstance(path)?.createNewFileSync() ?: false

    @JavascriptInterface
    fun canExecuteSync(path: String): Boolean = newInstance(path)?.canExecuteSync() ?: false

    @JavascriptInterface
    fun canWriteSync(path: String): Boolean = newInstance(path)?.canWriteSync() ?: false

    @JavascriptInterface
    fun canReadSync(path: String): Boolean = newInstance(path)?.canReadSync() ?: false

    @JavascriptInterface
    fun chownSync(path: String, uid: Int, gid: Int): Boolean =
        newInstance(path)?.chownSync(uid, gid) ?: false

    @JavascriptInterface
    fun chmodSync(path: String, mode: Int): Boolean = newInstance(path)?.chmodSync(mode) ?: false
}