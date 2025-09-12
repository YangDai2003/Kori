package org.yangdai.kori.presentation.component.main.card

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import org.yangdai.kori.presentation.component.note.markdown.MarkdownFormat

fun buildMarkdownAnnotatedString(text: String) =
    buildAnnotatedString {
        append(text)
        val codeBlockRanges = findAndApplyCodeBlockStyles(text)
        val mathBlockRanges = findAndApplyMathBlockStyles(text)
        val isInBlock = { position: Int ->
            codeBlockRanges.any { it.contains(position) }
                    || mathBlockRanges.any { it.contains(position) }
        }
        applyLinkStyles(text, isInBlock)
        applyHeadingStyles(text, isInBlock)
        applyGithubAlertStyles(text, isInBlock)
        applyInlineCodeStyles(text, isInBlock)
        applyHtmlTagStyles(text, isInBlock)
        applyAllListStyles(text, isInBlock)
        applyTableStyles(text, isInBlock)
        applyInlineMathStyles(text, isInBlock)
        applyBoldStyles(text, isInBlock)
        applyItalicStyles(text, isInBlock)
        applyStrikethroughStyles(text, isInBlock)
    }

private fun AnnotatedString.Builder.findAndApplyCodeBlockStyles(originalText: String): List<IntRange> {
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

private fun AnnotatedString.Builder.findAndApplyMathBlockStyles(originalText: String): List<IntRange> {
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

private fun AnnotatedString.Builder.applyLinkStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyAllListStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyHeadingStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyGithubAlertStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyInlineCodeStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
    MarkdownFormat.inlineCodeRegex.findAll(originalText).forEach { match ->
        if (isInBlock(match.range.first)) return@forEach
        addStyle(MarkdownFormat.inlineCodeStyle, match.range.first, match.range.last + 1)
    }
}

private fun AnnotatedString.Builder.applyHtmlTagStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyTableStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyInlineMathStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyBoldStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyItalicStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyStrikethroughStyles(
    originalText: String,
    isInBlock: (Int) -> Boolean
) {
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