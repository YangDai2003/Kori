package org.yangdai.kori.presentation.component.note.markdown

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.yangdai.kori.presentation.theme.linkColor

class MarkdownTransformation : OutputTransformation {

    override fun TextFieldBuffer.transformOutput() {
        // 步骤 1: 优先处理代码块，因为代码块内的 Markdown 语法不应被高亮。
        val codeBlockRanges = findAndApplyCodeBlockStyles()
        val isInCodeBlock = { position: Int -> codeBlockRanges.any { position in it } }

        // 步骤 2: 应用其他样式，并传入 isInCodeBlock 检查。
        applyLinkStyles(isInCodeBlock)
        applyHeadingStyles(isInCodeBlock)
        applyGithubAlertStyles(isInCodeBlock)
        applyInlineCodeStyles(isInCodeBlock)
        applyHtmlTagStyles(isInCodeBlock)
        applyAllListStyles(isInCodeBlock)
        applyTableStyles(isInCodeBlock)
        applyInlineMathStyles(isInCodeBlock)
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
            addStyle(
                MarkdownFormat.marker,
                match.range.first,
                match.range.first + markerStart.length
            )
            addStyle(MarkdownFormat.marker, endMarkerStart, match.range.last + 1)
            // 高亮语言标识
            if (lang.isNotEmpty()) {
                addStyle(
                    MarkdownFormat.codeBlockLanguage,
                    match.range.first + markerStart.length,
                    startMarkerEnd
                )
            }
            addStyle(MarkdownFormat.monoContent, startMarkerEnd, endMarkerStart)
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

    private fun TextFieldBuffer.applyAllListStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.combinedListRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach

            // groups[0] 是整个匹配项
            val leadingSpaceGroup = match.groups[1]!!  // 前导空格
            val markerGroup = match.groups[2]!!      // 列表标记: -, *, 1.
            val taskBoxGroup = match.groups[3]       // 任务列表方括号: [x] (可选)

            val start = match.range.first
            val end: Int = if (taskBoxGroup != null) {
                // 这是任务列表
                // 高亮范围包括：前导空格 + 列表标记 + 空格 + [x]
                start + leadingSpaceGroup.value.length + markerGroup.value.length + 1 + taskBoxGroup.value.length
            } else {
                // 这是普通列表
                // 高亮范围包括：前导空格 + 列表标记
                start + leadingSpaceGroup.value.length + markerGroup.value.length
            }

            addStyle(MarkdownFormat.marker, start, end)
        }
    }

    private fun TextFieldBuffer.applyHeadingStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.headingRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach

            val hashes = match.groupValues[1]
            val level = hashes.length // 标题级别 (1-6)

            // # 标记
            addStyle(MarkdownFormat.marker, match.range.first, match.range.first + level)

            // 根据标题级别应用不同的样式
            if (level in 1..MarkdownFormat.headingStyles.size) {
                // 列表索引从0开始，所以需要 level - 1
                val style = MarkdownFormat.headingStyles[level - 1]
                // 高亮标题文本
                addStyle(style, match.range.first + level + 1, match.range.last + 1)
            }
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

    private fun TextFieldBuffer.applyInlineCodeStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.inlineCodeRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach
            addStyle(MarkdownFormat.inlineCodeStyle, match.range.first, match.range.last + 1)
        }
    }

    private fun TextFieldBuffer.applyHtmlTagStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.htmlTagRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach

            val isClosingTag = match.groupValues[1].isNotEmpty() // 检查是否有 "/"
            val tagName = match.groupValues[2]

            val tagStart = match.range.first

            // 高亮开头的 "<" 或 "</"
            val openBracketEnd = tagStart + 1 + (if (isClosingTag) 1 else 0)
            addStyle(MarkdownFormat.htmlBrackets, tagStart, openBracketEnd)

            // 高亮标签名
            val tagNameStart = openBracketEnd
            val tagNameEnd = tagNameStart + tagName.length
            addStyle(MarkdownFormat.htmlTag, tagNameStart, tagNameEnd)

            // 高亮结尾的 ">"
            addStyle(MarkdownFormat.htmlBrackets, match.range.last, match.range.last + 1)
        }
    }

    private fun TextFieldBuffer.applyTableStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.tableRegex.findAll(originalText).forEach { tableMatch ->
            if (isInCodeBlock(tableMatch.range.first)) return@forEach

            addStyle(
                MarkdownFormat.monoContent,
                tableMatch.range.first,
                tableMatch.range.last + 1
            )

            // 查找表格内所有的 '|' 字符并应用 marker 样式
            val pipeRegex = Regex("""\|""")
            pipeRegex.findAll(tableMatch.value).forEach { pipeMatch ->
                // 计算 `|` 在原始文本中的绝对位置
                val pipeIndexInOriginalText = tableMatch.range.first + pipeMatch.range.first
                addStyle(
                    MarkdownFormat.marker,
                    pipeIndexInOriginalText,
                    pipeIndexInOriginalText + 1
                )
            }
        }
    }

    private fun TextFieldBuffer.applyInlineMathStyles(isInCodeBlock: (Int) -> Boolean) {
        MarkdownFormat.inlineMathRegex.findAll(originalText).forEach { match ->
            if (isInCodeBlock(match.range.first)) return@forEach
            // 高亮开头的 '$'
            addStyle(MarkdownFormat.marker, match.range.first, match.range.first + 1)
            // 高亮结尾的 '$'
            addStyle(MarkdownFormat.marker, match.range.last, match.range.last + 1)
            addStyle(MarkdownFormat.monoContent, match.range.first + 1, match.range.last)
        }
    }
}

object MarkdownFormat {
    val headingStyles = listOf(
        SpanStyle(fontWeight = FontWeight.Black, fontSynthesis = FontSynthesis.Weight), // H1
        SpanStyle(fontWeight = FontWeight.ExtraBold, fontSynthesis = FontSynthesis.Weight), // H2
        SpanStyle(fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.Weight), // H3
        SpanStyle(fontWeight = FontWeight.SemiBold, fontSynthesis = FontSynthesis.Weight), // H4
        SpanStyle(fontWeight = FontWeight.Medium, fontSynthesis = FontSynthesis.Weight), // H5
        SpanStyle(fontWeight = FontWeight.Normal, fontSynthesis = FontSynthesis.Weight)  // H6
    )

    // 使用橙色强调，mono-space 确保对齐
    val marker = SpanStyle(color = Color(0xFFCE8D6E), fontFamily = FontFamily.Monospace)
    val codeBlockLanguage = SpanStyle(color = Color(0xFFC67CBA))
    val monoContent = SpanStyle(fontFamily = FontFamily.Monospace)
    val linkText = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
    val linkUrl = SpanStyle(color = Color.Gray, fontStyle = FontStyle.Italic)

    // 为不同 alert 类型定义更鲜明的样式
    val noteStyle = SpanStyle(color = Color(0xFF2F81F7), background = Color(0x142F81F7))
    val tipStyle = SpanStyle(color = Color(0xFF238636), background = Color(0x14238636))
    val importantStyle = SpanStyle(color = Color(0xFF8250DF), background = Color(0x148250DF))
    val warningStyle = SpanStyle(color = Color(0xFFD29922), background = Color(0x14FFD299))
    val cautionStyle = SpanStyle(color = Color(0xFFF85149), background = Color(0x14F85149))

    val inlineCodeStyle =
        SpanStyle(background = Color.DarkGray.copy(alpha = 0.2f), fontFamily = FontFamily.Monospace)
    val htmlTag = SpanStyle(color = Color(0xFF26A69A))
    val htmlBrackets = SpanStyle(color = Color(0xFFB0BEC5))

    // 支持语言名包含符号（如c#, c++, .net, C--等）
    val codeBlockRegex = Regex("""```([^\s\n`]*)?\n([\s\S]*?)\n```""")
    val inlineCodeRegex = Regex("(?<!`)`([^`\n]+)`(?!`)")
    val linkRegex = Regex("""\[(.+?)]\((.+?)\)""")

    /**
     * 捕获组:
     * 1: 前导空格 (`[ \t]*`)
     * 2: 列表标记 (`[-*+]` 或 `\d+\.`)
     * 3: (可选) 任务列表的 `[ ]` 部分。如果这个组匹配成功，说明是任务列表。
     * 4: 任务列表复选框内的字符 (` ` 或 `x` 或 `X`)
     */
    val combinedListRegex = Regex(
        """^([ \t]*)([-*+]|\d+\.)\s+(?:(\[([xX ])])\s+)?.*$""",
        RegexOption.MULTILINE
    )
    val headingRegex = Regex("""^(#{1,6})\s+(.+)$""", RegexOption.MULTILINE)

    // 根据规范，最多允许3个空格的缩进，且不允许制表符
    val githubAlertRegex =
        Regex("""^ {0,3}> ?\[(!(NOTE|TIP|IMPORTANT|WARNING|CAUTION))]""", RegexOption.MULTILINE)
    val htmlTagRegex = Regex("""<(/)?([a-zA-Z0-9]+)[^>]*?>""")

    /**
     * 用于匹配整个 Markdown 表格。
     * - 第1组 `(^\|.+\n)`: 捕获表头行。必须以'|'开头。
     * - 第2组 `(^\|[\s\d:|-].+\n)`: 捕获分隔行。同样以'|'开头，且必须包含分隔符的有效字符。
     * - 第3组 `((?:^\|.+\n?)*)`: 捕获零行或多行表体。每一行都必须以'|'开头。
     */
    val tableRegex = Regex(
        """(^\|.+\n)(^\|[\s\d:|-].+\n)((?:^\|.+\n?)*)""",
        RegexOption.MULTILINE
    )

    /**
     * 匹配行内数学公式 (例如 $E=mc^2$)
     * - `(?<!\$)`: 负向先行断言，确保前面的字符不是'$'，以避免匹配$$块。
     * - `\$`: 匹配字面的'$'符号。
     * - `([^\n$]+?)`: 非贪婪地匹配一个或多个非换行符、非'$'的字符。
     * - `\$`: 匹配字面的'$'符号。
     * - `(?!\$)`: 负向后行断言，确保后面的字符不是'$'。
     */
    val inlineMathRegex = Regex("""(?<!\$)\$([^\n$]+?)\$(?!\$)""")
}