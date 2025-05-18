package org.yangdai.kori

import android.os.Build

actual val currentPlatformInfo: PlatformInfo = PlatformInfo(
    platform = Platform.Android,
    version = Build.VERSION.RELEASE.orEmpty(),
    deviceModel = Build.MODEL.orEmpty(),
    operatingSystem = OS.ANDROID
)