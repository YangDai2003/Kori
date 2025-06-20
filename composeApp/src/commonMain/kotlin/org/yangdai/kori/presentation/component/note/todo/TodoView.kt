package org.yangdai.kori.presentation.component.note.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val dateRegex = Regex("""^(\d{4}-\d{2}-\d{2}) """)
    val doneRegex = Regex("""^x\s""")
    val doneDateRegex = Regex("""^x\s(\d{4}-\d{2}-\d{2})(?:\s(\d{4}-\d{2}-\d{2}))?""")

    val undone = mutableListOf<TodoItem>()
    val done = mutableListOf<TodoItem>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        val isDone = doneRegex.containsMatchIn(trimmed)
        var priority: Char? = null
        var date: String? = null
        var sortDate: String? = null

        if (isDone) {
            // 完成任务
            val m = doneDateRegex.find(trimmed)
            if (m != null) {
                // m.groupValues[1] 完成日期, m.groupValues[2] 创建日期(可选)
                date = m.groupValues.getOrNull(2)
                sortDate = m.groupValues.getOrNull(1) // 按完成日期排序
            }
            // 查找优先级（完成任务优先级可能被保留）
            val afterDone = trimmed.removePrefix("x ").trimStart()
            val priMatch = priorityRegex.find(afterDone)
            if (priMatch != null) {
                priority = priMatch.groupValues[1][0]
            }
            done.add(TodoItem(trimmed, true, priority, date, sortDate))
        } else {
            // 未完成任务
            val priMatch = priorityRegex.find(trimmed)
            if (priMatch != null) {
                priority = priMatch.groupValues[1][0]
            }
            val dateMatch = dateRegex.find(trimmed.removePrefix("(${priority ?: ""}) ").trimStart())
            if (dateMatch != null) {
                date = dateMatch.groupValues[1]
            }
            sortDate = date
            undone.add(TodoItem(trimmed, false, priority, date, sortDate))
        }
    }
    // 排序
    val priorityOrder: (Char?) -> Int = { c -> if (c == null) 26 else (c - 'A') }
    val dateOrder: (String?) -> String = { it ?: "9999-99-99" }
    val undoneSorted = undone.sortedWith(
        compareBy<TodoItem> { priorityOrder(it.priority) }
            .thenBy { dateOrder(it.sortDate) }
    )
    val doneSorted = done.sortedWith(
        compareBy<TodoItem> { priorityOrder(it.priority) }
            .thenBy { dateOrder(it.sortDate) }
    )
    return Pair(undoneSorted, doneSorted)
}

@Composable
fun TodoView(
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
                    "待办",
                    style = MaterialTheme.typography.subtitle1,
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
                    "已完成",
                    style = MaterialTheme.typography.subtitle1,
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
    // 优先级颜色表（与TodoTransformation一致）
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
    } ?: Color(0xFFBDBDBD)

    val cardAlpha = if (todoItem.isDone) 0.5f else 1f
    val textColor = if (todoItem.isDone) Color(0xFF888888) else Color.Unspecified
    val textDecoration = if (todoItem.isDone) TextDecoration.LineThrough else null

    // 提取内容（去掉优先级、日期、完成标记等前缀）
    val content = remember(todoItem.raw) {
        var s = todoItem.raw
        if (todoItem.isDone) {
            s = s.removePrefix("x ").trimStart()
            // 去掉完成日期和创建日期
            s = s.replace(Regex("""^\d{4}-\d{2}-\d{2}( \d{4}-\d{2}-\d{2})? """), "")
        }
        // 去掉优先级
        s = s.replace(Regex("""^\([A-Z]\) """), "")
        // 去掉日期
        s = s.replace(Regex("""^\d{4}-\d{2}-\d{2} """), "")
        s.trim()
    }

    // 时间显示
    val dateLabel = if (todoItem.isDone) {
        // 完成日期
        todoItem.sortDate?.let { "完成于 $it" }
    } else {
        todoItem.date?.let { "计划 $it" }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .alpha(cardAlpha),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 优先级圆点或已完成图标
            if (todoItem.isDone) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "已完成",
                    tint = priorityColor,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(priorityColor, shape = RoundedCornerShape(8.dp))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = content,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = textDecoration
                    ),
                    maxLines = 3
                )
                if (!dateLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateLabel,
                        style = TextStyle(
                            color = Color(0xFF888888),
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}
