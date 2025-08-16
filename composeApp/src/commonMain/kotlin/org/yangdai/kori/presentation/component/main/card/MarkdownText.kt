package org.yangdai.kori.presentation.component.main.card

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import org.yangdai.kori.presentation.component.note.markdown.MarkdownFormat
import org.yangdai.kori.presentation.screen.settings.CardSize

@Composable
fun MarkdownText(text: String, noteItemProperties: NoteItemProperties) {
    Text(
        text = buildAnnotatedString {
            append(text)

            // 步骤 1: 优先处理代码块，它们内部的语法不应被高亮。
            val codeBlockRanges = applyCodeBlockStyles(text)
            val isInCodeBlock = { position: Int -> codeBlockRanges.any { position in it } }

            // 步骤 2: 应用其他所有样式，并传入 isInCodeBlock 检查。
            applyLinkStyles(text, isInCodeBlock)
            applyTaskListStyles(text, isInCodeBlock)
            applyListStyles(text, isInCodeBlock)
            applyHeadingStyles(text, isInCodeBlock)
            applyHrStyles(text, isInCodeBlock)
            applyGithubAlertStyles(text, isInCodeBlock)
        },
        style = MaterialTheme.typography.bodyMedium,
        maxLines = if (noteItemProperties.cardSize == CardSize.DEFAULT) 5 else 2,
        overflow = if (noteItemProperties.clipOverflow) TextOverflow.Clip else TextOverflow.Ellipsis
    )
}

private fun AnnotatedString.Builder.applyCodeBlockStyles(text: String): List<IntRange> {
    val ranges = mutableListOf<IntRange>()
    MarkdownFormat.codeBlockRegex.findAll(text).forEach { match ->
        val lang = match.groupValues[1]
//            val codeBlockContent = match.groupValues[2]

        val markerStart = "```"
        val startMarkerEnd = match.range.first + markerStart.length + lang.length
        val endMarkerStart = match.range.last - markerStart.length + 1

        // 高亮起始和结束标记 (```)
        addStyle(MarkdownFormat.marker, match.range.first, match.range.first + markerStart.length)
        addStyle(MarkdownFormat.marker, endMarkerStart, match.range.last + 1)
        // 高亮语言标识
        if (lang.isNotEmpty()) {
            addStyle(MarkdownFormat.keyword, match.range.first + markerStart.length, startMarkerEnd)
        }
        ranges.add(match.range)
    }
    return ranges
}

private fun AnnotatedString.Builder.applyLinkStyles(text: String, isInCodeBlock: (Int) -> Boolean) {
    MarkdownFormat.linkRegex.findAll(text).forEach { match ->
        if (isInCodeBlock(match.range.first)) return@forEach
        val linkText = match.groupValues[1]
        addStyle(
            MarkdownFormat.linkText,
            match.range.first,
            match.range.first + linkText.length + 2
        )
        addStyle(
            MarkdownFormat.linkUrl,
            match.range.first + linkText.length + 2,
            match.range.last + 1
        )
    }
}

private fun AnnotatedString.Builder.applyTaskListStyles(
    text: String,
    isInCodeBlock: (Int) -> Boolean
) {
    MarkdownFormat.taskListRegex.findAll(text).forEach { match ->
        if (isInCodeBlock(match.range.first)) return@forEach
        val markerEnd = match.value.indexOf(']') + 1
        addStyle(MarkdownFormat.marker, match.range.first, match.range.first + markerEnd)
    }
}

private fun AnnotatedString.Builder.applyListStyles(text: String, isInCodeBlock: (Int) -> Boolean) {
    MarkdownFormat.listRegex.findAll(text).forEach { match ->
        if (isInCodeBlock(match.range.first)) return@forEach
        val markerLength = match.groupValues[1].length + match.groupValues[2].length + 1
        addStyle(MarkdownFormat.marker, match.range.first, match.range.first + markerLength)
    }
}

private fun AnnotatedString.Builder.applyHeadingStyles(
    text: String,
    isInCodeBlock: (Int) -> Boolean
) {
    MarkdownFormat.headingRegex.findAll(text).forEach { match ->
        if (isInCodeBlock(match.range.first)) return@forEach
        val hashes = match.groupValues[1]
        addStyle(MarkdownFormat.marker, match.range.first, match.range.first + hashes.length)
        addStyle(
            MarkdownFormat.keyword,
            match.range.first + hashes.length + 1,
            match.range.last + 1
        )
    }
}

private fun AnnotatedString.Builder.applyHrStyles(text: String, isInCodeBlock: (Int) -> Boolean) {
    MarkdownFormat.hrRegex.findAll(text).forEach { match ->
        if (isInCodeBlock(match.range.first)) return@forEach
        addStyle(MarkdownFormat.marker, match.range.first, match.range.last + 1)
    }
}

private fun AnnotatedString.Builder.applyGithubAlertStyles(
    text: String,
    isInCodeBlock: (Int) -> Boolean
) {
    MarkdownFormat.githubAlertRegex.findAll(text).forEach { match ->
        if (isInCodeBlock(match.range.first)) return@forEach
        val type = match.groupValues[2]
        val style = when (type) {
            "NOTE" -> MarkdownFormat.noteStyle
            "TIP" -> MarkdownFormat.tipStyle
            "IMPORTANT" -> MarkdownFormat.importantStyle
            "WARNING" -> MarkdownFormat.warningStyle
            "CAUTION" -> MarkdownFormat.cautionStyle
            else -> return@forEach
        }
        addStyle(style, match.range.first, match.range.last + 1)
    }
}