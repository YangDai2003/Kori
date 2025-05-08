package org.yangdai.kori

actual fun currentPlatform(): Platform = Platform.Desktop

actual fun currentOperatingSystem(): OS {
    val osName = System.getProperty("os.name")?.lowercase() // Get OS name and convert to lowercase
        ?: return OS.UNKNOWN // Handle null case
    return when {
        osName.contains("win") -> OS.WINDOWS
        osName.contains("mac") || osName.contains("darwin") -> OS.MACOS // "darwin" is the kernel name for macOS
        osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> OS.LINUX // Common Unix/Linux identifiers
        else -> OS.UNKNOWN
    }
}

actual fun getPlatformVersion(): String = System.getProperty("os.version") ?: "N/A"
actual fun getDeviceModel(): String = System.getProperty("os.name") ?: "N/A"