package org.yangdai.kori.presentation.util

import android.content.Intent
import android.icu.text.DateFormat
import android.icu.text.NumberFormat
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import org.yangdai.kori.R
import java.util.Date

@Composable
actual fun Modifier.clickToShareText(text: String): Modifier {
    val context = LocalContext.current
    return clickable {
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.setType("text/plain")
        sendIntent.putExtra(
            Intent.EXTRA_TITLE, context.getString(R.string.app_name)
        )
        sendIntent.putExtra(
            Intent.EXTRA_TEXT, text
        )
        context.startActivity(Intent.createChooser(sendIntent, null))
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