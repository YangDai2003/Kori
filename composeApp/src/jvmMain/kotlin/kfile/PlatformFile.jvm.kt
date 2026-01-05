package kfile

import java.io.File
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

actual class PlatformFile(val file: File)

actual fun PlatformFile.exists(): Boolean = file.exists() && file.isFile

actual val PlatformFile.fileName: String
    get() = file.name

actual val PlatformFile.path: String
    get() = file.path

actual val PlatformFile.isDirectory: Boolean
    get() = file.isDirectory

actual val PlatformFile.extension: String
    get() = file.extension

actual suspend fun PlatformFile.readText(): String {
    return if (file.canRead()) file.readText() else ""
}

actual suspend fun PlatformFile.writeText(text: String) {
    if (file.exists() && file.canWrite()) file.writeText(text)
}

actual suspend fun PlatformFile.delete(): Boolean = file.delete()

@OptIn(ExperimentalTime::class)
actual fun PlatformFile.lastModified(): Instant {
    val milliSeconds = file.lastModified()
    if (milliSeconds == 0L) return Clock.System.now()
    return Instant.fromEpochMilliseconds(milliSeconds)
}