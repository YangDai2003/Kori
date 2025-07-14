import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

val os: OperatingSystem = OperatingSystem.current()
val arch: String = System.getProperty("os.arch")
val isAarch64: Boolean = arch.contains("aarch64")

val platform =
    when {
        os.isWindows -> "win"
        os.isMacOsX -> "mac"
        else -> "linux"
    } + if (isAarch64) "-aarch64" else ""

kotlin {
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.documentfile)
            implementation(libs.androidx.browser)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.compose.backhandler)
            implementation(libs.compose.adaptive)
            implementation(libs.compose.adaptive.navigation)
            implementation(libs.compose.adaptive.layout)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            implementation(libs.androidx.dataStore)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.sqlite.bundled)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.content.negotiation)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
            val javafxVersion = "21.0.7"
            implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-media:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-web:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-swing:$javafxVersion:$platform")
        }
    }
}

android {
    namespace = "org.yangdai.kori"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.yangdai.kori"
        minSdk = 29
        targetSdk = 36
        versionCode = 8
        versionName = "1.0.8"
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }
    androidResources {
        @Suppress("UnstableApiUsage")
        //noinspection MissingResourcesProperties
        generateLocaleConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                rootProject.file("proguard-rules.pro")
            )
        }

    }
    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val abi = output.filters.find { it.filterType == "ABI" }?.identifier ?: "universal"
                val outputFileName =
                    "Kori-android-$abi-${variant.versionName}-${variant.baseName}.apk"
                output.outputFileName = outputFileName
            }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}
room {
    schemaDirectory("$projectDir/schemas")
}

compose.desktop {
    application {
        mainClass = "org.yangdai.kori.MainKt"
        buildTypes.release.proguard {
//            configurationFiles.from(rootProject.file("compose-desktop.pro"))
            isEnabled = false
        }
        nativeDistributions {
            macOS {
                appCategory = "public.app-category.productivity"
                bundleID = "org.yangdai.kori"
                jvmArgs(
                    "-Dapple.awt.application.appearance=system"
                )
                infoPlist {
                    extraKeysRawXml = """
                    <key>CFBundleDocumentTypes</key>
                    <array>
                        <dict>
                            <key>CFBundleTypeName</key>
                            <string>Plain Text Document</string>
                            <key>LSItemContentTypes</key>
                            <array>
                                <string>public.plain-text</string>
                            </array>
                            <key>CFBundleTypeExtensions</key>
                            <array>
                                <string>txt</string>
                            </array>
                            <key>CFBundleTypeRole</key>
                            <string>Editor</string>
                            <key>LSHandlerRank</key>
                            <string>Alternate</string>
                        </dict>
                        <dict>
                            <key>CFBundleTypeName</key>
                            <string>Markdown Document</string>
                            <key>LSItemContentTypes</key>
                            <array>
                                <!-- UTI for Markdown -->
                                <string>net.daringfireball.markdown</string>
                                <!-- Fallback for generic text, if needed, but net.daringfireball.markdown is standard -->
                                <!-- <string>public.text</string> -->
                            </array>
                            <key>CFBundleTypeExtensions</key>
                            <array>
                                <string>md</string>
                                <string>markdown</string>
                            </array>
                            <key>CFBundleTypeRole</key>
                            <string>Editor</string>
                            <key>LSHandlerRank</key>
                            <string>Alternate</string>
                        </dict>
                    </array>
                    <key>NSServices</key>
                    <array>
                        <dict>
                            <key>NSMenuItem</key>
                            <dict>
                                <key>default</key>
                                <string>Send to Kori</string> <!-- 显示在菜单中的名称 -->
                            </dict>
                            <key>NSMessage</key>
                            <string>handleServiceAction</string>
                            <key>NSPortName</key>
                            <string>${bundleID}</string>
                            
                            <!-- 处理选中的文本 -->
                            <key>NSSendTypes</key>
                            <array>
                                <string>public.plain-text</string> <!-- 接收纯文本 -->
                            </array>
                            
                            <!-- 处理文件 (使其也出现在文件的 "共享" 菜单中) -->
                            <key>NSSendFileTypes</key>
                            <array>
                                <string>public.plain-text</string> <!-- .txt files -->
                                <string>net.daringfireball.markdown</string> <!-- .md files -->
                            </array>

                            <key>NSRequiredContext</key>
                            <dict>
                                <key>NSTextContent</key> <string>YES</string>
                            </dict>
                        </dict>
                    </array>
                    """.trimIndent()
                }
//                iconFile.set(project.file("icon.icns"))
            }
            windows {
                shortcut = true
                dirChooser = true
//                iconFile.set(project.file("icon.ico"))
            }
            linux {
                debMaintainer = "https://github.com/YangDai2003"
                appCategory = "Utility"
//                iconFile.set(project.file("icon.png"))
            }
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Pkg,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm
            )
            packageName = "Kori"
            packageVersion = "1.0.8"
            description = "Compose Multiplatform App"
            licenseFile.set(rootProject.file("LICENSE.txt"))
        }
    }
}
