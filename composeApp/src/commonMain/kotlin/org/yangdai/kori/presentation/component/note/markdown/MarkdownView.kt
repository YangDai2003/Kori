package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.ScrollState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import org.yangdai.kori.presentation.component.note.markdown.MarkdownStyles.Companion.rememberMarkdownStyles
import org.yangdai.kori.presentation.theme.AppConfig
import org.yangdai.kori.presentation.theme.linkColor
import org.yangdai.kori.presentation.util.toHexColor
import kotlin.math.roundToInt

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

private object StaticUris {
    val MERMAID = Res.getUri("files/mermaid.min.js")
    val KATEX = Res.getUri("files/katex/katex.min.js")
    val KATEX_CSS = Res.getUri("files/katex/katex.min.css")
    val KATEX_RENDER = Res.getUri("files/katex/auto-render.min.js")
    val PRISM = Res.getUri("files/prism/prism.js")
    val PRISM_LIGHT_CSS = Res.getUri("files/prism/prism-theme-light.css")
    val PRISM_DARK_CSS = Res.getUri("files/prism/prism-theme-dark.css")
}

fun processHtml(
    htmlTemplate: String,
    htmlContent: String,
    markdownStyles: MarkdownStyles,
    appConfig: AppConfig
) = htmlTemplate
    .replace("{{TEXT_COLOR}}", markdownStyles.hexTextColor)
    .replace("{{BACKGROUND_COLOR}}", markdownStyles.backgroundColor.toHexColor())
    .replace("{{CODE_BACKGROUND}}", markdownStyles.hexCodeBackgroundColor)
    .replace("{{PRE_BACKGROUND}}", markdownStyles.hexPreBackgroundColor)
    .replace("{{QUOTE_BACKGROUND}}", markdownStyles.hexQuoteBackgroundColor)
    .replace("{{LINK_COLOR}}", markdownStyles.hexLinkColor)
    .replace("{{BORDER_COLOR}}", markdownStyles.hexBorderColor)
    .replace("{{COLOR_SCHEME}}", if (appConfig.darkMode) "dark" else "light")
    .replace("{{MERMAID}}", StaticUris.MERMAID)
    .replace("{{KATEX}}", StaticUris.KATEX)
    .replace("{{KATEX-CSS}}", StaticUris.KATEX_CSS)
    .replace("{{KATEX-RENDER}}", StaticUris.KATEX_RENDER)
    .replace("{{PRISM}}", StaticUris.PRISM)
    .replace("{{PRISM-LIGHT-CSS}}", StaticUris.PRISM_LIGHT_CSS)
    .replace("{{PRISM-DARK-CSS}}", StaticUris.PRISM_DARK_CSS)
    .replace("{{FONT_SCALE}}", "${(appConfig.fontScale * 100).roundToInt()}%")
    .replace("{{CONTENT}}", htmlContent)

@Composable
expect fun MarkdownView(
    modifier: Modifier,
    html: String,
    scrollState: ScrollState,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>,
    styles: MarkdownStyles = rememberMarkdownStyles(MaterialTheme.colorScheme)
)
