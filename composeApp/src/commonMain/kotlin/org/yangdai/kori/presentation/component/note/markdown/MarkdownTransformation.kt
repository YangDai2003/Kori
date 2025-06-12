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

    companion object {
        private val codeBlockRegex = Regex("""```(\w+)?\n([\s\S]*?)\n```""")
        private val linkRegex = Regex("""\[(.+?)]\((.+?)\)""")
        private val taskListRegex = Regex("""^[ \t]*-\s\[([ x])]\s.+$""", RegexOption.MULTILINE)
        private val unorderedListRegex = Regex("""^([ \t]*)([-*+])\s.+$""", RegexOption.MULTILINE)
        private val orderedListRegex = Regex("""^[ \t]*\d+\.\s.+$""", RegexOption.MULTILINE)
        private val headingRegex = Regex("""^(#{1,6})\s+(.+)$""", RegexOption.MULTILINE)
        private val hrRegex = Regex("""^[ \t]*(\*{3,}|-{3,}|_{3,})[ \t]*$""", RegexOption.MULTILINE)
    }

    override fun OutputTransformationAnnotationScope.annotateOutput() {
        // 链接 [text](url)
        linkRegex.findAll(text).forEach {
            addAnnotation(linkText, it.range.first, it.range.first + it.groupValues[1].length + 2)
            addAnnotation(linkUrl, it.range.first + it.groupValues[1].length + 2, it.range.last + 1)
        }

        // 任务列表 - [ ] text or - [x] text
        taskListRegex.findAll(text).forEach {
            addAnnotation(marker, it.range.first, it.range.first + it.value.indexOf(']') + 1)
        }

        // 列表 - text or * text or + text
        unorderedListRegex.findAll(text).forEach {
            val start = it.range.first
            val markerLength =
                it.groupValues[1].length + it.groupValues[2].length + 1 // 前导空白 + 标记 + 空格
            addAnnotation(marker, start, start + markerLength)
        }

        // 有序列表 1. text
        orderedListRegex.findAll(text).forEach {
            addAnnotation(marker, it.range.first, it.range.first + it.value.indexOf('.') + 1)
        }

        // 先收集所有代码块区间
        val codeBlockRanges = codeBlockRegex.findAll(text).toList().also {
            it.forEach {
                val codeStart = it.range.first + it.groupValues[1].length + 3
                val codeEnd = it.range.last - 2
                // 添加代码块的起始和结束标记
                addAnnotation(marker, it.range.first, codeStart)
                addAnnotation(marker, codeEnd, it.range.last + 1)
                // 添加可选语言标记
                addAnnotation(keyword, it.range.first + 3, codeStart)
            }
        }

        // 判断某个位置是否在代码块区间内
        fun inCodeBlock(pos: Int): Boolean =
            codeBlockRanges.any { pos in it.range }

        // 标题
        headingRegex.findAll(text).forEach {
            if (!inCodeBlock(it.range.first)) {
                addAnnotation(marker, it.range.first, it.range.first + it.groupValues[1].length)
                addAnnotation(
                    keyword,
                    it.range.first + it.groupValues[1].length + 1,
                    it.range.last + 1
                )
            }
        }

        // 分割线 *** 或 --- 或 ___（独占一行，允许空格，不能有其他内容）
        hrRegex.findAll(text).forEach {
            addAnnotation(marker, it.range.first, it.range.last + 1)
        }
    }
}