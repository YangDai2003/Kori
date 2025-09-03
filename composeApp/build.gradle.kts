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
    alias(libs.plugins.composeHotReload)
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

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.documentfile)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.glance)
            implementation(libs.androidx.glance.appwidget)
            implementation(libs.androidx.webkit)
        }
        commonMain.dependencies {
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("org.jetbrains.compose.material3:material3:1.10.0-alpha01") {
                exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-datetime")
            }

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)

            implementation(libs.compose.backhandler)
            implementation(libs.compose.adaptive)
            implementation(libs.compose.adaptive.navigation)
            implementation(libs.compose.adaptive.layout)

            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.paging)
            implementation(libs.androidx.dataStore)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.sqlite.bundled)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)

            implementation(libs.coil.compose)
            implementation(libs.koog.agents)
        }
        iosMain.dependencies {}
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            val javafxVersion = "21.0.8"
            //noinspection UseTomlInstead,NewerVersionAvailable
            implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
            //noinspection NewerVersionAvailable,UseTomlInstead
            implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
            //noinspection NewerVersionAvailable,UseTomlInstead
            implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
            //noinspection NewerVersionAvailable,UseTomlInstead
            implementation("org.openjfx:javafx-media:$javafxVersion:$platform")
            //noinspection NewerVersionAvailable,UseTomlInstead
            implementation("org.openjfx:javafx-web:$javafxVersion:$platform")
            //noinspection NewerVersionAvailable,UseTomlInstead
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
        versionCode = 111
        versionName = "1.1.1"
    }
    splits {
        abi {
            // Detect app bundle and conditionally disable split abi
            // This is needed due to a "Sequence contains more than one matching element" error
            // present since AGP 8.9.0, for more info see:
            // https://issuetracker.google.com/issues/402800800

            // AppBundle tasks usually contain "bundle" in their name
            val isBuildingBundle =
                //noinspection WrongGradleMethod
                gradle.startParameter.taskNames.any { it.lowercase().contains("bundle") }

            // Disable when building appBundle
            isEnable = !isBuildingBundle
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
            excludes += "META-INF/INDEX.LIST"
            /*
            6 files found with path 'META-INF/INDEX.LIST' from inputs:
 - io.netty:netty-handler:4.1.118.Final/netty-handler-4.1.118.Final.jar
 - io.netty:netty-transport-native-unix-common:4.1.118.Final/netty-transport-native-unix-common-4.1.118.Final.jar
 - io.netty:netty-codec:4.1.118.Final/netty-codec-4.1.118.Final.jar
 - io.netty:netty-transport:4.1.118.Final/netty-transport-4.1.118.Final.jar
 - io.netty:netty-resolver:4.1.118.Final/netty-resolver-4.1.118.Final.jar
 - io.netty:netty-buffer:4.1.118.Final/netty-buffer-4.1.118.Final.jar
Adding a packaging block may help, please refer to
https://developer.android.com/reference/tools/gradle-api/com/android/build/api/dsl/Packaging
for more information
             */
            excludes += "META-INF/io.netty.versions.properties"
            /*
            7 files found with path 'META-INF/io.netty.versions.properties' from inputs:
 - io.netty:netty-handler:4.1.118.Final/netty-handler-4.1.118.Final.jar
 - io.netty:netty-transport-native-unix-common:4.1.118.Final/netty-transport-native-unix-common-4.1.118.Final.jar
 - io.netty:netty-codec:4.1.118.Final/netty-codec-4.1.118.Final.jar
 - io.netty:netty-transport:4.1.118.Final/netty-transport-4.1.118.Final.jar
 - io.netty:netty-resolver:4.1.118.Final/netty-resolver-4.1.118.Final.jar
 - io.netty:netty-buffer:4.1.118.Final/netty-buffer-4.1.118.Final.jar
 - io.netty:netty-common:4.1.118.Final/netty-common-4.1.118.Final.jar
Adding a packaging block may help, please refer to
https://developer.android.com/reference/tools/gradle-api/com/android/build/api/dsl/Packaging
for more information
             */
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
    add("kspJvm", libs.androidx.room.compiler)
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
            modules("jdk.unsupported", "jdk.unsupported.desktop", "java.net.http", "jdk.jsobject")
            macOS {
                appCategory = "public.app-category.productivity"
                bundleID = "org.yangdai.kori"
                jvmArgs(
                    "-Dapple.awt.application.appearance=system"
                )
                infoPlist {
                    extraKeysRawXml = macExtraPlistKeys
                }
                iconFile.set(project.file("icon.icns"))
            }
            windows {
                shortcut = true
                upgradeUuid = "80612DFA-3040-4C8D-A96A-9CB32176D0DA"
                iconFile.set(project.file("icon.ico"))
            }
            linux {
                debMaintainer = "https://github.com/YangDai2003"
                appCategory = "Utility"
                iconFile.set(project.file("icon.png"))
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
            packageVersion = "1.1.1"
            description = "Compose Multiplatform App"
            licenseFile.set(rootProject.file("LICENSE.txt"))
        }
    }
}

val macExtraPlistKeys: String
    get() = """
    <key>CFBundleURLTypes</key>
    <array>
        <dict>
            <key>CFBundleURLName</key>
            <string>Kori deep link</string>
            <key>CFBundleURLSchemes</key>
            <array>
                <string>kori</string>
            </array>
        </dict>
    </array>
    <key>CFBundleDocumentTypes</key>
	<array>
		<dict>
			<key>CFBundleTypeExtensions</key>
			<array>
				<string>md</string>
				<string>markdown</string>
			</array>
			<key>CFBundleTypeIconSystemGenerated</key>
			<true/>
			<key>CFBundleTypeName</key>
			<string>Markdown</string>
			<key>CFBundleTypeRole</key>
			<string>Editor</string>
			<key>LSHandlerRank</key>
			<string>Default</string>
			<key>LSItemContentTypes</key>
			<array>
				<string>net.daringfireball.markdown</string>
			</array>
		</dict>
		<dict>
			<key>CFBundleTypeExtensions</key>
			<array>
				<string>txt</string>
			</array>
			<key>CFBundleTypeIconSystemGenerated</key>
			<true/>
			<key>CFBundleTypeName</key>
			<string>纯文本</string>
			<key>CFBundleTypeRole</key>
			<string>Editor</string>
			<key>LSHandlerRank</key>
			<string>Default</string>
			<key>LSItemContentTypes</key>
			<array>
				<string>public.plain-text</string>
			</array>
		</dict>
		<dict>
			<key>CFBundleTypeExtensions</key>
			<array>
				<string>html</string>
				<string>htm</string>
			</array>
			<key>CFBundleTypeIconSystemGenerated</key>
			<true/>
			<key>CFBundleTypeName</key>
			<string>HTML</string>
			<key>CFBundleTypeRole</key>
			<string>Editor</string>
			<key>LSHandlerRank</key>
			<string>Default</string>
			<key>LSItemContentTypes</key>
			<array>
				<string>public.html</string>
			</array>
		</dict>
	</array>
    """
