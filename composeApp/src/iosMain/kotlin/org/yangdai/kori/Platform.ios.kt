package org.yangdai.kori

import platform.UIKit.UIDevice

actual val currentPlatformInfo: PlatformInfo = PlatformInfo(
    platform = Platform.IOS,
    version = UIDevice.currentDevice.systemVersion,
    deviceModel = UIDevice.currentDevice.model,
    operatingSystem = OS.IOS
)