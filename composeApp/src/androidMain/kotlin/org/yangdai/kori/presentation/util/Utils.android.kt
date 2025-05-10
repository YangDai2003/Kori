package org.yangdai.kori.presentation.util

import android.content.ClipData
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.text.NumberFormat
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import org.yangdai.kori.R
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import java.io.File
import java.util.Date

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

@Composable
fun rememberCustomTabsIntent(): CustomTabsIntent {
    return remember {
        CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
    }
}

private val dateTimeFormatter by lazy {
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
}

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