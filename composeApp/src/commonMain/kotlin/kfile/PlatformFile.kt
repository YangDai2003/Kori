package kfile

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

expect class PlatformFile

expect fun PlatformFile.exists(): Boolean

expect suspend fun PlatformFile.readText(): String

expect fun PlatformFile.getFileName(): String

expect fun PlatformFile.getPath(): String

expect fun PlatformFile.isDirectory(): Boolean

expect fun PlatformFile.getExtension(): String

expect suspend fun PlatformFile.writeText(text: String)

expect suspend fun PlatformFile.delete(): Boolean

@OptIn(ExperimentalTime::class)
expect fun PlatformFile.getLastModified(): Instant

@OptIn(ExperimentalTime::class)
fun String.normalizeFileName(): String = this.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
    .ifBlank { "file_${Clock.System.now().toEpochMilliseconds()}" }