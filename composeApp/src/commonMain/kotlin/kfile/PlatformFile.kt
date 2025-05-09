package kfile

import kotlinx.datetime.Instant

expect class PlatformFile

expect fun PlatformFile.exists(): Boolean

expect suspend fun PlatformFile.readText(): String

expect fun PlatformFile.getFileName(): String

expect fun PlatformFile.getPath(): String

expect fun PlatformFile.isDirectory(): Boolean

expect fun PlatformFile.getExtension(): String

expect suspend fun PlatformFile.writeText(text: String)

expect suspend fun PlatformFile.delete(): Boolean

expect fun PlatformFile.getLastModified(): Instant