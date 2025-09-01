package org.yangdai.kori.presentation.component.note.todo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.done
import kori.composeapp.generated.resources.todo
import kori.composeapp.generated.resources.todo_completed_at
import kori.composeapp.generated.resources.todo_scheduled_at
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.note.TodoItem

@Composable
fun TodoViewer(
    undoneItems: List<TodoItem>,
    doneItems: List<TodoItem>,
    modifier: Modifier,
) = LazyColumn(modifier = modifier) {
    // 待办
    if (undoneItems.isNotEmpty()) {
        stickyHeader {
            Text(
                stringResource(Res.string.todo),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
        items(undoneItems) { item ->
            TodoCard(item)
        }
    }
    // 已完成
    if (doneItems.isNotEmpty()) {
        stickyHeader {
            Text(
                stringResource(Res.string.done),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
        items(doneItems) { item ->
            TodoCard(item)
        }
    }
}

@Composable
private fun TodoCard(todoItem: TodoItem) {
    // 优先级颜色表保持不变
    val priorityColors = listOf(
        Color(0xFFD32F2F), Color(0xFFF57C00), Color(0xFFFBC02D), Color(0xFF388E3C),
        Color(0xFF1976D2), Color(0xFF7B1FA2), Color(0xFF0097A7), Color(0xFF5D4037),
        Color(0xFF0288D1), Color(0xFF388E3C), Color(0xFF8D6E63), Color(0xFF455A64),
        Color(0xFFAFB42B), Color(0xFFEC407A), Color(0xFF00ACC1), Color(0xFF43A047),
        Color(0xFF6D4C41), Color(0xFF7E57C2), Color(0xFF26A69A), Color(0xFF9E9D24),
        Color(0xFF8E24AA), Color(0xFF3949AB), Color(0xFFD84315), Color(0xFF00897B),
        Color(0xFF6D4C41), Color(0xFFBDBDBD)
    )
    val priorityColor = todoItem.priority?.let { c ->
        val idx = (c - 'A').coerceIn(0, 25)
        priorityColors[idx]
    } ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // 无优先级时给一个中性色

    // 根据是否完成来确定UI样式
    val contentColor = if (todoItem.isDone) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val dateColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textDecoration = if (todoItem.isDone) TextDecoration.LineThrough else null

    // 提取内容
    val content = remember(todoItem.raw) {
        var s = todoItem.raw
        if (todoItem.isDone) {
            s = s.removePrefix("x ").trimStart()
            s = s.replace(Regex("""^\d{4}-\d{2}-\d{2}( \d{4}-\d{2}-\d{2})? """), "")
        }
        s = s.replace(Regex("""^\([A-Z]\) """), "")
        s = s.replace(Regex("""^\d{4}-\d{2}-\d{2} """), "")
        s.trim()
    }

    // 时间显示（本地化）
    val dateLabel = if (todoItem.isDone) {
        todoItem.sortDate?.let { date ->
            stringResource(Res.string.todo_completed_at, date)
        }
    } else {
        todoItem.date?.let { date ->
            stringResource(Res.string.todo_scheduled_at, date)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 优先级圆点或已完成图标
            if (todoItem.isDone) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Circle,
                    contentDescription = null,
                    tint = priorityColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = textDecoration
                    ),
                    color = contentColor,
                    maxLines = 3
                )
                if (!dateLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = dateColor
                    )
                }
            }
        }
    }
}
