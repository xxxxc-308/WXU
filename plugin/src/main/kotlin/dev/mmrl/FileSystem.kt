@file:Suppress("unused")

package dev.mmrl

import android.webkit.JavascriptInterface
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.webui.interfaces.WXInterface
import com.dergoogler.mmrl.webui.interfaces.WXOptions

class FileSystem(wxOptions: WXOptions) : WXInterface(wxOptions) {
    override var name = "fs"

    @JavascriptInterface
    fun readTextSync(path: String): String? = path.withFile { readText() }

    @JavascriptInterface
    fun writeTextSync(path: String, text: String): Unit? = path.withFile { writeText(text) }

    @JavascriptInterface
    fun listSync(path: String): String? = listSync(path, ",")

    @JavascriptInterface
    fun listSync(path: String, delimiter: String): String? =
        path.withFile { list()?.joinToString(delimiter) }

    @JavascriptInterface
    fun existsSync(path: String): Boolean? = path.withFile { exists() }

    @JavascriptInterface
    fun isFileSync(path: String): Boolean? = path.withFile { isFile() }

    @JavascriptInterface
    fun isDirectorySync(path: String): Boolean? = path.withFile { isDirectory() }

    @JavascriptInterface
    fun isSymlinkSync(path: String): Boolean? = path.withFile { isSymlink() }

    @JavascriptInterface
    fun isBlockSync(path: String): Boolean? = path.withFile { isBlock() }

    @JavascriptInterface
    fun isNamedPipeSync(path: String): Boolean? = path.withFile { isNamedPipe() }

    @JavascriptInterface
    fun isCharacterSync(path: String): Boolean? = path.withFile { isCharacter() }

    @JavascriptInterface
    fun isHiddenSync(path: String): Boolean? = path.withFile { isHidden() }

    @JavascriptInterface
    fun isSocketSync(path: String): Boolean? = path.withFile { isSocket() }

    @JavascriptInterface
    fun mkdirSync(path: String): Boolean? = path.withFile { mkdir() }

    @JavascriptInterface
    fun mkdirsSync(path: String): Boolean? = path.withFile { mkdirs() }

    @JavascriptInterface
    fun rmSync(path: String): Boolean? = path.withFile { delete() }

    @JavascriptInterface
    fun sizeSync(path: String): Long? = path.withFile { length() }

    @JavascriptInterface
    fun renameSync(oldPath: String, newPath: String): Boolean? =
        oldPath.withFile { renameTo(SuFile(newPath)) }

    @JavascriptInterface
    fun copyFileSync(src: String, dest: String) = copyFileSync(src, dest, false)

    @JavascriptInterface
    fun copyFileSync(src: String, dest: String, overwrite: Boolean) =
        src.withFile { copyTo(SuFile(dest), overwrite) }

    @JavascriptInterface
    fun statSync(path: String): Long? = path.withFile { lastModified() }

    @JavascriptInterface
    fun createNewFileSync(path: String): Boolean? = path.withFile { createNewFile() }

    @JavascriptInterface
    fun canExecuteSync(path: String): Boolean? = path.withFile { canExecute() }

    @JavascriptInterface
    fun canWriteSync(path: String): Boolean? = path.withFile { canWrite() }

    @JavascriptInterface
    fun canReadSync(path: String): Boolean? = path.withFile { canRead() }

    @JavascriptInterface
    fun chownSync(path: String, uid: Int, gid: Int): Boolean = path.withFile(false) { setOwner(uid, gid) }

    @JavascriptInterface
    fun chmodSync(path: String, mode: Int): Boolean = path.withFile(false) { setPermissions(mode) }

    private fun <T> String.withFile(block: SuFile.() -> T): T? = withFile(null, block)
    private fun <T> String.withFile(default: T, block: SuFile.() -> T): T =
        with(SuFile(this)) {
            try {
                block()
            } catch (e: Exception) {
                console.error(e)
                default
            }
        }
}