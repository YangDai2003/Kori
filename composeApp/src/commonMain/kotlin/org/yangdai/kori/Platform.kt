package org.yangdai.kori

data class PlatformInfo(
    val version: String,
    val deviceModel: String,
    val operatingSystem: OS
)

expect val currentPlatformInfo: PlatformInfo

fun PlatformInfo.isDesktop(): Boolean {
    return operatingSystem == OS.LINUX || operatingSystem == OS.MACOS || operatingSystem == OS.WINDOWS
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