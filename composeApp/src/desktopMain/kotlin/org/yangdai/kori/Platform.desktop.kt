package org.yangdai.kori

actual val currentPlatformInfo: PlatformInfo = PlatformInfo(
    version = System.getProperty("os.name").orEmpty(),
    deviceModel = System.getProperty("os.arch").orEmpty(),
    operatingSystem = currentOperatingSystem()
)

private fun currentOperatingSystem(): OS {
    val osName = System.getProperty("os.name")?.lowercase() // Get OS name and convert to lowercase
        ?: return OS.UNKNOWN // Handle null case
    return when {
        osName.contains("win") -> OS.WINDOWS
        osName.contains("mac") || osName.contains("darwin") -> OS.MACOS // "darwin" is the kernel name for macOS
        osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> OS.LINUX // Common Unix/Linux identifiers
        else -> OS.UNKNOWN
    }
}