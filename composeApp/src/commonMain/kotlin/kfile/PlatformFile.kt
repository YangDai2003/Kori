package kfile

import org.yangdai.kori.data.local.entity.NoteType
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

expect class PlatformFile

expect fun PlatformFile.exists(): Boolean

expect val PlatformFile.fileName: String

expect val PlatformFile.path: String

expect val PlatformFile.isDirectory: Boolean

expect val PlatformFile.extension: String

expect suspend fun PlatformFile.readText(): String

expect suspend fun PlatformFile.writeText(text: String)

expect suspend fun PlatformFile.delete(): Boolean

@OptIn(ExperimentalTime::class)
expect fun PlatformFile.lastModified(): Instant

@OptIn(ExperimentalTime::class)
fun String.normalizeFileName(): String = this.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
    .ifBlank { "file_${Clock.System.now().toEpochMilliseconds()}" }

val PlatformFile.suitableNoteType: NoteType
    get() = if (
        listOf("md", "markdown", "mkd", "mdwn", "mdown", "mdtxt", "mdtext", "html")
            .any { e -> this.extension.lowercase().contains(e) }
    ) NoteType.MARKDOWN
    else if (this.extension.lowercase().contains("txt")) {
        if (this.fileName.contains("todo", ignoreCase = true)) NoteType.TODO
        else NoteType.PLAIN_TEXT
    } else NoteType.PLAIN_TEXT