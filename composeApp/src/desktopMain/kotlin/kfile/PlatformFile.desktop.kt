package kfile

import java.io.File

actual class PlatformFile(
    val file: File
)

actual fun PlatformFile.exists(): Boolean {
    return file.exists()
}

actual suspend fun PlatformFile.readText(): String {
    return file.readText()
}

actual fun PlatformFile.getFileName(): String {
    return file.name
}

actual fun PlatformFile.getPath(): String {
    return file.path
}

actual fun PlatformFile.isDirectory(): Boolean {
    return file.isDirectory
}

actual fun PlatformFile.getExtension(): String {
    return file.extension
}