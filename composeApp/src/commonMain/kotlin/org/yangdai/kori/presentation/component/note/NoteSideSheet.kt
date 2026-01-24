package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ManageHistory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.UnfoldLess
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kmark.MarkdownElementTypes
import kmark.ast.ASTNode
import kmark.ast.getTextInNode
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.char_count
import kori.composeapp.generated.resources.completed_tasks
import kori.composeapp.generated.resources.drawing
import kori.composeapp.generated.resources.line_count
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.outline
import kori.composeapp.generated.resources.overview
import kori.composeapp.generated.resources.paragraph_count
import kori.composeapp.generated.resources.pending_tasks
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.progress
import kori.composeapp.generated.resources.right_panel_close
import kori.composeapp.generated.resources.settings
import kori.composeapp.generated.resources.snapshots
import kori.composeapp.generated.resources.todo_text
import kori.composeapp.generated.resources.total_tasks
import kori.composeapp.generated.resources.type
import kori.composeapp.generated.resources.word_count
import kori.composeapp.generated.resources.word_count_without_punctuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults
import org.yangdai.kori.presentation.component.note.markdown.Properties.getPropertiesLineRange
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.util.formatNumber
import kotlin.math.abs
import kotlin.math.roundToInt

private val SheetWidth = 320.dp
private val ActionWidth = 48.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NoteSideSheet(
    isDrawerOpen: Boolean,
    onDismiss: () -> Unit,
    noteType: NoteType,
    text: String,
    onHeaderClick: (IntRange) -> Unit,
    navigateTo: (Screen) -> Unit,
    actionContent: @Composable ColumnScope.() -> Unit,
    drawerContent: @Composable ColumnScope.() -> Unit,
    openSnapshots: (() -> Unit)? = null
) {
    if (isDrawerOpen) {
        val scope = rememberCoroutineScope()
        val focusRequester = remember { FocusRequester() }
        var isExiting by remember { mutableStateOf(false) }
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        BoxWithConstraints(Modifier.fillMaxSize().clipToBounds()) {
            val drawerWidth = remember(maxWidth) {
                val maxAllowedWidth = maxWidth - 96.dp
                min(SheetWidth, maxAllowedWidth)
            }
            val drawerWidthPx = with(density) { drawerWidth.toPx() }
            val actionWidthPx = with(density) { ActionWidth.toPx() }
            val paddingWidth =
                WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(layoutDirection)
            val paddingWidthPx = with(density) { paddingWidth.toPx() }
            val fullOffsetPx = drawerWidthPx + actionWidthPx + paddingWidthPx
            val offsetX = remember { Animatable(fullOffsetPx) }

            // isExiting 状态变化时触发退出动画
            LaunchedEffect(isExiting) {
                if (isExiting) {
                    scope.launch {
                        offsetX.animateTo(fullOffsetPx)
                    }.invokeOnCompletion {
                        onDismiss()
                    }
                }
            }

            PredictiveBackHandler(enabled = !isExiting) { progress ->
                try {
                    progress.collect { event ->
                        scope.launch {
                            val newOffset = fullOffsetPx * event.progress
                            offsetX.snapTo(newOffset)
                        }
                    }
                    // 预测性返回手势完成，触发退出
                    if (!isExiting) isExiting = true
                } catch (_: CancellationException) {
                    // 手势取消，恢复到打开状态
                    scope.launch {
                        offsetX.animateTo(0f)
                    }
                }
            }

            val scrimColor = DrawerDefaults.scrimColor
            Canvas(
                Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures { if (!isExiting) isExiting = true }
                }
            ) {
                val alpha = (fullOffsetPx - offsetX.value) / fullOffsetPx * 0.32f
                drawRect(scrimColor.copy(alpha = alpha))
            }

            // 拖动状态
            val dragState = rememberDraggableState { delta ->
                scope.launch {
                    val newValue = (offsetX.value + delta).coerceIn(0f, fullOffsetPx)
                    offsetX.snapTo(newValue)
                }
            }

            val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
            Row(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                            when (keyEvent.key) {
                                Key.Tab -> {
                                    if (!isExiting) isExiting = true
                                    true
                                }

                                else -> false
                            }
                        } else false
                    }
                    .padding(
                        top = systemBarsPadding.calculateTopPadding(),
                        bottom = systemBarsPadding.calculateBottomPadding()
                    )
                    .align(Alignment.CenterEnd)
                    .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Top
            ) {
                // 侧边操作栏 (左侧部分)
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .background(
                            color = DrawerDefaults.modalContainerColor.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.extraLarge.copy(
                                topEnd = CornerSize(0),
                                bottomEnd = CornerSize(0)
                            )
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = actionContent
                )

                // 抽屉内容 (右侧主体)
                Surface(
                    modifier = Modifier.draggable(
                        orientation = Orientation.Horizontal,
                        state = dragState,
                        enabled = !isExiting,
                        reverseDirection = layoutDirection == LayoutDirection.Rtl,
                        onDragStopped = { velocity ->
                            val shouldClose = if (abs(velocity) < 100) {
                                offsetX.value > drawerWidthPx / 2
                            } else {
                                velocity > 0
                            }

                            if (shouldClose) {
                                if (!isExiting) isExiting = true
                            } else {
                                // 动画恢复到打开状态
                                scope.launch {
                                    offsetX.animateTo(0f)
                                }
                            }
                        }
                    ),
                    color = DrawerDefaults.modalContainerColor.copy(alpha = 0.95f),
                    shape = MaterialTheme.shapes.large.copy(
                        topEnd = CornerSize(0),
                        bottomEnd = CornerSize(0)
                    ),
                    shadowElevation = 2.dp,
                    tonalElevation = DrawerDefaults.ModalDrawerElevation
                ) {
                    var isAllExpanded by rememberSaveable { mutableStateOf(true) }

                    val outline by produceState(HeaderNode(), text, noteType) {
                        value =
                            if (noteType == NoteType.MARKDOWN) HeaderNode.fromText(text) else HeaderNode()
                    }

                    Row {
                        LazyColumn(Modifier.fillMaxHeight().width(drawerWidth)) {
                            // 顶部操作按钮
                            item(contentType = "TopBar") {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(top = 4.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        modifier = Modifier.padding(start = 12.dp),
                                        text = stringResource(Res.string.overview),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (openSnapshots != null)
                                            TooltipIconButton(
                                                hint = stringResource(Res.string.snapshots),
                                                icon = Icons.Outlined.ManageHistory,
                                                onClick = openSnapshots
                                            )
                                        TooltipIconButton(
                                            hint = stringResource(Res.string.settings),
                                            icon = Icons.Outlined.Settings,
                                            onClick = { navigateTo(Screen.Settings) }
                                        )
                                        TooltipIconButton(
                                            icon = painterResource(Res.drawable.right_panel_close),
                                            onClick = { if (!isExiting) isExiting = true }
                                        )
                                    }
                                }
                            }

                            // 详细内容
                            item(contentType = "Detail") {
                                SelectionContainer {
                                    Column(Modifier.padding(start = 16.dp, end = 12.dp)) {
                                        NoteSideSheetItem(
                                            key = stringResource(Res.string.type),
                                            value = when (noteType) {
                                                NoteType.PLAIN_TEXT -> stringResource(Res.string.plain_text)
                                                NoteType.MARKDOWN -> stringResource(Res.string.markdown)
                                                NoteType.TODO -> stringResource(Res.string.todo_text)
                                                NoteType.Drawing -> stringResource(Res.string.drawing)
                                            }
                                        )
                                        drawerContent()
                                        if (noteType == NoteType.PLAIN_TEXT || noteType == NoteType.MARKDOWN) {
                                            val textState by produceState(TextState(), text) {
                                                value = TextState.fromText(text)
                                            }
                                            /**文本文件信息：字符数，单词数，行数，段落数**/
                                            NoteSideSheetItem(
                                                key = stringResource(Res.string.char_count),
                                                value = formatNumber(textState.charCount)
                                            )
                                            NoteSideSheetItem(
                                                key = stringResource(Res.string.word_count),
                                                value = formatNumber(textState.wordCountWithPunctuation)
                                            )
                                            NoteSideSheetItem(
                                                key = stringResource(Res.string.word_count_without_punctuation),
                                                value = formatNumber(textState.wordCountWithoutPunctuation)
                                            )
                                            NoteSideSheetItem(
                                                key = stringResource(Res.string.line_count),
                                                value = formatNumber(textState.lineCount)
                                            )
                                            NoteSideSheetItem(
                                                key = stringResource(Res.string.paragraph_count),
                                                value = formatNumber(textState.paragraphCount)
                                            )
                                        } else if (noteType == NoteType.TODO) {
                                            val todoState by produceState(TodoState(), text) {
                                                value = TodoState.fromText(text)
                                            }
                                            /**总任务，已完成，待办，进度**/
                                            NoteSideSheetItem(
                                                key = stringResource(Res.string.total_tasks),
                                                value = todoState.totalTasks.toString()
                                            )
                                            NoteSideSheetItem(
                                                key = stringResource(Res.string.completed_tasks),
                                                value = todoState.completedTasks.toString()
                                            )
                                            NoteSideSheetItem(
                                                key = stringResource(Res.string.pending_tasks),
                                                value = todoState.pendingTasks.toString()
                                            )
                                            NoteSideSheetItem(
                                                key = stringResource(Res.string.progress),
                                                value = "${todoState.progress}%"
                                            )
                                        }
                                    }
                                }
                            }

                            // ... 大纲部分 (outline) ...
                            if (outline.children.isNotEmpty()) {
                                item(contentType = "Divider") {
                                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(end = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(start = 12.dp),
                                            text = stringResource(Res.string.outline),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )

                                        IconButton(onClick = { isAllExpanded = !isAllExpanded }) {
                                            Icon(
                                                imageVector = if (isAllExpanded) Icons.Outlined.UnfoldLess
                                                else Icons.Outlined.UnfoldMore,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }

                                items(outline.children) { header ->
                                    HeaderItem(
                                        header = header,
                                        depth = 0,
                                        onHeaderClick = onHeaderClick,
                                        parentExpanded = isAllExpanded
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.fillMaxHeight().width(paddingWidth))
                    }
                }
            }

            LaunchedEffect(Unit) {
                offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                focusRequester.requestFocus()
            }
        }
    }
}

@Stable
private data class HeaderNode(
    val title: String = "",
    val level: Int = 0,
    val range: IntRange = IntRange.EMPTY,
    val children: MutableList<HeaderNode> = mutableListOf()
) {
    companion object {
        suspend fun fromText(text: String): HeaderNode {
            return if (text.isBlank()) HeaderNode("", 0, IntRange.EMPTY)
            else withContext(Dispatchers.Default) {
                val root = HeaderNode("", 0, IntRange.EMPTY)
                val tree = MarkdownDefaults.parser.buildMarkdownTreeFromString(text)
                val propertiesLineRange = text.getPropertiesLineRange()
                try {
                    val headerStack = mutableListOf(root)
                    findHeadersRecursive(
                        tree,
                        text,
                        headerStack,
                        propertiesLineRange
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                root
            }
        }
    }
}

@Composable
fun NoteSideSheetItem(key: String, value: String) =
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {

        val annotatedString = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                append("$key：")
            }
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                append(value)
            }
        }

        Text(
            text = annotatedString,
            style = MaterialTheme.typography.bodyLarge
        )
    }

@Composable
private fun HeaderItem(
    header: HeaderNode,
    depth: Int,
    parentExpanded: Boolean,
    onHeaderClick: (IntRange) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(parentExpanded) {
        expanded = parentExpanded
    }
    val outlineColor = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeaderClick(header.range) }
            .heightIn(min = 32.dp)
            .drawBehind {
                // 如果深度大于0，才需要绘制线条
                if (depth > 0) {
                    val indentPerLevel = 16.dp.toPx()
                    val iconCenterOffset = 16.dp.toPx()

                    // 根据深度，循环绘制每一层级的垂直对齐线
                    for (i in 0 until depth) {
                        val startX = if (layoutDirection == LayoutDirection.Ltr) {
                            i * indentPerLevel + iconCenterOffset
                        } else {
                            size.width - (i * indentPerLevel + iconCenterOffset)
                        }
                        drawLine(
                            color = outlineColor,
                            start = Offset(startX, 0f),
                            end = Offset(startX, size.height), // 线条高度为当前 Row 的高度
                            cap = StrokeCap.Round
                        )
                    }
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconColor = MaterialTheme.colorScheme.onSurfaceVariant
        Canvas(
            Modifier.padding(start = (depth * 16).dp).size(32.dp)
                .then(
                    if (header.children.isNotEmpty()) {
                        Modifier.clip(CircleShape).clickable { expanded = !expanded }
                    } else {
                        Modifier
                    }
                )
        ) {
            drawCircle(
                color = iconColor,
                radius = 3.dp.toPx(),
                style = if (header.children.isNotEmpty() && !expanded) Fill else Stroke(1.dp.toPx())
            )
        }

        Text(
            text = header.title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis
        )
    }

    if (expanded) {
        header.children.forEach { child ->
            HeaderItem(
                header = child,
                depth = depth + 1,
                onHeaderClick = onHeaderClick,
                parentExpanded = parentExpanded
            )
        }
    }
}

/**
 * Recursively traverses the AST, finds headers, and builds the hierarchy.
 */
private fun findHeadersRecursive(
    node: ASTNode,
    fullText: String,
    headerStack: MutableList<HeaderNode>,
    propertiesRange: IntRange?
) {
    // --- Check if the current node IS a header ---
    val headerLevel = when (node.type) {
        MarkdownElementTypes.ATX_1, MarkdownElementTypes.SETEXT_1 -> 1
        MarkdownElementTypes.ATX_2, MarkdownElementTypes.SETEXT_2 -> 2
        MarkdownElementTypes.ATX_3 -> 3
        MarkdownElementTypes.ATX_4 -> 4
        MarkdownElementTypes.ATX_5 -> 5
        MarkdownElementTypes.ATX_6 -> 6
        else -> 0 // Not a header type we are processing
    }
    if (headerLevel > 0) {
        val range = IntRange(node.startOffset, node.endOffset - 1)
        // --- Skip if inside properties range ---
        if (propertiesRange == null || !propertiesRange.contains(range.first)) {
            val title =
                node.getTextInNode(fullText).trim().trimStart('#')
                    .dropLastWhile { it == '=' || it == '-' }.trimEnd().toString()
            val headerNode = HeaderNode(title, headerLevel, range)
            // --- Manage Hierarchy ---
            // Pop stack until parent level is less than current level
            while (headerStack.last().level >= headerLevel && headerStack.size > 1) {
                headerStack.removeAt(headerStack.lastIndex)
            }
            // Add new header as child of the correct parent
            headerStack.last().children.add(headerNode)
            // Push new header onto stack to be the parent for subsequent deeper headers
            headerStack.add(headerNode)
        }
        return // Stop descent once a header is processed
    }
    // --- If not a header, recurse into children ---
    node.children.forEach { child ->
        findHeadersRecursive(child, fullText, headerStack, propertiesRange)
    }
}

private data class TextState(
    val charCount: Int = 0,
    val wordCountWithPunctuation: Int = 0,
    val wordCountWithoutPunctuation: Int = 0,
    val lineCount: Int = 0,
    val paragraphCount: Int = 0
) {
    companion object {
        private val cjkRanges = arrayOf(
            0x4E00..0x9FFF,  // CJK 统一汉字
            0x3400..0x4DBF,  // CJK 扩展 A
            0x20000..0x2A6DF,// CJK 扩展 B
            0xF900..0xFAFF,  // CJK 兼容汉字
            0x2F800..0x2FA1F // CJK 兼容扩展
        )

        private fun isCJK(ch: Int): Boolean {
            return cjkRanges.any { ch in it }
        }

        private fun isPunctuation(ch: Char): Boolean {
            return ch in "，。、：；？！\"'（）《》「」【】!§$%&/()=?`*_:;><|,.#+~\\´{[]}"
        }

        suspend fun fromText(text: CharSequence): TextState {
            return if (text.isBlank()) TextState()
            else withContext(Dispatchers.Default) {
                val charCount = text.length
                var lineCount = 1
                var paragraphCount = 1
                var punctuationIncludedWordCount = 0
                var nonPunctuationWordCount = 0

                var state = 0
                val inWord = 1
                val previousWasNewline = 2

                var i = 0
                while (i < text.length) {
                    val ch = text[i]
                    when {
                        ch == '\n' -> {
                            state = state and inWord.inv()
                            lineCount++

                            if ((state and previousWasNewline) != 0) {
                                paragraphCount++
                            }
                            state = state or previousWasNewline
                        }

                        ch.isWhitespace() -> {
                            state = state and (inWord or previousWasNewline).inv()
                        }

                        isCJK(ch.code) -> {
                            punctuationIncludedWordCount++
                            nonPunctuationWordCount++
                            state = state and (inWord or previousWasNewline).inv()
                        }

                        isPunctuation(ch) -> {
                            punctuationIncludedWordCount++
                            state = state and (inWord or previousWasNewline).inv()
                        }

                        else -> {
                            if ((state and inWord) == 0) {
                                state = state or inWord
                                punctuationIncludedWordCount++
                                nonPunctuationWordCount++
                            }
                            state = state and previousWasNewline.inv()
                        }
                    }
                    i++
                }

                TextState(
                    charCount = charCount,
                    wordCountWithPunctuation = punctuationIncludedWordCount,
                    wordCountWithoutPunctuation = nonPunctuationWordCount,
                    lineCount = lineCount,
                    paragraphCount = paragraphCount
                )
            }
        }
    }
}

private data class TodoState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val progress: Int = 0
) {
    companion object {
        suspend fun fromText(text: CharSequence): TodoState {
            return if (text.isBlank()) TodoState()
            else withContext(Dispatchers.Default) {
                val lines = text.lines()
                val totalTasks = lines.count { it.isNotBlank() }
                val completedTasks =
                    lines.count {
                        it.trim()
                            .startsWith("x", ignoreCase = true)
                    }
                val pendingTasks = totalTasks - completedTasks
                val progress = if (totalTasks > 0) {
                    (completedTasks.toFloat() / totalTasks.toFloat() * 100).toInt()
                } else {
                    0
                }
                TodoState(
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    pendingTasks = pendingTasks,
                    progress = progress
                )
            }
        }
    }
}