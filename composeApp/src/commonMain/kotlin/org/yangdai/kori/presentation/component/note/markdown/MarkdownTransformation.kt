package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.text.input.AnnotatedOutputTransformation
import androidx.compose.foundation.text.input.OutputTransformationAnnotationScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.yangdai.kori.presentation.theme.linkColor

class MarkdownTransformation : AnnotatedOutputTransformation {

    val bold = SpanStyle(
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Normal,
        fontSynthesis = FontSynthesis.Weight
    )
    val italic = SpanStyle(
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Normal,
        fontSynthesis = FontSynthesis.Style
    )
    val boldItalic = SpanStyle(
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        fontSynthesis = FontSynthesis.All
    )
    val strikeThrough = SpanStyle(textDecoration = TextDecoration.LineThrough)
    val underline = SpanStyle(textDecoration = TextDecoration.Underline)
    val strikeThroughAndUnderline = SpanStyle(
        textDecoration = TextDecoration.combine(
            listOf(
                TextDecoration.LineThrough,
                TextDecoration.Underline
            )
        )
    )
    val code = SpanStyle(
        fontFamily = FontFamily.Monospace,
        background = Color.LightGray.copy(alpha = 0.3f)
    )
    val highlight = SpanStyle(
        color = Color.Black,
        background = Color.Yellow.copy(alpha = 1f)
    )
    val marker = SpanStyle(color = Color(0xFFCE8D6E), fontFamily = FontFamily.Monospace)
    val keyword = SpanStyle(color = Color(0xFFC67CBA))
    val link = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
    val symbol = SpanStyle(fontWeight = FontWeight.Light, color = Color.Gray)


    override fun OutputTransformationAnnotationScope.annotateOutput() {
        val headingRanges = Regex("""^(#{1,6})""", RegexOption.MULTILINE)
            .findAll(text)
            .map { it.range }
            .toList()
        for (range in headingRanges) {
            addAnnotation(marker, range.first, range.last + 1)
        }
    }
}