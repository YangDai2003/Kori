package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kmark.flavours.gfm.GFMFlavourDescriptor
import kmark.html.HtmlGenerator
import kmark.parser.MarkdownParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.yangdai.kori.presentation.component.note.markdown.MarkdownStyles.Companion.toMarkdownStyles
import org.yangdai.kori.presentation.theme.AppConfig
import org.yangdai.kori.presentation.theme.LocalAppConfig
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
        fun ColorScheme.toMarkdownStyles() = MarkdownStyles(
            hexTextColor = this.onSurface.toArgb().toHexColor(),
            hexCodeBackgroundColor = this.surfaceVariant.toArgb().toHexColor(),
            hexPreBackgroundColor = this.surfaceColorAtElevation(1.dp).toArgb().toHexColor(),
            hexQuoteBackgroundColor = this.secondaryContainer.toArgb().toHexColor(),
            hexLinkColor = linkColor.toArgb().toHexColor(),
            hexBorderColor = this.outline.toArgb().toHexColor(),
            backgroundColor = this.surface.toArgb()
        )
    }
}

object MarkdownDefaults {

    object Placeholders {
        const val TEXT_COLOR = "{{TEXT_COLOR}}"
        const val BACKGROUND_COLOR = "{{BACKGROUND_COLOR}}"
        const val CODE_BACKGROUND = "{{CODE_BACKGROUND}}"
        const val PRE_BACKGROUND = "{{PRE_BACKGROUND}}"
        const val QUOTE_BACKGROUND = "{{QUOTE_BACKGROUND}}"
        const val LINK_COLOR = "{{LINK_COLOR}}"
        const val BORDER_COLOR = "{{BORDER_COLOR}}"
        const val COLOR_SCHEME = "{{COLOR_SCHEME}}"
        const val FONT_SCALE = "{{FONT_SCALE}}"
        const val CONTENT = "{{CONTENT}}"
    }

    private val flavor = GFMFlavourDescriptor()
    val parser = MarkdownParser(flavor)

    suspend fun processMarkdown(content: String): String = withContext(Dispatchers.Default) {
        val tree = parser.buildMarkdownTreeFromString(content)
        HtmlGenerator(content, tree, flavor, true).generateHtml()
    }
}

@Composable
expect fun MarkdownViewer(
    modifier: Modifier,
    textFieldState: TextFieldState,
    scrollState: ScrollState,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>,
    styles: MarkdownStyles = MaterialTheme.colorScheme.toMarkdownStyles(),
    appConfig: AppConfig = LocalAppConfig.current
)