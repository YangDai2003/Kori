package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.yangdai.kori.presentation.theme.linkColor

class MarkdownTransformation : OutputTransformation {

    val marker = SpanStyle(color = Color(0xFFCE8D6E), fontFamily = FontFamily.Monospace)
    val keyword = SpanStyle(color = Color(0xFFC67CBA))
    val linkText = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
    val linkUrl =
        SpanStyle(fontWeight = FontWeight.Light, color = Color.Gray, fontStyle = FontStyle.Italic)

    // 为不同 alert 类型定义更鲜明的样式
    val noteStyle = SpanStyle(color = Color(0xFF2F81F7), background = Color(0x142F81F7))
    val tipStyle = SpanStyle(color = Color(0xFF238636), background = Color(0x14238636))
    val importantStyle = SpanStyle(color = Color(0xFF8250DF), background = Color(0x148250DF))
    val warningStyle = SpanStyle(color = Color(0xFFD29922), background = Color(0x14FFD299))
    val cautionStyle = SpanStyle(color = Color(0xFFF85149), background = Color(0x14F85149))

    companion object {
        // 支持语言名包含符号（如c#, c++, .net, C--等）
        private val codeBlockRegex = Regex("""```([^\s\n`]*)?\n([\s\S]*?)\n```""")
        private val linkRegex = Regex("""\[(.+?)]\((.+?)\)""")
        private val taskListRegex = Regex("""^[ \t]*-\s\[([ x])]\s.+$""", RegexOption.MULTILINE)
        private val listRegex = Regex("""^([ \t]*)([-*+]|(\d+\.))\s.+$""", RegexOption.MULTILINE)
        private val headingRegex = Regex("""^(#{1,6})\s+(.+)$""", RegexOption.MULTILINE)
        private val hrRegex = Regex("""^[ \t]*(\*{3,}|-{3,}|_{3,})[ \t]*$""", RegexOption.MULTILINE)
        private val githubAlertRegex =
            Regex("""^> \[(!(NOTE|TIP|IMPORTANT|WARNING|CAUTION))]""", RegexOption.MULTILINE)
    }

    override fun TextFieldBuffer.transformOutput() {
        // 链接 [text](url)
        linkRegex.findAll(originalText).forEach {
            addStyle(linkText, it.range.first, it.range.first + it.groupValues[1].length + 2)
            addStyle(linkUrl, it.range.first + it.groupValues[1].length + 2, it.range.last + 1)
        }

        // 任务列表 - [ ] text or - [x] text
        taskListRegex.findAll(originalText).forEach {
            addStyle(marker, it.range.first, it.range.first + it.value.indexOf(']') + 1)
        }

        // 列表处理
        listRegex.findAll(originalText).forEach {
            val start = it.range.first
            // 前导空白 + 标记 + 空格
            val markerLength = it.groupValues[1].length + it.groupValues[2].length + 1
            addStyle(marker, start, start + markerLength)
        }

        // 先收集所有代码块区间
        val codeBlockRanges = codeBlockRegex.findAll(originalText).toList().also {
            it.forEach { codeBlock ->
                val codeStart = codeBlock.range.first + codeBlock.groupValues[1].length + 3
                val codeEnd = codeBlock.range.last - 2
                // 添加代码块的起始和结束标记
                addStyle(marker, codeBlock.range.first, codeStart)
                addStyle(marker, codeEnd, codeBlock.range.last + 1)
                // 添加可选语言标记
                addStyle(keyword, codeBlock.range.first + 3, codeStart)
            }
        }

        // 判断某个位置是否在代码块区间内
        fun inCodeBlock(pos: Int): Boolean =
            codeBlockRanges.any { pos in it.range }

        // 标题
        headingRegex.findAll(originalText).forEach {
            if (!inCodeBlock(it.range.first)) {
                addStyle(marker, it.range.first, it.range.first + it.groupValues[1].length)
                addStyle(
                    keyword,
                    it.range.first + it.groupValues[1].length + 1,
                    it.range.last + 1
                )
            }
        }

        // 分割线 *** 或 --- 或 ___（独占一行，允许空格，不能有其他内容）
        hrRegex.findAll(originalText).forEach {
            addStyle(marker, it.range.first, it.range.last + 1)
        }

        // 为 > [!NOTE] [!TIP] [!IMPORTANT] [!WARNING] [!CAUTION] 添加不同颜色���式
        githubAlertRegex.findAll(originalText).forEach {
            val type = it.groupValues[2]
            val style = when (type) {
                "NOTE" -> noteStyle
                "TIP" -> tipStyle
                "IMPORTANT" -> importantStyle
                "WARNING" -> warningStyle
                "CAUTION" -> cautionStyle
                else -> keyword
            }
            // 只为 [] 部分添加样式
            val bracketStart = it.range.first + 2 // > 后的空格
            val bracketEnd = it.range.last + 1
            addStyle(style, bracketStart, bracketEnd)
        }
    }
}