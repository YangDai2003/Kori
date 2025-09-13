package org.yangdai.kori.presentation.component.note.todo

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.contextRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.contextStyle
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.dateRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.dateStyle
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.doneRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.doneStyle
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.metaRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.metaStyle
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.priorityRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.priorityStyles
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.projectRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.projectStyle

class TodoTransformation : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        // 按行处理
        var lineStart = 0
        originalText.lineSequence().forEach { line ->
            val lineEnd = lineStart + line.length

            // 1. 完成标记
            val doneMatch = doneRegex.find(line)
            if (doneMatch != null) {
                // 整行应用doneStyle
                addStyle(doneStyle, lineStart, lineEnd)
            } else {
                // 2. 优先级
                val priMatch = priorityRegex.find(line)
                if (priMatch != null) {
                    val priEnd = priMatch.range.last + 1
                    val idx = priMatch.groupValues[1][0] - 'A'
                    if (idx in 0..25) {
                        addStyle(priorityStyles[idx], lineStart, lineStart + priEnd)
                    }
                }
                // 3. 创建日期（行中任意位置）
                dateRegex.findAll(line).forEach { dateMatch ->
                    val dateStart = dateMatch.range.first
                    addStyle(dateStyle, lineStart + dateStart, lineStart + dateStart + 10)
                }
            }

            // 4. context
            contextRegex.findAll(line).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                addStyle(contextStyle, lineStart + start, lineStart + end)
            }
            // 5. project
            projectRegex.findAll(line).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                addStyle(projectStyle, lineStart + start, lineStart + end)
            }
            // 6. 元数据 key:value
            metaRegex.findAll(line).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                // 避免 context/project 被重复���亮
                val value = it.groupValues[1]
                if (!value.startsWith("@") && !value.startsWith("+")) {
                    addStyle(metaStyle, lineStart + start, lineStart + end)
                }
            }

            lineStart = lineEnd + 1 // +1 for '\n'
        }
    }
}

object TodoFormat {
    // A-Z颜色表
    val priorityColors = listOf(
        0xFFD32F2F, // A 红
        0xFFF57C00, // B 橙
        0xFFFBC02D, // C 黄
        0xFF388E3C, // D 绿
        0xFF1976D2, // E 蓝
        0xFF7B1FA2, // F 紫
        0xFF0097A7, // G 青
        0xFF5D4037, // H 棕
        0xFF0288D1, // I 靛
        0xFF388E3C, // J 绿
        0xFF8D6E63, // K 浅棕
        0xFF455A64, // L 蓝灰
        0xFFAFB42B, // M 橄榄
        0xFFEC407A, // N 粉
        0xFF00ACC1, // O 青
        0xFF43A047, // P 绿
        0xFF6D4C41, // Q 棕
        0xFF7E57C2, // R 紫
        0xFF26A69A, // S 蓝绿
        0xFF9E9D24, // T 黄绿
        0xFF8E24AA, // U 紫
        0xFF3949AB, // V 蓝
        0xFFD84315, // W 橙红
        0xFF00897B, // X 蓝绿
        0xFF6D4C41, // Y 棕
        0xFFBDBDBD  // Z 灰
    ).map { Color(it) }

    // 优先级样式A-Z
    val priorityStyles = (0..25).map { idx ->
        SpanStyle(
            color = priorityColors[idx],
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }

    val doneStyle =
        SpanStyle(color = Color(0xFF888888), textDecoration = TextDecoration.LineThrough)
    val dateStyle = SpanStyle(color = Color(0xFF888888))
    val contextStyle = SpanStyle(color = Color(0xFF009688)) // context: 青色
    val projectStyle = SpanStyle(color = Color(0xFFEC407A)) // project: 粉色
    val metaStyle = SpanStyle(color = Color(0xFF8E24AA))

    // 完成任务: 只要以 x 加空格开头即可
    val doneRegex = Regex("""^x\s""")

    // 优先级: (A)
    val priorityRegex = Regex("""^\(([A-Z])\)""")

    // 创建日期: 行中任意位置，前有空白或行首，后跟空格或行尾
    val dateRegex = Regex("""(?<=^|\s)(\d{4}-\d{2}-\d{2})(?=\s|$)""")

    // context: @xxx
    val contextRegex = Regex("""(?:^|\s)(@\S+)""")

    // project: +xxx
    val projectRegex = Regex("""(?:^|\s)(\+\S+)""")

    // 元数据: key:value
    val metaRegex = Regex("""(?:^|\s)([^\s:]+:[^\s:]+)""")
}