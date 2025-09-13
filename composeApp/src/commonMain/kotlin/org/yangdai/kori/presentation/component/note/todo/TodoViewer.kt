package org.yangdai.kori.presentation.component.note.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.foundation.text.input.insert
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.priorityColors

data class TodoItem(
    val raw: String,
    val isDone: Boolean,
    val priority: Char?, // A~Z
    val date: String?,   // yyyy-MM-dd
    val sortDate: String?, // 用于排序的日期
    val range: IntRange // 原文本中的范围
)

private val PRIORITY_REGEX = Regex("""^\(([A-Z])\) """)
private val DATE_REGEX = Regex("""(?<=^|\s)(\d{4}-\d{2}-\d{2})(?=\s|$)""")
private val DONE_REGEX = Regex("""^x\s""")
private val DONE_DATE_REGEX =
    Regex("""^x\s(?:\(([A-Z])\)\s)?(\d{4}-\d{2}-\d{2})(?:\s(\d{4}-\d{2}-\d{2}))?""")

suspend fun processTodo(content: CharSequence): Pair<List<TodoItem>, List<TodoItem>> =
    withContext(Dispatchers.Default) {
        val undone = mutableListOf<TodoItem>()
        val done = mutableListOf<TodoItem>()

        content.lines().forEachIndexed { _, line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEachIndexed

            val startIndex = content.indexOf(line)
            if (startIndex == -1) return@forEachIndexed
            val range = startIndex until (startIndex + line.length)

            if (DONE_REGEX.containsMatchIn(trimmed)) {
                val match = DONE_DATE_REGEX.find(trimmed)
                val priority = match?.groups?.get(1)?.value?.firstOrNull()
                val completionDate = match?.groups?.get(2)?.value
                val creationDate = match?.groups?.get(3)?.value
                done.add(TodoItem(trimmed, true, priority, creationDate, completionDate, range))
            } else {
                val priorityMatch = PRIORITY_REGEX.find(trimmed)
                val priority = priorityMatch?.groups?.get(1)?.value?.firstOrNull()
                val textToSearchDate =
                    priorityMatch?.let { trimmed.substring(it.range.last + 1) } ?: trimmed
                val dateMatch = DATE_REGEX.find(textToSearchDate)
                val date = dateMatch?.groups?.get(1)?.value
                undone.add(TodoItem(trimmed, false, priority, date, date, range))
            }
        }

        // 排序逻辑
        val priorityOrder: (Char?) -> Int = { it?.let { c -> c - 'A' } ?: 26 }
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

        Pair(undoneSorted, doneSorted)
    }

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Composable
fun TodoViewer(textFieldState: TextFieldState, modifier: Modifier) {
    var undoneItems by remember { mutableStateOf(listOf<TodoItem>()) }
    var doneItems by remember { mutableStateOf(listOf<TodoItem>()) }

    LaunchedEffect(Unit) {
        snapshotFlow { textFieldState.text }.debounce(100L)
            .mapLatest { processTodo(it) }
            .flowOn(Dispatchers.Default)
            .collect { (undone, done) ->
                undoneItems = undone
                doneItems = done
            }
    }

    LazyColumn(modifier) {
        // 待办
        if (undoneItems.isNotEmpty()) {
            stickyHeader {
                Surface {
                    Text(
                        stringResource(Res.string.todo),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillParentMaxWidth().padding(8.dp)
                    )
                }
            }
            items(undoneItems, key = { it.raw + it.range.first }) { item ->
                SwipeableCard(
                    modifier = Modifier.animateItem().fillParentMaxWidth()
                        .padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    todoItem = item,
                    onDelete = {
                        val end = (it.range.last + 1).coerceAtMost(textFieldState.text.length)
                        val nextCharIsNewline = textFieldState.text.getOrNull(end) == '\n'
                        val deleteUntil = if (nextCharIsNewline) end + 1 else end
                        textFieldState.edit { delete(it.range.first, deleteUntil) }
                    },
                    toggleDoneState = {
                        textFieldState.edit { insert(it.range.first, "x ") }
                    }
                )
            }
        }
        // 已完成
        if (doneItems.isNotEmpty()) {
            stickyHeader {
                Surface {
                    Text(
                        stringResource(Res.string.done),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.fillParentMaxWidth().padding(8.dp)
                    )
                }
            }
            items(doneItems, key = { it.raw + it.range.first }) { item ->
                SwipeableCard(
                    modifier = Modifier.animateItem().fillParentMaxWidth()
                        .padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    todoItem = item,
                    onDelete = {
                        val end = (it.range.last + 1).coerceAtMost(textFieldState.text.length)
                        val nextCharIsNewline = textFieldState.text.getOrNull(end) == '\n'
                        val deleteUntil = if (nextCharIsNewline) end + 1 else end
                        textFieldState.edit { delete(it.range.first, deleteUntil) }
                    },
                    toggleDoneState = {
                        val originalLine = textFieldState.text.substring(item.range)
                        val newLine = originalLine.removePrefix("x ").trimStart()
                        textFieldState.edit {
                            replace(item.range.first, item.range.last + 1, newLine)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SwipeableCard(
    modifier: Modifier,
    todoItem: TodoItem,
    onDelete: (TodoItem) -> Unit,
    toggleDoneState: (TodoItem) -> Unit
) {
    val scope = rememberCoroutineScope()
    val state = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            when (state.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove item",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red, CardDefaults.shape)
                            .wrapContentSize(Alignment.CenterEnd)
                            .padding(12.dp),
                        tint = Color.White
                    )
                }

                else -> {}
            }
        },
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        onDismiss = {
            scope.launch {
                state.reset()
                onDelete(todoItem)
            }
        },
        content = { TodoCard(todoItem, toggleDoneState) }
    )
}

@Composable
private fun TodoCard(todoItem: TodoItem, toggleDoneState: (TodoItem) -> Unit) {
    val priorityColor = todoItem.priority?.let { c ->
        priorityColors[(c - 'A').coerceIn(priorityColors.indices)]
    } ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // 无优先级时给一个中性色

    // 根据是否完成来确定UI样式
    val contentColor = if (todoItem.isDone) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
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

    Card(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.clip(CircleShape).clickable { toggleDoneState(todoItem) },
                contentAlignment = Alignment.Center
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
