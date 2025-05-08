package org.yangdai.kori

sealed class Platform {
    abstract val name: String
    abstract val version: String
    abstract val deviceModel: String

    // 定义所有可能的平台类型
    object Android : Platform() {
        override val name: String = "Android"
        override val version: String get() = getPlatformVersion()
        override val deviceModel: String get() = getDeviceModel()
    }

    object IOS : Platform() {
        override val name: String = "iOS"
        override val version: String get() = getPlatformVersion()
        override val deviceModel: String get() = getDeviceModel()
    }

    object Desktop : Platform() {
        override val name: String = "Desktop"
        override val version: String get() = getPlatformVersion()
        override val deviceModel: String get() = getDeviceModel()
    }

    object Web : Platform() {
        override val name: String = "Web"
        override val version: String get() = getPlatformVersion()
        override val deviceModel: String get() = getDeviceModel()
    }
}

expect fun getPlatformVersion(): String
expect fun getDeviceModel(): String

expect fun currentPlatform(): Platform

enum class OS {
    IOS,
    ANDROID,
    LINUX,
    MACOS,
    WINDOWS,
    UNKNOWN,
    WEB
}

expect fun currentOperatingSystem(): OS