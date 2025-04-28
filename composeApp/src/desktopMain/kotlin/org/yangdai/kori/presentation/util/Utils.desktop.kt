package org.yangdai.kori.presentation.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
actual fun Modifier.clickToShareText(text: String): Modifier = clickable {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(text)
    clipboard.setContents(selection, selection)
}
