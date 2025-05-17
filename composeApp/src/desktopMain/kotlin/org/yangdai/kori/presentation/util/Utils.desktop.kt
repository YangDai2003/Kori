package org.yangdai.kori.presentation.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import org.yangdai.kori.data.local.entity.NoteEntity
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

@Composable
actual fun Modifier.clickToShareText(text: String): Modifier = clickable {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(text)
    clipboard.setContents(selection, selection)
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
    return ClipEntry(string)
}

@Composable
actual fun Modifier.clickToShareFile(noteEntity: NoteEntity): Modifier {
    return this
}

actual fun shouldShowLanguageSetting(): Boolean = false

@Composable
actual fun Modifier.clickToLanguageSetting(): Modifier {
    return this
}