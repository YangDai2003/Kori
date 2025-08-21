package org.yangdai.kori.presentation.util

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.text.NumberFormat
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.yangdai.kori.R
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import java.io.File
import java.util.Date
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Composable
actual fun Modifier.clickToShareText(text: String): Modifier {
    val context = LocalContext.current
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, context.getString(R.string.app_name))
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return clickable {
        val chooserIntent = Intent.createChooser(sendIntent, null)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}

private val dateTimeFormatter by lazy {
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
}

@OptIn(ExperimentalTime::class)
actual fun formatInstant(instant: Instant): String {
    return try {
        val date = Date.from(instant.toJavaInstant())
        dateTimeFormatter.format(date)
    } catch (_: Exception) {
        instant.toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Formats.ISO)
    }
}

private val numberFormatter by lazy {
    NumberFormat.getNumberInstance()
}

actual fun formatNumber(int: Int): String {
    return try {
        numberFormatter.format(int)
    } catch (_: IllegalArgumentException) {
        int.toString()
    }
}

actual fun clipEntryOf(string: String): ClipEntry {
    val clipData = ClipData.newPlainText("Kori", string)
    return ClipEntry(clipData)
}

@Composable
actual fun Modifier.clickToShareFile(noteEntity: NoteEntity): Modifier {
    val context = LocalContext.current.applicationContext
    val isMarkdown = noteEntity.noteType == NoteType.MARKDOWN
    val fileName = noteEntity.title.trim().replace(" ", "_").replace("/", "_").replace(":", "_") +
            if (isMarkdown) ".md" else ".txt"
    val file = File(context.cacheDir, fileName)
    file.writeText(noteEntity.content)
    val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/*"
        putExtra(Intent.EXTRA_STREAM, fileUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return clickable {
        val chooserIntent = Intent.createChooser(sendIntent, null)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}

@Stable
data class SharedContent(
    val title: String = "",
    val text: String = "",
    val type: NoteType = NoteType.PLAIN_TEXT,
    val uri: Uri? = null
)

private fun Uri.getMimeType(context: Context): String? = context.contentResolver.getType(this)
private fun String?.isTextMimeType(): Boolean = this?.startsWith("text/") == true
private fun String?.isMarkdownMimeType(): Boolean =
    this == "text/markdown" || this == "application/x-markdown" || this == "text/x-markdown" || this == "text/html"

private fun String?.isMarkdownFileName(): Boolean =
    this?.lowercase()?.let {
        it.contains(".md") || it.contains(".markdown") || it.contains(".mkd")
                || it.contains(".mdwn") || it.contains(".mdown") || it.contains(".mdtxt")
                || it.contains(".mdtext") || it.contains(".html")
    } == true

private fun getFileName(context: Context, uri: Uri): String? {
    if ("content" == uri.scheme) {
        val docFile = DocumentFile.fromSingleUri(context, uri)
        return docFile?.name
    }
    if ("file" == uri.scheme) {
        return uri.lastPathSegment
    }
    return null
}

private fun Uri.getWritableUri(context: Context): Uri? {
    return try {
        val docFile = DocumentFile.fromSingleUri(context, this)
        if (docFile != null && docFile.exists() && docFile.isFile
            && docFile.canWrite() && docFile.canRead()
        ) {
            this
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

private fun readTextFromUri(context: Context, uri: Uri): String? {
    if (uri.scheme == "content" || uri.scheme == "file") {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }
        } catch (e: SecurityException) {
            System.err.println("SecurityException reading URI: $uri - $e")
            null // Insufficient permissions
        } catch (e: Exception) {
            System.err.println("Exception reading URI: $uri - $e")
            null // Other read errors
        }
    }
    return null
}

internal inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(name) as? T
    }

fun Intent.parseSharedContent(context: Context): SharedContent {
    return when (action) {
        Intent.ACTION_SEND -> parseActionSend(context)
        Intent.ACTION_VIEW, Intent.ACTION_EDIT -> parseActionViewEdit(context)
        else -> SharedContent() // Default for unhandled or unknown actions
    }
}

private fun Intent.parseActionSend(context: Context): SharedContent {
    var title = getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()
    var text = getStringExtra(Intent.EXTRA_TEXT).orEmpty()
    var noteType = NoteType.PLAIN_TEXT

    // Check for a single shared stream (e.g., sharing a file)
    val streamUri = getParcelableExtraCompat<Uri>(Intent.EXTRA_STREAM)
    if (streamUri != null) {
        val streamFileName = getFileName(context, streamUri).orEmpty()
        if (title.isEmpty()) title = streamFileName

        val mimeType = type ?: streamUri.getMimeType(context)
        // 判断是否为markdown
        if (mimeType.isMarkdownMimeType() || streamFileName.isMarkdownFileName()) {
            noteType = NoteType.MARKDOWN
        }

        // If EXTRA_TEXT is empty, try to get text from the stream if it's a text type
        if (text.isEmpty()) {
            if (mimeType.isTextMimeType()) {
                text = readTextFromUri(context, streamUri).orEmpty()
            }
            if (text.isEmpty()) {
                text = "Shared file: ${streamFileName.ifEmpty { streamUri.toString() }}"
            }
        } else {
            val streamText = readTextFromUri(context, streamUri)
            text += if (streamText != null) "\n\n--- Attached File: $streamFileName ---\n$streamText"
            else "\n\n(Attached file: $streamFileName)"
        }
    }
    return SharedContent(title, text, noteType)
}

private fun Intent.parseActionViewEdit(context: Context): SharedContent {
    var title = ""
    var text = ""
    var writableUri: Uri? = null
    var type = NoteType.PLAIN_TEXT

    data?.let { uri ->
        title = getFileName(context, uri).orEmpty()
        val effectiveMimeType = this.type ?: uri.getMimeType(context)
        // 判断是否为markdown
        if (effectiveMimeType.isMarkdownMimeType() || title.isMarkdownFileName()) {
            type = NoteType.MARKDOWN
        }
        if (effectiveMimeType.isTextMimeType()) {
            text = readTextFromUri(context, uri).orEmpty()
        }
        writableUri = uri.getWritableUri(context)
    }
    return SharedContent(title, text, type, uri = writableUri)
}

actual fun shouldShowLanguageSetting(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}

@SuppressLint("InlinedApi")
@Composable
actual fun Modifier.clickToLanguageSetting(): Modifier {
    val context = LocalContext.current.applicationContext
    return clickable {
        try {
            val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                context.startActivity(intent)
            } catch (_: Exception) {
            }
        }
    }
}