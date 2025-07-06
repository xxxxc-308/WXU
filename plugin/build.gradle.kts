plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 35
    namespace = "dev.mmrl"

    defaultConfig {
        minSdk = 21
        multiDexEnabled = false
    }

    buildTypes {
        release {
            isShrinkResources = false
            multiDexEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
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
    compileOnly(libs.webui.x.portable)
    compileOnly(libs.mmrl.platform)
}

val androidHome: String? = System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")

val isWindows = System.getProperty("os.name").lowercase().contains("win")

val d8Bin = androidHome?.let {
    File(it, "build-tools/35.0.0/d8" + if (isWindows) ".bat" else "").absolutePath
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

tasks.register("build-dex") {
    doFirst {
        if (classesOutput.exists()) {
            classesOutput.delete()
        }
        if (dexOutput.exists()) {
            dexOutput.delete()
        }
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
        }
    }
}
