package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.text.input.AnnotatedOutputTransformation
import androidx.compose.foundation.text.input.OutputTransformationAnnotationScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.yangdai.kori.presentation.theme.linkColor

class MarkdownTransformation : AnnotatedOutputTransformation {

    val marker = SpanStyle(color = Color(0xFFCE8D6E), fontFamily = FontFamily.Monospace)
    val keyword = SpanStyle(color = Color(0xFFC67CBA))

    val linkText = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
    val linkUrl =
        SpanStyle(fontWeight = FontWeight.Light, color = Color.Gray, fontStyle = FontStyle.Italic)

    override fun OutputTransformationAnnotationScope.annotateOutput() {
        // 链接 [text](url)
        Regex("""\[(.+?)]\((.+?)\)""").findAll(text).forEach {
            addAnnotation(linkText, it.range.first, it.range.first + it.groupValues[1].length + 2)
            addAnnotation(linkUrl, it.range.first + it.groupValues[1].length + 2, it.range.last + 1)
        }

        // 任务列表 - [ ] text or - [x] text
        Regex("""^[ \t]*-\s\[([ x])]\s.+$""", RegexOption.MULTILINE).findAll(text).forEach {
            addAnnotation(marker, it.range.first, it.range.first + it.value.indexOf(']') + 1)
        }

        // 列表 - text or * text or + text
        Regex("""^([ \t]*)([-*+])\s.+$""", RegexOption.MULTILINE).findAll(text).forEach {
            val start = it.range.first
            val markerLength =
                it.groupValues[1].length + it.groupValues[2].length + 1 // 前导空白 + 标记 + 空格
            addAnnotation(marker, start, start + markerLength)
        }

        // 有序列表 1. text
        Regex("""^[ \t]*\d+\.\s.+$""", RegexOption.MULTILINE).findAll(text).forEach {
            addAnnotation(marker, it.range.first, it.range.first + it.value.indexOf('.') + 1)
        }

        // 代码块
        Regex("""```(\w+)?\n([\s\S]*?)\n```""").findAll(text).forEach {
            val codeStart = it.range.first + it.groupValues[1].length + 3
            addAnnotation(marker, it.range.first, it.range.first + 3)
            addAnnotation(marker, it.range.last - 2, it.range.last + 1)
            // it.groupValues[1] 是可选语言
            addAnnotation(
                keyword,
                it.range.first + 3,
                codeStart
            )
        }

        // 分割线 *** 或 --- 或 ___（独占一行，允许空格，不能有其他内容）
        Regex("""^[ \t]*(\*{3,}|-{3,}|_{3,})[ \t]*$""", RegexOption.MULTILINE).findAll(text)
            .forEach {
                addAnnotation(marker, it.range.first, it.range.last + 1)
            }
    }
}