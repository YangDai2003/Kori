package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.yangdai.kori.presentation.component.note.markdown.MarkdownFormat.keyword
import org.yangdai.kori.presentation.component.note.markdown.MarkdownFormat.marker
import org.yangdai.kori.presentation.theme.linkColor

class MarkdownTransformation : OutputTransformation {

    override fun TextFieldBuffer.transformOutput() {
        // 步骤 1: 优先处理代码块，因为代码块内的 Markdown 语法不应被高亮。
        val codeBlockRanges = findAndApplyCodeBlockStyles()
        val isInCodeBlock = { position: Int -> codeBlockRanges.any { position in it } }

        // 步骤 2: 应用其他样式，并传入 isInCodeBlock 检查。
        applyLinkStyles(isInCodeBlock)
        applyTaskListStyles(isInCodeBlock)
        applyListStyles(isInCodeBlock)
        applyHeadingStyles(isInCodeBlock)
        applyHrStyles(isInCodeBlock)
        applyGithubAlertStyles(isInCodeBlock)
    }

    /**
     * 查找并高亮代码块，同时返回所有代码块的范围列表。
     */
    private fun TextFieldBuffer.findAndApplyCodeBlockStyles(): List<IntRange> {
        val ranges = mutableListOf<IntRange>()
        MarkdownFormat.codeBlockRegex.findAll(originalText).forEach { match ->
            val lang = match.groupValues[1]
//            val codeBlockContent = match.groupValues[2]

            val markerStart = "```"
            val startMarkerEnd = match.range.first + markerStart.length + lang.length
            val endMarkerStart = match.range.last - markerStart.length + 1

            // 高亮起始和结束标记 (```)
            addStyle(marker, match.range.first, match.range.first + markerStart.length)
            addStyle(marker, endMarkerStart, match.range.last + 1)
            // 高亮语言标识
            if (lang.isNotEmpty()) {
                addStyle(keyword, match.range.first + markerStart.length, startMarkerEnd)
            }
            ranges.add(match.range)
        }
        return ranges
    }

    private fun TextFieldBuffer.applyLinkStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.linkRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach

            val text = match.groupValues[1]
            // 高亮 [text] 部分
            addStyle(
                MarkdownFormat.linkText,
                match.range.first,
                match.range.first + text.length + 2
            )
            // 高亮 (url) 部分
            addStyle(
                MarkdownFormat.linkUrl,
                match.range.first + text.length + 2,
                match.range.last + 1
            )
        }
    }

    private fun TextFieldBuffer.applyTaskListStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.taskListRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach
            // 高亮 - [ ] 或 - [x] 部分
            val markerEnd = match.value.indexOf(']') + 1
            addStyle(marker, match.range.first, match.range.first + markerEnd)
        }
    }

    private fun TextFieldBuffer.applyListStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.listRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach
            val leadingSpace = match.groupValues[1]
            val listMarker = match.groupValues[2]
            // 高亮 前导空格 + 标记 + 空格
            val markerEnd = leadingSpace.length + listMarker.length + 1
            addStyle(marker, match.range.first, match.range.first + markerEnd)
        }
    }

    private fun TextFieldBuffer.applyHeadingStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.headingRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach

            val hashes = match.groupValues[1]
            // 高亮 # 标记
            addStyle(marker, match.range.first, match.range.first + hashes.length)
            // 高亮标题文本
            addStyle(keyword, match.range.first + hashes.length + 1, match.range.last + 1)
        }
    }

    private fun TextFieldBuffer.applyHrStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.hrRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach
            addStyle(marker, match.range.first, match.range.last + 1)
        }
    }

    private fun TextFieldBuffer.applyGithubAlertStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.githubAlertRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach

            val alertType = match.groupValues[2]
            val style = when (alertType) {
                "NOTE" -> MarkdownFormat.noteStyle
                "TIP" -> MarkdownFormat.tipStyle
                "IMPORTANT" -> MarkdownFormat.importantStyle
                "WARNING" -> MarkdownFormat.warningStyle
                "CAUTION" -> MarkdownFormat.cautionStyle
                else -> return@forEach // 不应发生，但作为保障
            }
            // 高亮 > [!TYPE] 部分
            addStyle(style, match.range.first, match.range.last + 1)
        }
    }
}

object MarkdownFormat {
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

    // 支持语言名包含符号（如c#, c++, .net, C--等）
    val codeBlockRegex = Regex("""```([^\s\n`]*)?\n([\s\S]*?)\n```""")
    val linkRegex = Regex("""\[(.+?)]\((.+?)\)""")
    val taskListRegex = Regex("""^[ \t]*-\s\[([ x])]\s.+$""", RegexOption.MULTILINE)
    val listRegex = Regex("""^([ \t]*)([-*+]|(\d+\.))\s.+$""", RegexOption.MULTILINE)
    val headingRegex = Regex("""^(#{1,6})\s+(.+)$""", RegexOption.MULTILINE)
    val hrRegex = Regex("""^[ \t]*(\*{3,}|-{3,}|_{3,})[ \t]*$""", RegexOption.MULTILINE)
    val githubAlertRegex =
        Regex("""^> \[(!(NOTE|TIP|IMPORTANT|WARNING|CAUTION))]""", RegexOption.MULTILINE)
}