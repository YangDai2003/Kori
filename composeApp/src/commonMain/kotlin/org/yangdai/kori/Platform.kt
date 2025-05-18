package org.yangdai.kori

data class PlatformInfo(
    val platform: Platform,
    val version: String,
    val deviceModel: String,
    val operatingSystem: OS
)

expect val currentPlatformInfo: PlatformInfo

enum class Platform {
    IOS,
    Desktop,
    Android,
    Web
}

enum class OS {
    IOS,
    ANDROID,
    LINUX,
    MACOS,
    WINDOWS,
    UNKNOWN,
    WEB
}