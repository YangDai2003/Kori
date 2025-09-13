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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.control
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownLint
import org.yangdai.kori.presentation.component.note.markdown.MarkdownTransformation
import org.yangdai.kori.presentation.component.note.markdown.MarkdownViewer
import org.yangdai.kori.presentation.component.note.markdown.markdownKeyEvents
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextTransformation
import org.yangdai.kori.presentation.component.note.plaintext.plainTextKeyEvents
import org.yangdai.kori.presentation.component.note.todo.TodoLint
import org.yangdai.kori.presentation.component.note.todo.TodoTransformation
import org.yangdai.kori.presentation.component.note.todo.TodoViewer
import org.yangdai.kori.presentation.component.note.todo.todoTextKeyEvents
import kotlin.math.abs

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.AdaptiveEditorViewer(
    isLargeScreen: Boolean,
    pagerState: PagerState,
    editor: @Composable (Modifier) -> Unit,
    viewer: (@Composable (Modifier) -> Unit)?
) = if (viewer == null) {
    editor(Modifier.fillMaxWidth().weight(1f))
} else {
    if (isLargeScreen)
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            var editorWeight by remember { mutableFloatStateOf(0.5f) }
            val containerWidth = LocalWindowInfo.current.containerSize.width

            val anchorPoints =
                rememberSaveable { listOf(0f, 0.2f, 1f / 3f, 0.5f, 2f / 3f, 0.8f, 1f) }

            if (editorWeight > 0.1f)
                editor(
                    Modifier.fillMaxHeight().weight(editorWeight).onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyDown && it.isCtrlPressed && it.isShiftPressed) {
                            val currentAnchorIndex = anchorPoints.indexOf(editorWeight)
                            when (it.key) {
                                Key.DirectionLeft -> {
                                    if (currentAnchorIndex > 0) editorWeight =
                                        anchorPoints[currentAnchorIndex - 1]
                                    true
                                }

                                Key.DirectionRight -> {
                                    if (currentAnchorIndex < anchorPoints.lastIndex) editorWeight =
                                        anchorPoints[currentAnchorIndex + 1]
                                    true
                                }

                                else -> false
                            }
                        } else false
                    }
                )

            TooltipBox(
                positionProvider = TooltipDefaults
                    .rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                tooltip = { PlainTooltip { Text("${stringResource(Res.string.control)} + ⇧ + ↔︎") } },
                state = rememberTooltipState()
            ) {
                VerticalDragHandle(
                    modifier = Modifier
                        .sizeIn(maxWidth = 12.dp, minWidth = 4.dp)
                        .draggable(
                            interactionSource = interactionSource,
                            state = rememberDraggableState { delta ->
                                editorWeight = (editorWeight + delta / containerWidth)
                            },
                            orientation = Orientation.Horizontal,
                            onDragStopped = {
                                val closestAnchor = anchorPoints.minBy { abs(it - editorWeight) }
                                editorWeight = closestAnchor
                            }
                        ),
                    interactionSource = interactionSource
                )
            }

            if (editorWeight < 0.9f) viewer(Modifier.fillMaxHeight().weight(1f - editorWeight))
        }
    else
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

/**
 * A composable function that renders an adaptive editor based on the provided `NoteType`.
 *
 * @param modifier Modifier to be applied to the editor.
 * @param noteType The type of note, which determines the editor to be displayed (Markdown, Plain Text...).
 * @param textFieldState The state of the text field used in the editor.
 * @param scrollState The scroll state for the editor.
 * @param readOnly Whether the editor is in read-only mode.
 * @param isLineNumberVisible Whether line numbers are visible in the editor.
 * @param isLintingEnabled Whether linting is active in the editor.
 * @param headerRange The range of headers for Markdown editor, if applicable.
 * @param findAndReplaceState The state for find-and-replace functionality in the editor.
 */
@Composable
fun AdaptiveEditor(
    modifier: Modifier,
    noteType: NoteType,
    textFieldState: TextFieldState,
    scrollState: ScrollState,
    readOnly: Boolean,
    isLineNumberVisible: Boolean,
    isLintingEnabled: Boolean,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState
) {
    val textFieldModifier = remember(noteType, textFieldState) {
        when (noteType) {
            NoteType.MARKDOWN -> Modifier.markdownKeyEvents(textFieldState)
            NoteType.TODO -> Modifier.todoTextKeyEvents(textFieldState)
            NoteType.PLAIN_TEXT -> Modifier.plainTextKeyEvents(textFieldState)
            else -> Modifier
        }
    }

    val outputTransformation = remember(noteType) {
        when (noteType) {
            NoteType.MARKDOWN -> MarkdownTransformation()
            NoteType.TODO -> TodoTransformation()
            NoteType.PLAIN_TEXT -> PlainTextTransformation()
            else -> null
        }
    }

    val lint = remember(noteType, isLintingEnabled) {
        if (isLintingEnabled) {
            when (noteType) {
                NoteType.MARKDOWN -> MarkdownLint()
                NoteType.TODO -> TodoLint()
                else -> null
            }
        } else null
    }

    TextEditor(
        modifier = modifier,
        textFieldModifier = textFieldModifier,
        textFieldState = textFieldState,
        scrollState = scrollState,
        findAndReplaceState = findAndReplaceState,
        readOnly = readOnly,
        isLineNumberVisible = isLineNumberVisible,
        lint = lint,
        headerRange = headerRange,
        outputTransformation = outputTransformation
    )
}

@Composable
fun AdaptiveViewer(
    modifier: Modifier,
    noteType: NoteType,
    textFieldState: TextFieldState,
    scrollState: ScrollState,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>
) = when (noteType) {
    NoteType.MARKDOWN -> {
        MarkdownViewer(
            modifier = modifier,
            textFieldState = textFieldState,
            scrollState = scrollState,
            isSheetVisible = isSheetVisible,
            printTrigger = printTrigger
        )
    }

    NoteType.TODO -> {
        TodoViewer(
            modifier = modifier,
            textFieldState = textFieldState
        )
    }

    else -> Spacer(modifier)
}