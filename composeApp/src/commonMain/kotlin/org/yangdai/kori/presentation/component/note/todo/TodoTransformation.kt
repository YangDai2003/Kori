package org.yangdai.kori.presentation.component.note.todo

import androidx.compose.foundation.text.input.AnnotatedOutputTransformation
import androidx.compose.foundation.text.input.OutputTransformationAnnotationScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

class TodoTransformation : AnnotatedOutputTransformation {

    // A-Z颜色表
    private val priorityColors = listOf(
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
    private val priorityStyles = (0..25).map { idx ->
        SpanStyle(
            color = priorityColors[idx],
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }

    private val doneStyle =
        SpanStyle(color = Color(0xFF888888), textDecoration = TextDecoration.LineThrough)
    private val dateStyle = SpanStyle(color = Color(0xFF888888))
    private val contextStyle = SpanStyle(color = Color(0xFF009688)) // context: 青色
    private val projectStyle = SpanStyle(color = Color(0xFFEC407A)) // project: 粉色
    private val metaStyle = SpanStyle(color = Color(0xFF8E24AA))

    companion object {
        // 完成任务: 只要以 x 加空格开头即可
        private val doneRegex = Regex("""^x\s""")

        // 优先级: (A)
        private val priorityRegex = Regex("""^\(([A-Z])\)""")

        // 创建日期: 2020-01-01
        private val dateRegex = Regex("""^(\d{4}-\d{2}-\d{2})""")

        // context: @xxx
        private val contextRegex = Regex("""(?:^|\s)(@\S+)""")

        // project: +xxx
        private val projectRegex = Regex("""(?:^|\s)(\+\S+)""")

        // 元数据: key:value
        private val metaRegex = Regex("""(?:^|\s)([^\s:]+:[^\s:]+)""")
    }

    override fun OutputTransformationAnnotationScope.annotateOutput() {
        // 按行处理
        var lineStart = 0
        text.lines().forEach { line ->
            val lineEnd = lineStart + line.length

            // 1. 完成标记
            val doneMatch = doneRegex.find(line)
            if (doneMatch != null) {
                // 整行应用doneStyle
                addAnnotation(doneStyle, lineStart, lineEnd)
            } else {
                // 2. 优先级
                val priMatch = priorityRegex.find(line)
                if (priMatch != null) {
                    val priEnd = priMatch.range.last + 1
                    val idx = priMatch.groupValues[1][0] - 'A'
                    if (idx in 0..25) {
                        addAnnotation(priorityStyles[idx], lineStart, lineStart + priEnd)
                    }
                }
                // 3. 创建日期（优先级后或行首）
                val dateMatch = dateRegex.find(line)
                if (dateMatch != null) {
                    val dateStart = dateMatch.range.first
                    addAnnotation(dateStyle, lineStart + dateStart, lineStart + dateStart + 10)
                }
            }

            // 4. context
            contextRegex.findAll(line).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                addAnnotation(contextStyle, lineStart + start, lineStart + end)
            }
            // 5. project
            projectRegex.findAll(line).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                addAnnotation(projectStyle, lineStart + start, lineStart + end)
            }
            // 6. 元数据 key:value
            metaRegex.findAll(line).forEach {
                val start = it.range.first
                val end = it.range.last + 1
                // 避免 context/project 被重复高亮
                val value = it.groupValues[1]
                if (!value.startsWith("@") && !value.startsWith("+")) {
                    addAnnotation(metaStyle, lineStart + start, lineStart + end)
                }
            }

            lineStart = lineEnd + 1 // +1 for '\n'
        }
    }
}