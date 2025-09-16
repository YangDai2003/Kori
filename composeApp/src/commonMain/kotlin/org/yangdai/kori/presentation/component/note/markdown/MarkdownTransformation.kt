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
import kmark.MarkdownElementTypes
import kmark.MarkdownTokenTypes
import kmark.ast.ASTNode
import kmark.flavours.gfm.GFMElementTypes
import kmark.flavours.gfm.GFMFlavourDescriptor
import kmark.flavours.gfm.GFMTokenTypes
import kmark.parser.MarkdownParser
import org.yangdai.kori.presentation.theme.linkColor

class MarkdownTransformation : OutputTransformation {
    private val flavor = GFMFlavourDescriptor()
    private val parser = MarkdownParser(flavor)

    override fun TextFieldBuffer.transformOutput() {
        if (originalText.isEmpty()) return
        val rootNode = parser.buildMarkdownTreeFromString(originalText.toString())
        visitNode(rootNode)
    }

    private fun TextFieldBuffer.visitNode(node: ASTNode) {
        var handleChildrenRecursively = true

        when (node.type) {

            /*---Code---*/
            MarkdownTokenTypes.CODE_FENCE_START, MarkdownTokenTypes.CODE_FENCE_END -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.endOffset)
            }

            MarkdownTokenTypes.FENCE_LANG -> {
                addStyle(MarkdownFormat.codeBlockLanguage, node.startOffset, node.endOffset)
            }

            MarkdownTokenTypes.CODE_FENCE_CONTENT -> {
                addStyle(MarkdownFormat.monoContent, node.startOffset, node.endOffset)
            }

            MarkdownElementTypes.CODE_SPAN -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.startOffset + 1)
                addStyle(MarkdownFormat.marker, node.endOffset - 1, node.endOffset)
                addStyle(MarkdownFormat.inlineCodeStyle, node.startOffset + 1, node.endOffset - 1)
                handleChildrenRecursively = false
            }

            /*---Inline Content---*/
            MarkdownElementTypes.EMPH -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.startOffset + 1)
                addStyle(MarkdownFormat.marker, node.endOffset - 1, node.endOffset)
                if (node.endOffset > node.startOffset + 2) { //确保中间有文本
                    addStyle(MarkdownFormat.italicStyle, node.startOffset + 1, node.endOffset - 1)
                }
            }

            MarkdownElementTypes.STRONG -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.startOffset + 2)
                addStyle(MarkdownFormat.marker, node.endOffset - 2, node.endOffset)
                if (node.endOffset > node.startOffset + 4) { //确保中间有文本
                    addStyle(MarkdownFormat.boldStyle, node.startOffset + 2, node.endOffset - 2)
                }
            }

            GFMElementTypes.STRIKETHROUGH -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.startOffset + 2)
                addStyle(MarkdownFormat.marker, node.endOffset - 2, node.endOffset)
                if (node.endOffset > node.startOffset + 4) { //确保中间有文本
                    addStyle(
                        MarkdownFormat.strikethroughStyle,
                        node.startOffset + 2,
                        node.endOffset - 2
                    )
                }
            }

            /*---List---*/
            MarkdownTokenTypes.LIST_BULLET, MarkdownTokenTypes.LIST_NUMBER, GFMTokenTypes.CHECK_BOX -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.endOffset)
            }

            /*---Table---*/
            GFMTokenTypes.TABLE_SEPARATOR -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.endOffset)
            }

            GFMElementTypes.TABLE -> {
                addStyle(MarkdownFormat.monoContent, node.startOffset, node.endOffset)
            }

            /*---Math---*/
            GFMElementTypes.INLINE_MATH -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.startOffset + 1)
                addStyle(MarkdownFormat.marker, node.endOffset - 1, node.endOffset)
                val mathContent = originalText.substring(node.startOffset + 1, node.endOffset - 1)
                MarkdownFormat.latexRegex.findAll(mathContent).forEach { latexMatch ->
                    val style = when {
                        latexMatch.groups[1] != null -> MarkdownFormat.latexCommand
                        latexMatch.groups[2] != null -> MarkdownFormat.latexNumber
                        latexMatch.groups[3] != null -> MarkdownFormat.brackets
                        else -> return@forEach
                    }

                    // 计算高亮范围在原始文本中的绝对位置
                    val start = node.startOffset + 1 + latexMatch.range.first
                    val end = node.startOffset + 1 + latexMatch.range.last + 1
                    addStyle(style, start, end)
                }
                handleChildrenRecursively = false
            }

            GFMElementTypes.BLOCK_MATH -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.startOffset + 2)
                addStyle(MarkdownFormat.marker, node.endOffset - 2, node.endOffset)
                val mathContent = originalText.substring(node.startOffset + 2, node.endOffset - 2)
                MarkdownFormat.latexRegex.findAll(mathContent).forEach { latexMatch ->
                    val style = when {
                        latexMatch.groups[1] != null -> MarkdownFormat.latexCommand
                        latexMatch.groups[2] != null -> MarkdownFormat.latexNumber
                        latexMatch.groups[3] != null -> MarkdownFormat.brackets
                        else -> return@forEach
                    }

                    val start = node.startOffset + 2 + latexMatch.range.first
                    val end = node.startOffset + 2 + latexMatch.range.last + 1
                    addStyle(style, start, end)
                }
                handleChildrenRecursively = false
            }

            /*---Horizontal Rule---*/
            MarkdownTokenTypes.HORIZONTAL_RULE -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.endOffset)
            }

            /*---HTML---*/
            MarkdownTokenTypes.HTML_TAG, MarkdownElementTypes.HTML_BLOCK -> {
                val html = originalText.substring(node.startOffset, node.endOffset)

                MarkdownFormat.htmlTagRegex.findAll(html).forEach { match ->

                    // 安全地提取捕获组
                    val isClosingTag = match.groups[1] != null    // 组1: "/"
                    val tagNameGroup = match.groups[2]            // 组2: "tag"
                    val attributesGroup = match.groups[3]       // 组3: 'xx="yyy"'

                    // 如果没有匹配到标签名，则跳过
                    if (tagNameGroup == null) return@forEach

                    val tagStart = match.range.first + node.startOffset
                    // 计算开括号部分的结束位置 (e.g., "<" or "</")
                    val openBracketEnd = tagStart + 1 + (if (isClosingTag) 1 else 0)
                    addStyle(MarkdownFormat.brackets, tagStart, openBracketEnd)
                    // 高亮闭括号 ">"
                    addStyle(
                        MarkdownFormat.brackets,
                        match.range.last + node.startOffset,
                        match.range.last + node.startOffset + 1
                    )

                    // 标签名的起始位置紧跟在开括号之后
                    val tagNameStart = openBracketEnd
                    val tagNameEnd = tagNameStart + tagNameGroup.value.length
                    addStyle(MarkdownFormat.htmlTag, tagNameStart, tagNameEnd)

                    // 仅当是开标签且属性部分不为空时处理
                    if (!isClosingTag && attributesGroup != null && attributesGroup.value.isNotEmpty()) {
                        // 属性字符串在原始文本中的绝对起始位置
                        val attributesStart = tagNameEnd
                        MarkdownFormat.quotedContentRegex.findAll(attributesGroup.value)
                            .forEach { quoteMatch ->
                                // 计算双引号内容在原始文本中的绝对位置：
                                // 属性的绝对起始位置 + 双引号内容在属性字符串中的相对起始位置
                                val quoteStart = attributesStart + quoteMatch.range.first
                                val quoteEnd = attributesStart + quoteMatch.range.last + 1
                                addStyle(MarkdownFormat.htmlQuotedValue, quoteStart, quoteEnd)
                            }
                    }
                }
            }

            /*---Heading---*/
            MarkdownTokenTypes.ATX_HEADER, MarkdownTokenTypes.SETEXT_1, MarkdownTokenTypes.SETEXT_2 -> {
                addStyle(MarkdownFormat.marker, node.startOffset, node.endOffset)
            }

            MarkdownElementTypes.ATX_1, MarkdownElementTypes.SETEXT_1 -> {
                addStyle(MarkdownFormat.headingStyles[0], node.startOffset, node.endOffset)
            }

            MarkdownElementTypes.ATX_2, MarkdownElementTypes.SETEXT_2 -> {
                addStyle(MarkdownFormat.headingStyles[1], node.startOffset, node.endOffset)
            }

            MarkdownElementTypes.ATX_3 -> {
                addStyle(MarkdownFormat.headingStyles[2], node.startOffset, node.endOffset)
            }

            MarkdownElementTypes.ATX_4 -> {
                addStyle(MarkdownFormat.headingStyles[3], node.startOffset, node.endOffset)
            }

            MarkdownElementTypes.ATX_5 -> {
                addStyle(MarkdownFormat.headingStyles[4], node.startOffset, node.endOffset)
            }

            MarkdownElementTypes.ATX_6 -> {
                addStyle(MarkdownFormat.headingStyles[5], node.startOffset, node.endOffset)
            }

            /*---Link---*/
            MarkdownElementTypes.LINK_TEXT, MarkdownElementTypes.AUTOLINK -> {
                addStyle(MarkdownFormat.linkTextStyle, node.startOffset, node.endOffset)
            }

            MarkdownElementTypes.LINK_DESTINATION -> {
                addStyle(MarkdownFormat.urlStyle, node.startOffset, node.endOffset)
            }

            /*---BlockQuote---*/
            MarkdownElementTypes.BLOCK_QUOTE -> {
                val text = originalText.substring(node.startOffset, node.endOffset)
                MarkdownFormat.githubAlertRegex.findAll(text).forEach { match ->
                    val alertType = match.groupValues[2]
                    val style = when (alertType) {
                        "NOTE" -> MarkdownFormat.noteStyle
                        "TIP" -> MarkdownFormat.tipStyle
                        "IMPORTANT" -> MarkdownFormat.importantStyle
                        "WARNING" -> MarkdownFormat.warningStyle
                        "CAUTION" -> MarkdownFormat.cautionStyle
                        else -> return@forEach
                    }
                    addStyle(
                        style,
                        node.startOffset + match.range.first,
                        node.startOffset + match.range.last + 1
                    )
                }
            }

            else -> {
                // 对于未特殊处理的节点，不应用特定样式
            }
        }

        if (handleChildrenRecursively) {
            node.children.forEach { child ->
                visitNode(child)
            }
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
    val linkTextStyle = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
    val urlStyle = SpanStyle(color = Color.Gray, fontStyle = FontStyle.Italic)
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

    // 根据规范，最多允许3个空格的缩进，且不允许制表符
    val githubAlertRegex =
        Regex("""^ {0,3}> ?\[(!(NOTE|TIP|IMPORTANT|WARNING|CAUTION))]""", RegexOption.MULTILINE)
    val htmlTagRegex = Regex("""<(/)?([a-zA-Z0-9]+)([^>\n]*)>""")
    val quotedContentRegex = Regex("\"[^\"]*\"")
    val latexRegex = Regex("""(\\[a-zA-Z]+)|(\d+(?:\.\d+)?)|([{}()\[\]])""")
}