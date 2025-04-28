package org.yangdai.kori.presentation.util

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.yangdai.kori.R

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