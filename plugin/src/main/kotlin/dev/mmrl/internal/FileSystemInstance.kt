@file:Suppress("unused")

package dev.mmrl.internal

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions

class FileSystemInstance(
    private val file: SuFile,
    wxOptions: WXOptions,
) : WXInterface(wxOptions) {

    @JavascriptInterface
    fun readTextSync(): String? = file.withCatching { readText() }

    @JavascriptInterface
    fun writeTextSync(text: String): Unit? = file.withCatching { writeText(text) }

    @JavascriptInterface
    fun listSync(): String? = listSync(",")

    @JavascriptInterface
    fun listSync(delimiter: String): String? =
        file.withCatching { list()?.joinToString(delimiter) }

    @JavascriptInterface
    fun existsSync(): Boolean = file.withCatching(false) { exists() }

    @JavascriptInterface
    fun isFileSync(): Boolean = file.withCatching(false) { isFile() }

    @JavascriptInterface
    fun isDirectorySync(): Boolean = file.withCatching(false) { isDirectory() }

    @JavascriptInterface
    fun isSymlinkSync(): Boolean = file.withCatching(false) { isSymlink() }

    @JavascriptInterface
    fun isBlockSync(): Boolean = file.withCatching(false) { isBlock() }

    @JavascriptInterface
    fun isNamedPipeSync(): Boolean = file.withCatching(false) { isNamedPipe() }

    @JavascriptInterface
    fun isCharacterSync(): Boolean = file.withCatching(false) { isCharacter() }

    @JavascriptInterface
    fun isHiddenSync(): Boolean = file.withCatching(false) { isHidden() }

    @JavascriptInterface
    fun isSocketSync(): Boolean = file.withCatching(false) { isSocket() }

    @JavascriptInterface
    fun mkdirSync(): Boolean = file.withCatching(false) { mkdir() }

    @JavascriptInterface
    fun mkdirsSync(): Boolean = file.withCatching(false) { mkdirs() }

    @JavascriptInterface
    fun rmSync(): Boolean = file.withCatching(false) { delete() }

    @JavascriptInterface
    fun sizeSync(): Long? = file.withCatching { length() }

    @JavascriptInterface
    fun renameSync(newPath: String): Boolean =
        file.withCatching(false) { renameTo(SuFile(newPath)) }

    @JavascriptInterface
    fun copyFileSync(dest: String) = copyFileSync(dest, false)

    @JavascriptInterface
    fun copyFileSync(dest: String, overwrite: Boolean) =
        file.withCatching { copyTo(SuFile(dest), overwrite) }

    @JavascriptInterface
    fun statSync(): Long? = file.withCatching { lastModified() }

    @JavascriptInterface
    fun createNewFileSync(): Boolean = file.withCatching(false) { createNewFile() }

    @JavascriptInterface
    fun canExecuteSync(): Boolean = file.withCatching(false) { canExecute() }

    @JavascriptInterface
    fun canWriteSync(): Boolean = file.withCatching(false) { canWrite() }

    @JavascriptInterface
    fun canReadSync(): Boolean = file.withCatching(false) { canRead() }

    @JavascriptInterface
    fun chownSync(uid: Int, gid: Int): Boolean = file.withCatching(false) { setOwner(uid, gid) }

    @JavascriptInterface
    fun chmodSync(mode: Int): Boolean = file.withCatching(false) { setPermissions(mode) }

    private fun <T> SuFile.withCatching(block: SuFile.() -> T): T? = withCatching(null, block)
    private fun <T> SuFile.withCatching(default: T, block: SuFile.() -> T): T =
        with(this) {
            try {
                block()
            } catch (e: Exception) {
                console.error(e)
                default
            }
        }
}