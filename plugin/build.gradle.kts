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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    kotlinOptions {
        jvmTarget = "1.8"
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
    compileOnly("com.github.MMRLApp:WebUI-X-Portable:v61")
    compileOnly("com.github.MMRLApp.MMRL:platform:v33773")
}

val androidHome: String? = System.getenv("ANDROID_HOME")
    ?: System.getenv("ANDROID_SDK_ROOT")

val d8Bin = "$androidHome/build-tools/34.0.0/d8"
val buildDir: File = project.layout.buildDirectory.get().asFile

val classesJar = buildDir.resolve("intermediates/aar_main_jar/release/syncReleaseLibJars/classes.jar")
val dexOutput = buildDir.resolve("classes.dex")

fun d8(vararg args: String) {
    exec {
        commandLine(d8Bin, *args)
    }
}

tasks.register("build-dex") {
    dependsOn("build")

    doLast {
        if (!File(d8Bin).canExecute()) {
            file(d8Bin).setExecutable(true)
        }

        d8("--output=${buildDir.absolutePath}", classesJar.absolutePath)

        println("DEX file created at: $dexOutput")
    }
}
