plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.mmrl"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        multiDexEnabled = false

        ndk {
            abiFilters += arrayOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_STL=c++_static",
                    "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"
                )
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/jni/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isShrinkResources = false
            multiDexEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    packaging.resources.excludes += setOf(
        "META-INF/**",
        "okhttp3/**",
        "kotlin/**",
        "org/**",
        "**.properties",
        "**.bin",
        "**/*.proto"
    )
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    compileOnly(composeBom)

    compileOnly("androidx.activity:activity-compose:1.9.0")
    compileOnly("androidx.compose.ui:ui:1.6.7")
    compileOnly("androidx.compose.material3:material3:1.2.1")

    compileOnly(libs.webui.x.portable)
    compileOnly(libs.mmrl.platform)
}

// Optional D8 build task — unchanged
val androidHome: String? = System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")

val isWindows = System.getProperty("os.name").lowercase().contains("win")

val d8Bin = androidHome?.let {
    File(it, "build-tools/35.0.0/d8" + if (isWindows) ".bat" else "").absolutePath
}

val adbBin = androidHome?.let {
    File(it, "platform-tools/adb" + if (isWindows) ".exe" else "").absolutePath
}

val buildDir: File = project.layout.buildDirectory.get().asFile

val classesJar =
    buildDir.resolve("intermediates/aar_main_jar/release/syncReleaseLibJars/classes.jar")
val classesOutput = buildDir.resolve("classes.dex")
val dexOutput = buildDir.resolve("wxu.dex")

fun d8(vararg args: String) {
    if (d8Bin == null) {
        error("ANDROID_HOME or ANDROID_SDK_ROOT not set. Cannot locate d8.")
    }
    exec {
        commandLine(d8Bin, *args)
    }
}

fun adb(vararg args: String) {
    if (adbBin == null) {
        error("ANDROID_HOME or ANDROID_SDK_ROOT not set. Cannot locate adb.")
    }
    exec {
        commandLine(adbBin, *args)
    }
}

fun findLatestSoFile(): File? {
    return buildDir.resolve("intermediates/cxx/Debug").walkTopDown()
        .filter { it.isFile && it.name == "libnative.so" && "arm64-v8a" in it.path }
        .maxByOrNull { it.lastModified() }
}

tasks.register("build-dex") {
    doFirst {
        classesOutput.delete()
        dexOutput.delete()
    }

    dependsOn("build")

    doLast {
        if (d8Bin == null) {
            println("Skipping build-dex: ANDROID_HOME or ANDROID_SDK_ROOT not set.")
            return@doLast
        }

        val d8File = File(d8Bin)
        if (!d8File.exists()) {
            println("Skipping build-dex: d8 not found at $d8Bin")
            return@doLast
        }

        if (!d8File.canExecute()) {
            d8File.setExecutable(true)
        }

        d8("--output", buildDir.absolutePath, classesJar.absolutePath)

        if (classesOutput.renameTo(dexOutput)) {
            println("DEX file created at: $dexOutput")
            println("Pushing DEX file to device...")
            adb(
                "push",
                dexOutput.absolutePath,
                "/data/adb/modules/mmrl_wpd/webroot/plugins/wxu.dex"
            )
        }

        val latestSo = findLatestSoFile()
        if (latestSo != null) {
            val copyTo = buildDir.resolve("libnative.so")
            copyTo.parentFile.mkdirs()
            latestSo.copyTo(copyTo, overwrite = true)
            println("Copied .so to: $copyTo")
            adb(
                "push",
                latestSo.absolutePath,
                "/data/adb/modules/mmrl_wpd/webroot/shared/libnative.so"
            )
        } else {
            println("No .so file found in intermediates.")
        }
    }
}
