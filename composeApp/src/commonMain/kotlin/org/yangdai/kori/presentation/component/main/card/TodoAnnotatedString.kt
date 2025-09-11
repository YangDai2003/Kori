package org.yangdai.kori.presentation.component.main.card

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.Bullet
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.em
import org.yangdai.kori.presentation.component.note.todo.TodoFormat

fun buildTodoAnnotatedString(text: String) =
    buildAnnotatedString {
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

private fun taskBullet(isDone: Boolean, color: Color) =
    if (isDone)
        Bullet(
            CheckedBoxShape(),
            0.7.em,
            0.7.em,
            0.25.em,
            SolidColor(color),
            drawStyle = Stroke(width = 3f, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
    else
        Bullet(
            UncheckedBoxShape(),
            0.7.em,
            0.7.em,
            0.25.em,
            SolidColor(color),
            drawStyle = Stroke(width = 3f, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )

/**
 * A shape representing a checked box with a checkmark.
 */
private class CheckedBoxShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadius = size.width * 0.2f
        val path = Path().apply {
            // Draw the outer box
            addRoundRect(
                RoundRect(
                    Rect(0f, 0f, size.width, size.height),
                    CornerRadius(cornerRadius)
                )
            ) // Draw the checkmark
            moveTo(size.width * 0.25f, size.height * 0.5f)
            lineTo(size.width * 0.45f, size.height * 0.7f)
            lineTo(size.width * 0.75f, size.height * 0.3f)
        }
        return Outline.Generic(path)
    }
}

/**
 * A shape representing an unchecked box.
 */
private class UncheckedBoxShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadius = size.width * 0.2f
        return Outline.Rounded(
            RoundRect(
                Rect(0f, 0f, size.width, size.height),
                CornerRadius(cornerRadius)
            )
        )
    }
}