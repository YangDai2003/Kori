package org.yangdai.kori

import android.os.Build

actual fun currentPlatform(): Platform = Platform.Android
actual fun currentOperatingSystem(): OS = OS.ANDROID

actual fun getPlatformVersion(): String = Build.VERSION.RELEASE ?: "N/A"
actual fun getDeviceModel(): String = Build.MODEL ?: "N/A"