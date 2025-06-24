package kfile

import java.io.File
import java.nio.file.Files
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

actual class PlatformFile(
    val file: File
)

actual fun PlatformFile.exists(): Boolean {
    return file.exists()
}

actual suspend fun PlatformFile.readText(): String {
    return if (file.exists() && file.canRead() && file.isFile) file.readText() else ""
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

actual suspend fun PlatformFile.writeText(text: String) {
    if (file.exists() && file.canWrite() && file.isFile) file.writeText(text)
}

actual suspend fun PlatformFile.delete(): Boolean {
    return Files.deleteIfExists(file.toPath())
}

@OptIn(ExperimentalTime::class)
actual fun PlatformFile.getLastModified(): Instant {
    return Instant.fromEpochMilliseconds(file.lastModified())
}