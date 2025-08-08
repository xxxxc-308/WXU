package dev.mmrl.model

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.dergoogler.mmrl.webui.interfaces.listToJson
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WXApplicationInfo(
    val packageName: String,
    val name: String?,
    val label: String?,
    val versionName: String?,
    val versionCode: Long,
    val nonLocalizedLabel: String?,
    var appComponentFactory: String? = null,
    var backupAgentName: String? = null,
    var category: Int? = -1,
    var className: String? = null,
    var compatibleWidthLimitDp: Int = 0,
    var compileSdkVersion: Int? = 0,
    var compileSdkVersionCodename: String? = null,
    var dataDir: String? = null,
    var description: String? = null,
    var deviceProtectedDataDir: String? = null,
    var enabled: Boolean = true,
    var flags: Int? = 0,
    var largestWidthLimitDp: Int? = 0,
    var manageSpaceActivityName: String? = null,
    var minSdkVersion: Int? = 0,
    var nativeLibraryDir: String? = null,
    var permission: String? = null,
    var processName: String? = null,
    var publicSourceDir: String? = null,
    var requiresSmallestWidthDp: Int? = 0,
    var sharedLibraryFiles: String? = null,
    var sourceDir: String? = null,
    var splitNames: String? = null,
    var splitPublicSourceDirs: String? = null,
    var splitSourceDirs: String? = null,
    var storageUuid: String? = null,
    var targetSdkVersion: Int? = 0,
    var taskAffinity: String? = null,
    var theme: Int? = 0,
    var uiOptions: Int? = 0,
    var uid: Int = 0,
) {
    companion object {
        fun PackageInfo.toWXApplicationInfo(context: Context): WXApplicationInfo {
            val spm = context.packageManager

            return with(applicationInfo) {
                return@with WXApplicationInfo(
                    packageName = packageName,
                    name = this?.name,
                    versionName = versionName,
                    versionCode = PackageInfoCompat.getLongVersionCode(this@toWXApplicationInfo),
                    label = this?.loadLabel(spm).toString(),
                    nonLocalizedLabel = this?.nonLocalizedLabel?.toString(),
                    appComponentFactory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) this?.appComponentFactory else null,
                    backupAgentName = this?.backupAgentName,
                    category = this?.category,
                    className = this?.className,
                    compileSdkVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) this?.compileSdkVersion else null,
                    compileSdkVersionCodename = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) this?.compileSdkVersionCodename else null,
                    dataDir = this?.dataDir,
                    deviceProtectedDataDir = this?.deviceProtectedDataDir,
                    enabled = this?.enabled == true,
                    flags = this?.flags,
                    largestWidthLimitDp = this?.largestWidthLimitDp,
                    manageSpaceActivityName = this?.manageSpaceActivityName,
                    minSdkVersion = this?.minSdkVersion,
                    nativeLibraryDir = this?.nativeLibraryDir,
                    permission = this?.permission,
                    processName = this?.processName,
                    publicSourceDir = this?.publicSourceDir,
                    requiresSmallestWidthDp = this?.requiresSmallestWidthDp,
                    // sharedLibraryFiles = listToJson(sharedLibraryFiles.toList()),
                    sourceDir = this?.sourceDir,
                    storageUuid = this?.storageUuid?.toString(),
                    targetSdkVersion = this?.targetSdkVersion,
                    taskAffinity = this?.taskAffinity,
                    theme = this?.theme,
                    uiOptions = this?.uiOptions,
                    splitNames = listToJson(splitNames.toList()),
                    splitPublicSourceDirs = listToJson(this?.splitPublicSourceDirs?.toList()),
                    splitSourceDirs = listToJson(this?.splitSourceDirs?.toList()),
                )
            }
        }
    }
}