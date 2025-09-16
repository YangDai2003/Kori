package org.yangdai.kori.presentation.component.main.card

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import kmark.MarkdownElementTypes
import kmark.MarkdownTokenTypes
import kmark.ast.ASTNode
import kmark.flavours.gfm.GFMElementTypes
import kmark.flavours.gfm.GFMFlavourDescriptor
import kmark.flavours.gfm.GFMTokenTypes
import kmark.parser.MarkdownParser
import org.yangdai.kori.presentation.component.note.markdown.MarkdownFormat

fun buildMarkdownAnnotatedString(text: String): AnnotatedString {

    if (text.isEmpty()) return AnnotatedString("")
    val flavor = GFMFlavourDescriptor()
    val parser = MarkdownParser(flavor)
    val rootNode = parser.buildMarkdownTreeFromString(text)

    return buildAnnotatedString {
        append(text)
        visitNode(rootNode, text)
    }
}

private fun AnnotatedString.Builder.visitNode(node: ASTNode, originalText: String) {
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
            visitNode(child, originalText)
        }
    }
}