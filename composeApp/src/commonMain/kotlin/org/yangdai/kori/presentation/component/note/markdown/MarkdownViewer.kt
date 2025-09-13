package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kmark.flavours.gfm.GFMFlavourDescriptor
import kmark.html.HtmlGenerator
import kmark.parser.MarkdownParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.yangdai.kori.presentation.component.note.markdown.MarkdownStyles.Companion.rememberMarkdownStyles
import org.yangdai.kori.presentation.theme.linkColor
import org.yangdai.kori.presentation.util.toHexColor

data class MarkdownStyles(
    val hexTextColor: String,
    val hexCodeBackgroundColor: String,
    val hexPreBackgroundColor: String,
    val hexQuoteBackgroundColor: String,
    val hexLinkColor: String,
    val hexBorderColor: String,
    val backgroundColor: Int
) {
    companion object {
        private fun fromColorScheme(colorScheme: ColorScheme) = MarkdownStyles(
            hexTextColor = colorScheme.onSurface.toArgb().toHexColor(),
            hexCodeBackgroundColor = colorScheme.surfaceVariant.toArgb().toHexColor(),
            hexPreBackgroundColor = colorScheme.surfaceColorAtElevation(1.dp).toArgb().toHexColor(),
            hexQuoteBackgroundColor = colorScheme.secondaryContainer.toArgb().toHexColor(),
            hexLinkColor = linkColor.toArgb().toHexColor(),
            hexBorderColor = colorScheme.outline.toArgb().toHexColor(),
            backgroundColor = colorScheme.surface.toArgb()
        )

        @Composable
        fun rememberMarkdownStyles(colorScheme: ColorScheme): MarkdownStyles {
            return remember(colorScheme) { fromColorScheme(colorScheme) }
        }
    }
}

object MarkdownDefaults {
    val flavor = GFMFlavourDescriptor()
    val parser = MarkdownParser(flavor)
}

suspend fun processMarkdown(content: String): String = withContext(Dispatchers.Default) {
    val tree = MarkdownDefaults.parser.buildMarkdownTreeFromString(content)
    HtmlGenerator(content, tree, MarkdownDefaults.flavor, true).generateHtml()
}

@Composable
expect fun MarkdownViewer(
    modifier: Modifier,
    textFieldState: TextFieldState,
    scrollState: ScrollState,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>,
    styles: MarkdownStyles = rememberMarkdownStyles(MaterialTheme.colorScheme)
)
