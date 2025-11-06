package org.yangdai.kori.presentation.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import org.yangdai.kori.data.local.entity.NoteEntity
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

@Composable
actual fun Modifier.clickToShareText(text: String): Modifier = clickable {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(text)
    clipboard.setContents(selection, selection)
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
        instant.toString()
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

@OptIn(ExperimentalComposeUiApi::class)
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