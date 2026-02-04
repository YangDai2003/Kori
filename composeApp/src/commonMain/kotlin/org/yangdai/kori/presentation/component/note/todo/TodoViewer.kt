package org.yangdai.kori.presentation.component.note.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.done
import kori.composeapp.generated.resources.todo
import kori.composeapp.generated.resources.todo_completed_at
import kori.composeapp.generated.resources.todo_created_at
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.note.todo.TodoDefaults.extractFilters
import org.yangdai.kori.presentation.component.note.todo.TodoDefaults.processTodo
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.contextRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.contextStyle
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.doneRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.metaRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.metaStyle
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.priorityColors
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.projectRegex
import org.yangdai.kori.presentation.component.note.todo.TodoFormat.projectStyle
import org.yangdai.kori.presentation.component.note.toggleLineStart

@Stable
data class TodoItem(
    val content: String,
    val isDone: Boolean,
    val priority: Char?, // A~Z
    val creationDate: String?,   // yyyy-MM-dd
    val completionDate: String?, // 用于排序的日期
    val range: IntRange // 原文本中的范围
)

@Stable
object TodoDefaults {
    private val PRIORITY_REGEX = Regex("""^\((?<priority>[A-Z])\) """)
    private val DATE_REGEX = Regex("""(?<=^|\s)(?<date>\d{4}-\d{2}-\d{2})(?=\s|$)""")
    private val DONE_LINE_REGEX =
        Regex("""^x\s(?:\((?<priorityChar>[A-Z])\)\s)?(?<completionDate>\d{4}-\d{2}-\d{2})(?:\s(?<creationDate>\d{4}-\d{2}-\d{2}))?""")

    suspend fun processTodo(content: CharSequence): Pair<List<TodoItem>, List<TodoItem>> =
        withContext(Dispatchers.Default) {
            val undone = mutableListOf<TodoItem>()
            val done = mutableListOf<TodoItem>()

            var currentOffset = 0
            content.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty()) return@forEach

                val range = currentOffset until (currentOffset + line.length)
                val isDone = doneRegex.containsMatchIn(trimmed)
                var content = trimmed

                // Common parsing logic
                var priority: Char? = null
                var creationDateStr: String? = null
                var completionDateStr: String? = null

                if (isDone) {
                    val doneMatch = DONE_LINE_REGEX.find(content)
                    if (doneMatch != null) {
                        priority = doneMatch.groups["priorityChar"]?.value?.firstOrNull()
                        completionDateStr = doneMatch.groups["completionDate"]?.value
                        creationDateStr = doneMatch.groups["creationDate"]?.value
                        // Remove prefix based on what was matched by DONE_LINE_REGEX
                        content = content.substring(doneMatch.range.last + 1).trimStart()
                    } else {
                        // Handle case where it starts with "x " but doesn't match full DONE_LINE_REGEX
                        content = content.removePrefix("x ").trimStart()
                    }
                } else {
                    val priorityMatch = PRIORITY_REGEX.find(content)
                    if (priorityMatch != null) {
                        priority = priorityMatch.groups["priority"]?.value?.firstOrNull()
                        content = content.substring(priorityMatch.range.last + 1).trimStart()
                    }

                    val dateMatch = DATE_REGEX.find(content)
                    if (dateMatch != null) {
                        val dateValue = dateMatch.groups["date"]?.value
                        creationDateStr = dateValue
                        completionDateStr = dateValue
                        content = content.replaceFirst(dateMatch.value, "").trim()
                    }
                }

                val todo =
                    TodoItem(content, isDone, priority, creationDateStr, completionDateStr, range)
                if (isDone) done.add(todo) else undone.add(todo)
                currentOffset += line.length + 1
            }

            // 排序逻辑
            val priorityOrder: (Char?) -> Int = { it?.let { c -> c - 'A' } ?: 26 }
            val dateOrder: (String?) -> String = { it ?: "9999-99-99" }

            val undoneSorted = undone.sortedWith(
                compareBy<TodoItem> { priorityOrder(it.priority) }
                    .thenBy { dateOrder(it.completionDate) }
            )
            val doneSorted = done.sortedWith(
                compareBy<TodoItem> { dateOrder(it.completionDate) }
                    .thenBy { priorityOrder(it.priority) }
            )

            undoneSorted to doneSorted
        }

    // Extract all unique contexts, projects, and metadata from text content
    suspend fun extractFilters(content: CharSequence): Triple<Set<String>, Set<String>, Set<String>> =
        withContext(Dispatchers.Default) {
            val contexts = mutableSetOf<String>()
            val projects = mutableSetOf<String>()
            val metadata = mutableSetOf<String>()

            content.lines().forEach { line ->
                val trimmedLine = line.trim()
                if (trimmedLine.isNotEmpty()) {
                    // Extract contexts (@xxx)
                    contextRegex.findAll(trimmedLine).forEach { match ->
                        contexts.add(match.groupValues[1].trim())
                    }

                    // Extract projects (+xxx)
                    projectRegex.findAll(trimmedLine).forEach { match ->
                        projects.add(match.groupValues[1].trim())
                    }

                    // Extract metadata (key:value)
                    metaRegex.findAll(trimmedLine).forEach { match ->
                        val value = match.groupValues[1].trim()
                        if (!value.startsWith("@") && !value.startsWith("+")) {
                            metadata.add(value)
                        }
                    }
                }
            }

            Triple(contexts, projects, metadata)
        }
}

@OptIn(
    FlowPreview::class, ExperimentalCoroutinesApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun TodoViewer(textFieldState: TextFieldState, modifier: Modifier) {
    var undoneItems by remember { mutableStateOf(listOf<TodoItem>()) }
    var doneItems by remember { mutableStateOf(listOf<TodoItem>()) }

    // Available filters
    var availableContexts by remember { mutableStateOf(emptySet<String>()) }
    var availableProjects by remember { mutableStateOf(emptySet<String>()) }
    var availableMetadata by remember { mutableStateOf(emptySet<String>()) }

    // Active filters
    val activeContexts = remember { mutableStateListOf<String>() }
    val activeProjects = remember { mutableStateListOf<String>() }
    val activeMetadata = remember { mutableStateListOf<String>() }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text }.debounce(200L)
            .mapLatest { text ->
                val (undone, done) = processTodo(text)
                val (contexts, projects, metadata) = extractFilters(text)
                Pair(Pair(undone, done), Triple(contexts, projects, metadata))
            }
            .flowOn(Dispatchers.Default)
            .collect { (todoData, filterData) ->
                val (undone, done) = todoData
                val (contexts, projects, metadata) = filterData

                undoneItems = undone
                doneItems = done
                availableContexts = contexts
                availableProjects = projects
                availableMetadata = metadata
            }
    }

    // Apply filters to items
    val filteredUndoneItems by derivedStateOf {
        if (activeContexts.isEmpty() && activeProjects.isEmpty() && activeMetadata.isEmpty()) {
            undoneItems
        } else {
            undoneItems.filter { item ->
                // Context filter: if active contexts exist, item must contain at least one
                val contextMatch = if (activeContexts.isEmpty()) true else
                    activeContexts.any { context ->
                        contextRegex.findAll(item.content).any { it.value.trim() == context }
                    }

                // Project filter: if active projects exist, item must contain at least one
                val projectMatch = if (activeProjects.isEmpty()) true else
                    activeProjects.any { project ->
                        projectRegex.findAll(item.content).any { it.value.trim() == project }
                    }

                // Metadata filter: if active metadata exist, item must contain at least one
                val metadataMatch = if (activeMetadata.isEmpty()) true else
                    activeMetadata.any { metadata ->
                        metaRegex.findAll(item.content).any { it.value.trim() == metadata }
                    }

                contextMatch && projectMatch && metadataMatch
            }
        }
    }

    val filteredDoneItems by derivedStateOf {
        if (activeContexts.isEmpty() && activeProjects.isEmpty() && activeMetadata.isEmpty()) {
            doneItems
        } else {
            doneItems.filter { item ->
                // Context filter: if active contexts exist, item must contain at least one
                val contextMatch = if (activeContexts.isEmpty()) true else
                    activeContexts.any { context ->
                        contextRegex.findAll(item.content).any { it.value.trim() == context }
                    }

                // Project filter: if active projects exist, item must contain at least one
                val projectMatch = if (activeProjects.isEmpty()) true else
                    activeProjects.any { project ->
                        projectRegex.findAll(item.content).any { it.value.trim() == project }
                    }

                // Metadata filter: if active metadata exist, item must contain at least one
                val metadataMatch = if (activeMetadata.isEmpty()) true else
                    activeMetadata.any { metadata ->
                        metaRegex.findAll(item.content).any { it.value.trim() == metadata }
                    }

                contextMatch && projectMatch && metadataMatch
            }
        }
    }

    var showFilters by remember { mutableStateOf(false) }

    Column(modifier) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            // 待办
            if (filteredUndoneItems.isNotEmpty()) {
                stickyHeader(key = "Header Todo") {
                    Surface {
                        Text(
                            stringResource(Res.string.todo),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.fillParentMaxWidth()
                                .padding(bottom = 8.dp, start = 16.dp, top = 4.dp)
                        )
                    }
                }
                items(filteredUndoneItems, key = { it.hashCode() }) { item ->
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
                            textFieldState.edit { toggleLineStart("x ", it.range.first) }
                        }
                    )
                }
            }
            // 已完成
            if (filteredDoneItems.isNotEmpty()) {
                stickyHeader(key = "Header Done") {
                    Surface {
                        Text(
                            stringResource(Res.string.done),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.fillParentMaxWidth()
                                .padding(bottom = 8.dp, start = 16.dp)
                        )
                    }
                }
                items(filteredDoneItems, key = { it.hashCode() }) { item ->
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
                            textFieldState.edit { toggleLineStart("x ", it.range.first) }
                        }
                    )
                }
            }
        }

        Box(contentAlignment = Alignment.Center) {
            HorizontalDivider()
            IconButton(
                modifier = Modifier.size(
                    IconButtonDefaults.extraSmallContainerSize(
                        widthOption = IconButtonDefaults.IconButtonWidthOption.Uniform
                    )
                ),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                onClick = { showFilters = !showFilters }
            ) {
                Icon(
                    imageVector = if (showFilters) Icons.Outlined.FilterAltOff else Icons.Outlined.FilterAlt,
                    contentDescription = null
                )
            }
        }

        AnimatedVisibility(showFilters) {   // Filter chips section
            TodoFilterChips(
                availableContexts = availableContexts,
                availableProjects = availableProjects,
                availableMetadata = availableMetadata,
                activeContexts = activeContexts,
                activeProjects = activeProjects,
                activeMetadata = activeMetadata,
                onContextClick = { context ->
                    if (activeContexts.contains(context)) {
                        activeContexts.remove(context)
                    } else {
                        activeContexts.add(context)
                    }
                },
                onProjectClick = { project ->
                    if (activeProjects.contains(project)) {
                        activeProjects.remove(project)
                    } else {
                        activeProjects.add(project)
                    }
                },
                onMetadataClick = { metadata ->
                    if (activeMetadata.contains(metadata)) {
                        activeMetadata.remove(metadata)
                    } else {
                        activeMetadata.add(metadata)
                    }
                }
            )
        }
    }
}

@Composable
private fun TodoFilterChips(
    availableContexts: Set<String>,
    availableProjects: Set<String>,
    availableMetadata: Set<String>,
    activeContexts: MutableList<String>,
    activeProjects: MutableList<String>,
    activeMetadata: MutableList<String>,
    onContextClick: (String) -> Unit,
    onProjectClick: (String) -> Unit,
    onMetadataClick: (String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    if (availableContexts.isEmpty() && availableProjects.isEmpty() && availableMetadata.isEmpty()) {
        return
    }

    Column(Modifier.fillMaxWidth()) {
        // Context chips
        if (availableContexts.isNotEmpty()) {
            LazyRow(contentPadding = contentPadding) {
                items(availableContexts.toList()) { context ->
                    FilterChip(
                        selected = activeContexts.contains(context),
                        onClick = { onContextClick(context) },
                        label = { Text(context, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF009688).copy(alpha = 0.1f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = true,
                            selectedBorderColor = Color(0xFF009688)
                        ),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }

        // Project chips
        if (availableProjects.isNotEmpty()) {
            LazyRow(contentPadding = contentPadding) {
                items(availableProjects.toList()) { project ->
                    FilterChip(
                        selected = activeProjects.contains(project),
                        onClick = { onProjectClick(project) },
                        label = { Text(project, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEC407A).copy(alpha = 0.1f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = true,
                            selectedBorderColor = Color(0xFFEC407A)
                        ),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }

        // Metadata chips
        if (availableMetadata.isNotEmpty()) {
            LazyRow(contentPadding = contentPadding) {
                items(availableMetadata.toList()) { metadata ->
                    FilterChip(
                        selected = activeMetadata.contains(metadata),
                        onClick = { onMetadataClick(metadata) },
                        label = { Text(metadata, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF8E24AA).copy(alpha = 0.1f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = true,
                            selectedBorderColor = Color(0xFF8E24AA)
                        ),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
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
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            MaterialTheme.colorScheme.errorContainer,
                            CardDefaults.shape
                        ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove task",
                            modifier = Modifier.padding(end = 12.dp).size(20.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                SwipeToDismissBoxValue.StartToEnd -> {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            CardDefaults.shape
                        ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Icon(
                            if (todoItem.isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = if (todoItem.isDone) "Done" else "Not done",
                            modifier = Modifier.padding(start = 12.dp).size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {}
            }
        },
        modifier = modifier,
        onDismiss = { swipeToDismissBoxValue ->
            when (swipeToDismissBoxValue) {
                SwipeToDismissBoxValue.EndToStart -> onDelete(todoItem)
                SwipeToDismissBoxValue.StartToEnd -> toggleDoneState(todoItem)
                else -> {}
            }
            scope.launch { state.snapTo(SwipeToDismissBoxValue.Settled) }
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

    // 时间显示（本地化）
    val dateLabel = if (todoItem.isDone) {
        todoItem.completionDate?.let { date ->
            stringResource(Res.string.todo_completed_at, date)
        }
    } else {
        todoItem.creationDate?.let { date ->
            stringResource(Res.string.todo_created_at, date)
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
                    text = buildAnnotatedString {
                        val text = todoItem.content
                        append(text)
                        // 高亮 context
                        contextRegex.findAll(text).forEach { matchResult ->
                            addStyle(
                                style = contextStyle,
                                start = matchResult.range.first,
                                end = matchResult.range.last + 1
                            )
                        }

                        // 高亮 project
                        projectRegex.findAll(text).forEach { matchResult ->
                            addStyle(
                                style = projectStyle,
                                start = matchResult.range.first,
                                end = matchResult.range.last + 1
                            )
                        }

                        metaRegex.findAll(text).forEach { matchResult ->
                            addStyle(
                                style = metaStyle,
                                start = matchResult.range.first,
                                end = matchResult.range.last + 1
                            )
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(textDecoration = textDecoration),
                    color = contentColor,
                )
                if (!dateLabel.isNullOrBlank()) {
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