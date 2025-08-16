package org.yangdai.kori.presentation.component.main.card

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import org.yangdai.kori.presentation.component.note.todo.TodoFormat
import org.yangdai.kori.presentation.screen.settings.CardSize

@Composable
fun TodoText(text: String, noteItemProperties: NoteItemProperties) {
    val annotatedString = buildAnnotatedString {
        append(text)
        var lineStart = 0
        text.lineSequence().forEach { line ->
            val lineEnd = lineStart + line.length
            // 检查完成标记
            val doneMatch = TodoFormat.doneRegex.find(line)
            if (doneMatch != null) {
                // 已完成的任务，整行应用删除线样式
                addStyle(TodoFormat.doneStyle, lineStart, lineEnd)
            } else {
                // 未完成的任务，检查优先级和日期
                // 优先级: (A)
                val priMatch = TodoFormat.priorityRegex.find(line)
                if (priMatch != null) {
                    val priEnd = priMatch.range.last + 1
                    val idx = priMatch.groupValues[1][0] - 'A'
                    if (idx in 0..25) {
                        addStyle(TodoFormat.priorityStyles[idx], lineStart, lineStart + priEnd)
                    }
                }
                // 创建日期: YYYY-MM-DD
                TodoFormat.dateRegex.findAll(line).forEach { dateMatch ->
                    val dateStart = dateMatch.range.first
                    val dateEnd = dateMatch.range.last + 1
                    addStyle(TodoFormat.dateStyle, lineStart + dateStart, lineStart + dateEnd)
                }
            }

            // 对所有行（无论是否完成）都应用以下样式
            // 上下文: @context
            TodoFormat.contextRegex.findAll(line).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                addStyle(TodoFormat.contextStyle, lineStart + start, lineStart + end)
            }
            // 项目: +project
            TodoFormat.projectRegex.findAll(line).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                addStyle(TodoFormat.projectStyle, lineStart + start, lineStart + end)
            }
            // 元数据: key:value
            TodoFormat.metaRegex.findAll(line).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                val value = it.groupValues[1]
                // 避免与 context/project 重复高亮
                if (!value.startsWith("@") && !value.startsWith("+")) {
                    addStyle(TodoFormat.metaStyle, lineStart + start, lineStart + end)
                }
            }

            lineStart = lineEnd + 1 // +1 for '\n'
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = if (noteItemProperties.cardSize == CardSize.DEFAULT) 5 else 2,
        overflow = if (noteItemProperties.clipOverflow) TextOverflow.Clip else TextOverflow.Ellipsis
    )
}