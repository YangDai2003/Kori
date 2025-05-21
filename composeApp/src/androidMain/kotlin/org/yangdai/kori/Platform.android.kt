package org.yangdai.kori

import android.os.Build

actual val currentPlatformInfo: PlatformInfo = PlatformInfo(
    platform = Platform.Android,
    version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Build.VERSION.RELEASE_OR_CODENAME else Build.VERSION.RELEASE,
    deviceModel = Build.MODEL.orEmpty(),
    operatingSystem = OS.ANDROID
)