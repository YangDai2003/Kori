package kfile

import java.io.File
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

actual class PlatformFile(val file: File)

actual fun PlatformFile.exists(): Boolean = file.exists()

actual suspend fun PlatformFile.readText(): String {
    return if (file.exists() && file.canRead() && file.isFile) file.readText() else ""
}

actual fun PlatformFile.getFileName(): String = file.name

actual fun PlatformFile.getPath(): String = file.path

actual fun PlatformFile.isDirectory(): Boolean = file.isDirectory

actual fun PlatformFile.getExtension(): String = file.extension

actual suspend fun PlatformFile.writeText(text: String) {
    if (file.exists() && file.canWrite() && file.isFile) file.writeText(text)
}

actual suspend fun PlatformFile.delete(): Boolean = file.delete()

@OptIn(ExperimentalTime::class)
actual fun PlatformFile.getLastModified(): Instant {
    val milliSeconds = file.lastModified()
    if (milliSeconds == 0L) return Clock.System.now()
    return Instant.fromEpochMilliseconds(milliSeconds)
}