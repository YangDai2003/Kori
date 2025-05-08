package org.yangdai.kori

import platform.UIKit.UIDevice

actual fun currentPlatform(): Platform = Platform.IOS
actual fun currentOperatingSystem(): OS = OS.IOS

actual fun getPlatformVersion(): String = UIDevice.currentDevice.systemVersion
actual fun getDeviceModel(): String = UIDevice.currentDevice.model