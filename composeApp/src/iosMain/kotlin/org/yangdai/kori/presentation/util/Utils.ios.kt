package org.yangdai.kori.presentation.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.Instant
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun Modifier.clickToShareText(text: String): Modifier = clickable {
    val activityViewController = UIActivityViewController(listOf(text), null)
    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
        activityViewController, animated = true, completion = null
    )
}

private val dateTimeFormatter by lazy {
    NSDateFormatter().apply {
        dateStyle = NSDateFormatterMediumStyle
        timeStyle = NSDateFormatterShortStyle
    }
}

actual fun formatInstant(instant: Instant): String {
    val date = NSDate(instant.toEpochMilliseconds() / 1000.0)
    return dateTimeFormatter.stringFromDate(date)
}

private val numberFormatter by lazy {
    NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterDecimalStyle
    }
}

actual fun formatNumber(int: Int): String {
    val nsNumber = NSNumber(int)
    return numberFormatter.stringFromNumber(nsNumber) ?: int.toString()
}