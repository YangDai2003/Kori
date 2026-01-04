package kfile

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

actual class PlatformFile(
    val context: Context,
    val uri: Uri,
    internal val documentFile: DocumentFile = DocumentFile.fromSingleUri(context, uri)
        ?: throw IllegalArgumentException("Invalid URI")
)

actual fun PlatformFile.exists(): Boolean = documentFile.exists()

actual suspend fun PlatformFile.readText(): String {
    return if (documentFile.exists() && documentFile.canRead() && documentFile.isFile)
        context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() } ?: ""
    else ""
}

actual fun PlatformFile.getFileName(): String = documentFile.name ?: ""

actual fun PlatformFile.getPath(): String = uri.toString()

actual fun PlatformFile.isDirectory(): Boolean = documentFile.isDirectory

actual fun PlatformFile.getExtension(): String =
    documentFile.name?.substringAfterLast('.', "") ?: ""

actual suspend fun PlatformFile.writeText(text: String) {
    context.contentResolver.openOutputStream(uri, "wt")?.use { it.bufferedWriter().write(text) }
}

actual suspend fun PlatformFile.delete(): Boolean = documentFile.delete()

@OptIn(ExperimentalTime::class)
actual fun PlatformFile.getLastModified(): Instant {
    val milliSeconds = documentFile.lastModified()
    if (milliSeconds == 0L) return Clock.System.now()
    return Instant.fromEpochMilliseconds(milliSeconds)
}