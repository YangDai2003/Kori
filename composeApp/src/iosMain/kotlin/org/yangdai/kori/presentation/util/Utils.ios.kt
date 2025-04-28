package org.yangdai.kori.presentation.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun Modifier.clickToShareText(text: String): Modifier = clickable {
    val activityViewController = UIActivityViewController(listOf(text), null)
    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
        activityViewController, animated = true, completion = null
    )
}
