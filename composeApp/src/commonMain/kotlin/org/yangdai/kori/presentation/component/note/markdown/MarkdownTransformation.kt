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
        val mathBlockRanges = findAndApplyMathBlockStyles()
        val isInBlock = { position: Int ->
            codeBlockRanges.any { it.contains(position) }
                    || mathBlockRanges.any { it.contains(position) }
        }

        // 步骤 2: 应用其他样式，并传入 isInCodeBlock 检查。
        applyLinkStyles(isInBlock)
        applyHeadingStyles(isInBlock)
        applyGithubAlertStyles(isInBlock)
        applyInlineCodeStyles(isInBlock)
        applyHtmlTagStyles(isInBlock)
        applyAllListStyles(isInBlock)
        applyTableStyles(isInBlock)
        applyInlineMathStyles(isInBlock)
        applyBoldStyles(isInBlock)
        applyItalicStyles(isInBlock)
        applyStrikethroughStyles(isInBlock)
    }

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

    private fun TextFieldBuffer.findAndApplyMathBlockStyles(): List<IntRange> {
        val ranges = mutableListOf<IntRange>()
        MarkdownFormat.mathBlockRegex.findAll(originalText).forEach { match ->
            ranges.add(match.range)
            // 高亮首尾的 '$$' 标记
            val openingMarkerEnd = match.range.first + 2
            val closingMarkerStart = match.range.last - 1
            addStyle(MarkdownFormat.marker, match.range.first, openingMarkerEnd)
            addStyle(MarkdownFormat.marker, closingMarkerStart, match.range.last + 1)

            // 提取公式内容
            val contentGroup = match.groups[1] ?: return@forEach

            // 内容的起始位置就是整个匹配的起始位置 + 开头标记"$$"的长度(2)
            val contentOffset = match.range.first + 2
            MarkdownFormat.latexRegex.findAll(contentGroup.value).forEach { latexMatch ->
                val style = when {
                    latexMatch.groups[1] != null -> MarkdownFormat.latexCommand
                    latexMatch.groups[2] != null -> MarkdownFormat.latexNumber
                    latexMatch.groups[3] != null -> MarkdownFormat.brackets
                    latexMatch.groups[4] != null -> MarkdownFormat.latexOperator
                    else -> return@forEach
                }

                // 计算高亮范围在原始文本中的绝对位置
                val start = contentOffset + latexMatch.range.first
                val end = contentOffset + latexMatch.range.last + 1
                addStyle(style, start, end)
            }
        }
        return ranges
    }

    private fun TextFieldBuffer.applyLinkStyles(isInBlock: (Int) -> Boolean) {
        MarkdownFormat.linkRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach

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

    private fun TextFieldBuffer.applyAllListStyles(isInBlock: (Int) -> Boolean) {
        MarkdownFormat.combinedListRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach

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

    private fun TextFieldBuffer.applyHeadingStyles(isInBlock: (Int) -> Boolean) {
        MarkdownFormat.headingRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach

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

    private fun TextFieldBuffer.applyGithubAlertStyles(isInBlock: (Int) -> Boolean) {
        MarkdownFormat.githubAlertRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach

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

    private fun TextFieldBuffer.applyInlineCodeStyles(isInBlock: (Int) -> Boolean) {
        MarkdownFormat.inlineCodeRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach
            addStyle(MarkdownFormat.inlineCodeStyle, match.range.first, match.range.last + 1)
        }
    }

    private fun TextFieldBuffer.applyHtmlTagStyles(isInBlock: (Int) -> Boolean) {
        // 正则表达式，用于在属性字符串中查找所有双引号包裹的内容
        val quotedContentRegex = Regex("\"[^\"]*\"")

        MarkdownFormat.htmlTagRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach

            // 安全地提取捕获组
            val isClosingTag = match.groups[1] != null    // 组1: "/"
            val tagNameGroup = match.groups[2]            // 组2: "tag"
            val attributesGroup = match.groups[3]       // 组3: 'xx="yyy"'

            // 如果没有匹配到标签名，则跳过
            if (tagNameGroup == null) return@forEach

            val tagStart = match.range.first

            // 计算开括号部分的结束位置 (e.g., "<" or "</")
            val openBracketEnd = tagStart + 1 + (if (isClosingTag) 1 else 0)
            addStyle(MarkdownFormat.brackets, tagStart, openBracketEnd)
            // 高亮闭括号 ">"
            addStyle(MarkdownFormat.brackets, match.range.last, match.range.last + 1)

            // 标签名的起始位置紧跟在开括号之后
            val tagNameStart = openBracketEnd
            val tagNameEnd = tagNameStart + tagNameGroup.value.length
            addStyle(MarkdownFormat.htmlTag, tagNameStart, tagNameEnd)

            // 仅当是开标签且属性部分不为空时处理
            if (!isClosingTag && attributesGroup != null && attributesGroup.value.isNotEmpty()) {
                // 属性字符串在原始文本中的绝对起始位置
                val attributesStart = tagNameEnd
                quotedContentRegex.findAll(attributesGroup.value).forEach { quoteMatch ->
                    // 计算双引号内容在原始文本中的绝对位置：
                    // 属性的绝对起始位置 + 双引号内容在属性字符串中的相对起始位置
                    val quoteStart = attributesStart + quoteMatch.range.first
                    val quoteEnd = attributesStart + quoteMatch.range.last + 1
                    addStyle(MarkdownFormat.htmlQuotedValue, quoteStart, quoteEnd)
                }
            }
        }
    }

    private fun TextFieldBuffer.applyTableStyles(isInBlock: (Int) -> Boolean) {
        MarkdownFormat.tableRegex.findAll(originalText).forEach { tableMatch ->
            if (isInBlock(tableMatch.range.first)) return@forEach

            addStyle(MarkdownFormat.monoContent, tableMatch.range.first, tableMatch.range.last + 1)

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

    private fun TextFieldBuffer.applyInlineMathStyles(isInBlock: (Int) -> Boolean) {
        MarkdownFormat.inlineMathRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach
            // 高亮开头的 '$', 高亮结尾的 '$'
            addStyle(MarkdownFormat.marker, match.range.first, match.range.first + 1)
            addStyle(MarkdownFormat.marker, match.range.last, match.range.last + 1)

            // 提取公式内容及其在原始文本中的起始位置
            val content = match.groupValues[1]
            val contentOffset = match.range.first + 1

            // 在公式内容上进行二次匹配，实现内部语法高亮
            MarkdownFormat.latexRegex.findAll(content).forEach { latexMatch ->
                // 根据匹配到的捕获组来决定应用哪种样式
                val style = when {
                    latexMatch.groups[1] != null -> MarkdownFormat.latexCommand
                    latexMatch.groups[2] != null -> MarkdownFormat.latexNumber
                    latexMatch.groups[3] != null -> MarkdownFormat.brackets
                    latexMatch.groups[4] != null -> MarkdownFormat.latexOperator
                    else -> return@forEach
                }

                // 计算高亮范围在原始文本中的绝对位置
                val start = contentOffset + latexMatch.range.first
                val end = contentOffset + latexMatch.range.last + 1
                addStyle(style, start, end)
            }
        }
    }

    private fun TextFieldBuffer.applyBoldStyles(isInBlock: (Int) -> Boolean) {
        MarkdownFormat.boldRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach

            val marker = match.groupValues[1]
            val markerSize = marker.length

            // 高亮标记 (e.g., **)
            addStyle(MarkdownFormat.marker, match.range.first, match.range.first + markerSize)
            addStyle(MarkdownFormat.marker, match.range.last - markerSize + 1, match.range.last + 1)

            // 对内容应用加粗样式
            addStyle(
                MarkdownFormat.boldStyle,
                match.range.first + markerSize,
                match.range.last - markerSize + 1
            )
        }
    }

    private fun TextFieldBuffer.applyItalicStyles(isInBlock: (Int) -> Boolean) {
        // 注意：这个正则表达式会匹配 *...* 和 _..._ 但会避免匹配 **...** 和 __...__ 的一部分
        MarkdownFormat.italicRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach

            val marker = match.groupValues[1]
            val markerSize = marker.length

            // 高亮标记 (e.g., *)
            addStyle(MarkdownFormat.marker, match.range.first, match.range.first + markerSize)
            addStyle(MarkdownFormat.marker, match.range.last - markerSize + 1, match.range.last + 1)

            // 对内容应用斜体样式
            addStyle(
                MarkdownFormat.italicStyle,
                match.range.first + markerSize,
                match.range.last - markerSize + 1
            )
        }
    }

    private fun TextFieldBuffer.applyStrikethroughStyles(isInBlock: (Int) -> Boolean) {
        MarkdownFormat.strikethroughRegex.findAll(originalText).forEach { match ->
            if (isInBlock(match.range.first)) return@forEach

            val markerSize = 2 // "~~" 的长度

            // 高亮标记 (~~)
            addStyle(MarkdownFormat.marker, match.range.first, match.range.first + markerSize)
            addStyle(MarkdownFormat.marker, match.range.last - markerSize + 1, match.range.last + 1)

            // 对内容应用删除线样式
            addStyle(
                MarkdownFormat.strikethroughStyle,
                match.range.first + markerSize,
                match.range.last - markerSize + 1
            )
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
    val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
    val italicStyle = SpanStyle(fontStyle = FontStyle.Italic)
    val strikethroughStyle = SpanStyle(textDecoration = TextDecoration.LineThrough)

    // 为不同 alert 类型定义更鲜明的样式
    val noteStyle = SpanStyle(color = Color(0xFF2F81F7), background = Color(0x142F81F7))
    val tipStyle = SpanStyle(color = Color(0xFF238636), background = Color(0x14238636))
    val importantStyle = SpanStyle(color = Color(0xFF8250DF), background = Color(0x148250DF))
    val warningStyle = SpanStyle(color = Color(0xFFD29922), background = Color(0x14FFD299))
    val cautionStyle = SpanStyle(color = Color(0xFFF85149), background = Color(0x14F85149))

    val inlineCodeStyle =
        SpanStyle(background = Color.DarkGray.copy(alpha = 0.2f), fontFamily = FontFamily.Monospace)
    val htmlTag = SpanStyle(color = Color(0xFFD92C54))       // 标签名样式 (例如: div)
    val brackets = SpanStyle(color = Color(0xFF808080))  // 括号样式
    val htmlQuotedValue = SpanStyle(color = Color(0xFF8ABB6C)) // (例如: "yyy")

    val latexCommand = SpanStyle(color = Color(0xFF512DA8))  // LaTeX 命令样式 (例如: \frac, \alpha)
    val latexNumber = SpanStyle(color = Color(0xFF00796B))   // LaTeX 数字样式 (例如: 2, 3.14)
    val latexOperator = SpanStyle(color = Color(0xFF039BE5))  // LaTeX 运算符样式 (例如: +, -, ^, _, =)

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
    val htmlTagRegex = Regex("""<(/)?([a-zA-Z0-9]+)([^>\n]*)>""")

    /**
     * 用于匹配整个 Markdown 表格。
     * - 第1组 `(^\|.+\n)`: 捕获表头行。必须以'|'开头。
     * - 第2组 `(^\|[\s\d:|-].+\n)`: 捕获分隔行。同样以'|'开头，且必须包含分隔符的有效字符。
     * - 第3组 `((?:^\|.+\n?)*)`: 捕获零行或多行表体。每一行都必须以'|'开头。
     */
    val tableRegex = Regex("""(^\|.+\n)(^\|[\s\d:|-].+\n)((?:^\|.+\n?)*)""", RegexOption.MULTILINE)

    /**
     * 匹配行内数学公式 (例如 $E=mc^2$)
     * - `(?<!\$)`: 负向先行断言，确保前面的字符不是'$'，以避免匹配$$块。
     * - `\$`: 匹配字面的'$'符号。
     * - `([^\n$]+?)`: 非贪婪地匹配一个或多个非换行符、非'$'的字符。
     * - `\$`: 匹配字面的'$'符号。
     * - `(?!\$)`: 负向后行断言，确保后面的字符不是'$'。
     */
    val inlineMathRegex = Regex("""(?<!\$)\$([^\n$]+?)\$(?!\$)""")
    val latexRegex = Regex("""(\\[a-zA-Z]+)|(\d+(?:\.\d+)?)|([{}()\[\]])|([+*/=^_,.<>|-])""")
    val mathBlockRegex = Regex("""\$\$([\s\S]+?)\$\$""")

    /**
     * 匹配 **粗体** 或 __粗体__
     * - `(\*\*|__)`: 第1捕获组，匹配 `**` 或 `__`
     * - `(?=\S)`: 正向先行断言，确保标记后面紧跟一个非空白字符
     * - `(.+?)`: 第2捕获组，非贪婪地匹配任何字符
     * - `(?<=\S)`: 反向先行断言，确保标记前面紧跟一个非空白字符
     * - `\1`: 反向引用，确保开始和结束标记匹配
     */
    val boldRegex = Regex("""(\*\*|__)(?=\S)(.+?)(?<=\S)\1""")

    /**
     * 匹配 *斜体* 或 _斜体_
     * - `(?<![*_])`: 反向先行断言，确保前面不是另一个 `*` 或 `_` (避免匹配粗体)
     * - `(\*|_)`: 第1捕获组，匹配 `*` 或 `_`
     * - `(?![*_])`: 正向先行断言，确保后面不是另一个 `*` 或 `_`
     * - `(?=\S)(.+?)(?<=\S)`: 与粗体逻辑相同，匹配内容
     * - `\1`: 确保开始和结束标记匹配
     */
    val italicRegex = Regex("""(?<![*_])([*_])(?![*_])(?=\S)(.+?)(?<=\S)\1(?![*_])""")

    /**
     * 匹配 ~~删除线~~
     * - `(~~)`: 捕获组
     * - `(?=\S)(.+?)(?<=\S)`: 与粗体逻辑相同
     * - `\1`: 确保开始和结束标记匹配
     */
    val strikethroughRegex = Regex("""(~~)(?=\S)(.+?)(?<=\S)\1""")
}