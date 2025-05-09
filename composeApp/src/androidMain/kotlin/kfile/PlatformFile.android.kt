package kfile

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException

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
        if (documentFile.canRead()) {
            return try {
                context.contentResolver.openInputStream(uri)?.bufferedReader()
                    ?.use { it.readText() } ?: ""
            } catch (_: FileNotFoundException) {
                ""
            }
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
        documentFile.type?.substringAfterLast("/") ?: ""
    } ?: ""
}