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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

// 数据模型
private data class TodoItem(
    val raw: String,
    val isDone: Boolean,
    val priority: Char?, // A~Z
    val date: String?,   // yyyy-MM-dd
    val sortDate: String?, // 用于排序的日期
)

private fun parseTodoLines(lines: List<String>): Pair<List<TodoItem>, List<TodoItem>> {
    val priorityRegex = Regex("""^\(([A-Z])\) """)
    val dateRegex = Regex("""(?<=^|\s)(\d{4}-\d{2}-\d{2})(?=\s|$)""")
    val doneRegex = Regex("""^x\s""")
    val doneDateRegex =
        Regex("""^x\s(?:\(([A-Z])\)\s)?(\d{4}-\d{2}-\d{2})(?:\s(\d{4}-\d{2}-\d{2}))?""")

    val undone = mutableListOf<TodoItem>()
    val done = mutableListOf<TodoItem>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue

        if (doneRegex.containsMatchIn(trimmed)) {
            var priority: Char? = null
            var date: String? = null // 创建日期
            var sortDate: String? = null // 完成日期

            val m = doneDateRegex.find(trimmed)
            if (m != null) {
                // groupValues 索引: [0]是完整匹配, [1]是优先级, [2]是完成日期, [3]是创建日期
                val priorityStr = m.groupValues.getOrNull(1)
                if (!priorityStr.isNullOrEmpty()) {
                    priority = priorityStr[0]
                }
                sortDate = m.groupValues.getOrNull(2) // 完成日期，用于排序
                date = m.groupValues.getOrNull(3)     // 创建日期 (可选)
            }
            done.add(TodoItem(trimmed, true, priority, date, sortDate))
        } else {
            var priority: Char? = null
            var date: String? = null

            val priMatch = priorityRegex.find(trimmed)
            if (priMatch != null) {
                priority = priMatch.groupValues[1][0]
            }

            // 从优先级之后开始查找
            val textToSearchDate =
                if (priMatch != null) trimmed.substring(priMatch.range.last + 1) else trimmed
            val dateMatch = dateRegex.find(textToSearchDate)
            if (dateMatch != null) {
                date = dateMatch.groupValues[1]
            }
            undone.add(TodoItem(trimmed, false, priority, date, date))
        }
    }

    // 排序逻辑 (保持不变)
    val priorityOrder: (Char?) -> Int = { c -> if (c == null) 26 else (c - 'A') }
    val dateOrder: (String?) -> String = { it ?: "9999-99-99" }
    val undoneSorted = undone.sortedWith(
        compareBy<TodoItem> { priorityOrder(it.priority) }
            .thenBy { dateOrder(it.sortDate) }
    )
    val doneSorted = done.sortedWith(
        // 已完成任务通常按完成日期倒序排列，但这里按正序，与您原逻辑保持一致
        compareBy<TodoItem> { dateOrder(it.sortDate) }
            .thenBy { priorityOrder(it.priority) }
    )
    return Pair(undoneSorted, doneSorted)
}

@Composable
fun TodoViewer(
    todoText: String,
    modifier: Modifier = Modifier,
) {
    var undone by remember { mutableStateOf(listOf<TodoItem>()) }
    var done by remember { mutableStateOf(listOf<TodoItem>()) }

    LaunchedEffect(todoText) {
        withContext(Dispatchers.Default) {
            val (u, d) = parseTodoLines(todoText.lines())
            withContext(Dispatchers.Main) {
                undone = u
                done = d
            }
        }
    }

    LazyColumn(modifier = modifier) {
        // 待办
        if (undone.isNotEmpty()) {
            stickyHeader {
                Text(
                    stringResource(Res.string.todo),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
            items(undone) { item ->
                TodoCard(item)
            }
        }
        // 已完成
        if (done.isNotEmpty()) {
            stickyHeader {
                Text(
                    stringResource(Res.string.done),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
            items(done) { item ->
                TodoCard(item)
            }
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
