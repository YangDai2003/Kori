package org.yangdai.kori.presentation.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.ClipEntry
import kfile.normalizeFileName
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.datetime.toNSDate
import org.yangdai.kori.data.local.entity.NoteEntity
import org.yangdai.kori.data.local.entity.NoteType
import platform.CoreGraphics.CGBitmapContextCreate
import platform.CoreGraphics.CGBitmapContextCreateImage
import platform.CoreGraphics.CGColorSpaceCreateDeviceRGB
import platform.CoreGraphics.CGImageAlphaInfo
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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

@OptIn(ExperimentalTime::class)
actual fun formatInstant(instant: Instant): String {
    return dateTimeFormatter.stringFromDate(instant.toNSDate())
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

@OptIn(ExperimentalComposeUiApi::class)
actual fun clipEntryOf(string: String): ClipEntry {
    return ClipEntry.withPlainText(string)
}

@OptIn(ExperimentalForeignApi::class)
@Suppress("CAST_NEVER_SUCCEEDS")
@Composable
actual fun Modifier.clickToShareFile(noteEntity: NoteEntity): Modifier {
    val tempDir = NSTemporaryDirectory()
    val isMarkdown = noteEntity.noteType == NoteType.MARKDOWN
    val fileName = noteEntity.title.normalizeFileName() + if (isMarkdown) ".md" else ".txt"
    val tempDirURL = NSURL.fileURLWithPath(tempDir, isDirectory = true)
    val fileURL = tempDirURL.URLByAppendingPathComponent(fileName) ?: return this
    val nsString = noteEntity.content as NSString
    val success = nsString.writeToURL(
        url = fileURL,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null
    )
    if (!success) return this
    return clickable {
        val activityViewController = UIActivityViewController(listOf(fileURL), null)
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            activityViewController, animated = true, completion = null
        )
    }
}

actual fun shouldShowLanguageSetting() = true

@Composable
actual fun Modifier.clickToLanguageSetting(): Modifier {
    return clickable {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)

        if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(settingsUrl, mapOf<Any?, Any?>(), null)
        }
    }
}

fun Color.toUIColor(): UIColor {
    return UIColor.colorWithRed(
        red = red.toDouble(),
        green = green.toDouble(),
        blue = blue.toDouble(),
        alpha = alpha.toDouble()
    )
}

fun Int.toUIColor(): UIColor {
    val red = (this shr 16 and 0xFF) / 255.0
    val green = (this shr 8 and 0xFF) / 255.0
    val blue = (this and 0xFF) / 255.0
    val alpha = (this shr 24 and 0xFF) / 255.0
    return UIColor.colorWithRed(red, green, blue, alpha)
}

@OptIn(ExperimentalForeignApi::class)
fun ImageBitmap.toUIImage(): UIImage? {
    val width = this.width
    val height = this.height
    val bytes = this.asSkiaBitmap().readPixels() ?: return null
    // BGRA to RGBA
    for (i in 0 until bytes.size step 4) {
        val b = bytes[i]
        val g = bytes[i + 1]
        val r = bytes[i + 2]
        bytes[i] = r
        bytes[i + 1] = g
        bytes[i + 2] = b
    }

    val colorSpace = CGColorSpaceCreateDeviceRGB()
    val context = CGBitmapContextCreate(
        data = bytes.usePinned { it.addressOf(0) },
        width = width.toULong(),
        height = height.toULong(),
        bitsPerComponent = 8u,
        bytesPerRow = (4 * width).toULong(),
        space = colorSpace,
        bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
    )

    val cgImage = CGBitmapContextCreateImage(context)
    return cgImage?.let { UIImage.imageWithCGImage(it) }
}