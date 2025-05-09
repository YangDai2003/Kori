package kfile

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

actual class PlatformFile(
    val context: Context,
    val uri: Uri,
    internal val documentFile: DocumentFile? = DocumentFile.fromSingleUri(context, uri)
)

actual fun PlatformFile.exists(): Boolean {
    return documentFile?.exists() == true
}

actual suspend fun PlatformFile.readText(): String {
    documentFile?.let { documentFile ->
        if (documentFile.exists() && documentFile.canRead() && documentFile.isFile) {
            return context.contentResolver.openInputStream(uri)?.bufferedReader()
                ?.use { it.readText() } ?: ""
        }
    }
    return ""
}

actual fun PlatformFile.getFileName(): String {
    return documentFile?.let { documentFile ->
        documentFile.name ?: ""
    } ?: ""
}

actual fun PlatformFile.getPath(): String {
    return uri.toString()
}

actual fun PlatformFile.isDirectory(): Boolean {
    return documentFile?.isDirectory == true
}

actual fun PlatformFile.getExtension(): String {
    return documentFile?.let { documentFile ->
        documentFile.name?.substringAfterLast('.', "") ?: ""
    } ?: ""
}

actual suspend fun PlatformFile.writeText(text: String) {
    documentFile?.let { documentFile ->
        if (documentFile.exists() && documentFile.canWrite() && documentFile.isFile) {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()
                ?.use { it.write(text) }
        }
    }
}

actual suspend fun PlatformFile.delete(): Boolean {
    return documentFile?.let { documentFile ->
        if (documentFile.exists()) documentFile.delete()
        else false
    } == true
}

actual fun PlatformFile.getLastModified(): Instant {
    val milliSeconds = documentFile?.lastModified() ?: 0L
    if (milliSeconds == 0L) return Clock.System.now()
    return Instant.fromEpochMilliseconds(milliSeconds)
}