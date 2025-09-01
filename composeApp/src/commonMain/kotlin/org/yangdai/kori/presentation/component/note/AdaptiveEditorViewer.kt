package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownTransformation
import org.yangdai.kori.presentation.component.note.markdown.MarkdownViewer
import org.yangdai.kori.presentation.component.note.markdown.markdownKeyEvents
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextTransformation
import org.yangdai.kori.presentation.component.note.plaintext.plainTextKeyEvents
import org.yangdai.kori.presentation.component.note.todo.TodoTransformation
import org.yangdai.kori.presentation.component.note.todo.TodoViewer
import org.yangdai.kori.presentation.component.note.todo.todoTextKeyEvents
import kotlin.math.abs

@Composable
fun ColumnScope.AdaptiveEditorViewer(
    isLargeScreen: Boolean,
    pagerState: PagerState,
    editor: @Composable (Modifier) -> Unit,
    viewer: (@Composable (Modifier) -> Unit)?
) = if (viewer == null) {
    editor(Modifier.fillMaxWidth().weight(1f))
} else {
    if (isLargeScreen) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            var editorWeight by remember { mutableFloatStateOf(0.5f) }
            val windowWidth = LocalWindowInfo.current.containerSize.width

            editor(Modifier.fillMaxHeight().weight(editorWeight))

            VerticalDragHandle(
                modifier = Modifier
                    .sizeIn(maxWidth = 12.dp, minWidth = 4.dp)
                    .draggable(
                        interactionSource = interactionSource,
                        state = rememberDraggableState { delta ->
                            editorWeight =
                                (editorWeight + delta / windowWidth)
                                    .coerceIn(0.15f, 0.85f)
                        },
                        orientation = Orientation.Horizontal,
                        onDragStopped = {
                            val positions = listOf(0.2f, 1f / 3f, 0.5f, 2f / 3f, 0.8f)
                            val closest =
                                positions.minByOrNull { abs(it - editorWeight) }
                            if (closest != null) {
                                editorWeight = closest
                            }
                        }
                    ),
                interactionSource = interactionSource
            )

            viewer(Modifier.fillMaxHeight().weight(1f - editorWeight))
        }
    } else {
        HorizontalPager(
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = pagerState,
            beyondViewportPageCount = 1,
            userScrollEnabled = false
        ) { currentPage ->
            when (currentPage) {
                0 -> editor(Modifier.fillMaxSize())
                1 -> viewer(Modifier.fillMaxSize())
            }
        }
    }
}

/**
 * A composable function that renders an adaptive editor based on the provided `NoteType`.
 *
 * @param modifier Modifier to be applied to the editor.
 * @param noteType The type of note, which determines the editor to be displayed (Markdown, Plain Text...).
 * @param textFieldState The state of the text field used in the editor.
 * @param scrollState The scroll state for the editor.
 * @param isReadOnly Whether the editor is in read-only mode.
 * @param isLineNumberVisible Whether line numbers are visible in the editor.
 * @param isLintActive Whether linting is active in the editor.
 * @param headerRange The range of headers for Markdown editor, if applicable.
 * @param findAndReplaceState The state for find-and-replace functionality in the editor.
 */
@Composable
fun AdaptiveEditor(
    modifier: Modifier,
    noteType: NoteType,
    textFieldState: TextFieldState,
    scrollState: ScrollState,
    isReadOnly: Boolean,
    isLineNumberVisible: Boolean,
    isLintActive: Boolean,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState
) = when (noteType) {
    NoteType.MARKDOWN -> {
        TextEditor(
            modifier = modifier,
            textFieldModifier = Modifier.markdownKeyEvents(textFieldState)
                .dragAndDropText(textFieldState),
            textState = textFieldState,
            scrollState = scrollState,
            findAndReplaceState = findAndReplaceState,
            headerRange = headerRange,
            editorProperties = EditorProperties(
                isReadOnly = isReadOnly,
                isLineNumberVisible = isLineNumberVisible,
                isLintActive = isLintActive
            ),
            outputTransformation = remember { MarkdownTransformation() }
        )
    }

    NoteType.TODO -> {
        TextEditor(
            modifier = modifier,
            textFieldModifier = Modifier.todoTextKeyEvents(textFieldState)
                .dragAndDropText(textFieldState),
            textState = textFieldState,
            scrollState = scrollState,
            findAndReplaceState = findAndReplaceState,
            editorProperties = EditorProperties(
                isReadOnly = isReadOnly,
                isLineNumberVisible = isLineNumberVisible
            ),
            outputTransformation = remember { TodoTransformation() }
        )
    }

    NoteType.PLAIN_TEXT -> {
        TextEditor(
            modifier = modifier,
            textFieldModifier = Modifier.plainTextKeyEvents(textFieldState)
                .dragAndDropText(textFieldState),
            textState = textFieldState,
            scrollState = scrollState,
            findAndReplaceState = findAndReplaceState,
            editorProperties = EditorProperties(
                isReadOnly = isReadOnly,
                isLineNumberVisible = isLineNumberVisible
            ),
            outputTransformation = remember { PlainTextTransformation() }
        )
    }

    else -> {}
}

@Composable
fun AdaptiveViewer(
    modifier: Modifier,
    processedContent: ProcessedContent,
    scrollState: ScrollState,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>
) = when (processedContent) {
    ProcessedContent.Empty -> Spacer(modifier)
    is ProcessedContent.Markdown -> {
        MarkdownViewer(
            modifier = modifier,
            html = processedContent.html,
            scrollState = scrollState,
            isSheetVisible = isSheetVisible,
            printTrigger = printTrigger
        )
    }

    is ProcessedContent.Todo -> {
        TodoViewer(
            modifier = modifier,
            undoneItems = processedContent.undoneItems,
            doneItems = processedContent.doneItems
        )
    }
}