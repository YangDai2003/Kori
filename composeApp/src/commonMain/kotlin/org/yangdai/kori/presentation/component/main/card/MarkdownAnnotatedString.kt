package org.yangdai.kori.presentation.component.main.card

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import org.yangdai.kori.presentation.component.note.markdown.MarkdownFormat

fun buildMarkdownAnnotatedString(text: String) =
    buildAnnotatedString {
        append(text)

        val codeBlockRanges = findAndApplyCodeBlockStyles(text)
        val isInCodeBlock = { position: Int -> codeBlockRanges.any { position in it } }

        applyLinkStyles(text, isInCodeBlock)
        applyHeadingStyles(text, isInCodeBlock)
        applyGithubAlertStyles(text, isInCodeBlock)
        applyInlineCodeStyles(text, isInCodeBlock)
        applyHtmlTagStyles(text, isInCodeBlock)
        applyAllListStyles(text, isInCodeBlock)
        applyTableStyles(text, isInCodeBlock)
        applyInlineMathStyles(text, isInCodeBlock)
    }

/**
 * 查找并高亮代码块，同时返回所有代码块的范围列表。
 */
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

private fun AnnotatedString.Builder.applyLinkStyles(
    originalText: String,
    isInCodeBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyAllListStyles(
    originalText: String,
    isInCodeBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyHeadingStyles(
    originalText: String,
    isInCodeBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyGithubAlertStyles(
    originalText: String,
    isInCodeBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyInlineCodeStyles(
    originalText: String,
    isInCodeBlock: (Int) -> Boolean
) {
    MarkdownFormat.inlineCodeRegex.findAll(originalText).forEach { match ->
        if (isInCodeBlock(match.range.first)) return@forEach
        addStyle(MarkdownFormat.inlineCodeStyle, match.range.first, match.range.last + 1)
    }
}

private fun AnnotatedString.Builder.applyHtmlTagStyles(
    originalText: String,
    isInCodeBlock: (Int) -> Boolean
) {
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

private fun AnnotatedString.Builder.applyTableStyles(
    originalText: String,
    isInCodeBlock: (Int) -> Boolean
) {
    MarkdownFormat.tableRegex.findAll(originalText).forEach { tableMatch ->
        if (isInCodeBlock(tableMatch.range.first)) return@forEach

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
    isInCodeBlock: (Int) -> Boolean
) {
    MarkdownFormat.inlineMathRegex.findAll(originalText).forEach { match ->
        if (isInCodeBlock(match.range.first)) return@forEach
        // 高亮开头的 '$'
        addStyle(MarkdownFormat.marker, match.range.first, match.range.first + 1)
        // 高亮结尾的 '$'
        addStyle(MarkdownFormat.marker, match.range.last, match.range.last + 1)
        addStyle(MarkdownFormat.monoContent, match.range.first + 1, match.range.last)
    }
}