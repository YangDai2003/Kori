package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.visible
import androidx.compose.foundation.text.input.TextFieldState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastRoundToInt
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.control
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownLint
import org.yangdai.kori.presentation.component.note.markdown.MarkdownTransformation
import org.yangdai.kori.presentation.component.note.markdown.MarkdownViewer
import org.yangdai.kori.presentation.component.note.markdown.markdownKeyEvents
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextTransformation
import org.yangdai.kori.presentation.component.note.todo.TodoLint
import org.yangdai.kori.presentation.component.note.todo.TodoTransformation
import org.yangdai.kori.presentation.component.note.todo.TodoViewer
import kotlin.math.abs

@Composable
fun ColumnScope.AdaptiveEditorViewer(
    showDualPane: Boolean,
    isReadView: Boolean,
    defaultEditorWeight: Float,
    onEditorWeightChanged: (Float) -> Unit,
    editor: @Composable (Modifier) -> Unit,
    viewer: (@Composable (Modifier) -> Unit)?
) {
    val interactionSource = remember { MutableInteractionSource() }
    var editorWeight by remember { mutableFloatStateOf(defaultEditorWeight) }
    val layoutDirection = LocalLayoutDirection.current
    var layoutWidthPx by remember { mutableFloatStateOf(1f) }
    val anchorPoints = rememberSaveable { listOf(0f, 0.2f, 1f / 3f, 0.5f, 2f / 3f, 0.8f, 1f) }

    val keyboardModifier = if (showDualPane && viewer != null) {
        Modifier.onPreviewKeyEvent {
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
    } else Modifier

    val pageOffset by animateFloatAsState(
        targetValue = if (isReadView) 1f else 0f,
        label = "pageOffset"
    )

    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .then(keyboardModifier),
        content = {
            if (viewer == null) {
                editor(Modifier.layoutId("editor").fillMaxSize())
            } else {
                val showEditor = !showDualPane || editorWeight > 0.1f
                val showViewer = !showDualPane || editorWeight < 0.9f

                if (showEditor) {
                    editor(
                        Modifier.layoutId("editor").fillMaxSize()
                            .visible(!showDualPane && pageOffset < 0.5f)
                            .focusProperties {
                                canFocus =
                                    if (showDualPane) editorWeight > 0.1f else pageOffset < 0.5f
                            }
                    )
                }

                if (showViewer) {
                    viewer(
                        Modifier.layoutId("viewer").fillMaxSize()
                            .visible(!showDualPane && pageOffset > 0.5f)
                            .focusProperties {
                                canFocus =
                                    if (showDualPane) editorWeight < 0.9f else pageOffset > 0.5f
                            }
                    )
                }

                if (showDualPane) {
                    Column(
                        modifier = Modifier.layoutId("handle").fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                TooltipAnchorPosition.Above
                            ),
                            tooltip = { PlainTooltip { Text("${stringResource(Res.string.control)} + ⇧ + ↔︎") } },
                            state = rememberTooltipState()
                        ) {
                            VerticalDragHandle(
                                modifier = Modifier
                                    .sizeIn(maxWidth = 12.dp, minWidth = 4.dp)
                                    .draggable(
                                        state = rememberDraggableState { delta ->
                                            editorWeight += delta / layoutWidthPx
                                        },
                                        orientation = Orientation.Horizontal,
                                        interactionSource = interactionSource,
                                        reverseDirection = layoutDirection == LayoutDirection.Rtl,
                                        onDragStopped = {
                                            val closestAnchor =
                                                anchorPoints.minBy { abs(it - editorWeight) }
                                            editorWeight = closestAnchor
                                            onEditorWeightChanged(closestAnchor)
                                        }
                                    ),
                                interactionSource = interactionSource
                            )
                        }
                    }
                }
            }
        }
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        layoutWidthPx = width.toFloat().coerceAtLeast(1f)

        val editorMeasurable = measurables.firstOrNull { it.layoutId == "editor" }
        val viewerMeasurable = measurables.firstOrNull { it.layoutId == "viewer" }
        val handleMeasurable = measurables.firstOrNull { it.layoutId == "handle" }

        if (viewer == null) {
            val editorPlaceable = editorMeasurable?.measure(constraints)
            layout(width, height) {
                editorPlaceable?.placeRelative(0, 0)
            }
        } else if (showDualPane) {
            val handlePlaceable = handleMeasurable?.measure(
                Constraints(minWidth = 0, maxWidth = width, minHeight = height, maxHeight = height)
            )
            val handleWidth = handlePlaceable?.width ?: 0
            val availableWidth = (width - handleWidth).coerceAtLeast(0)

            val actualEditorWeight = editorWeight.coerceIn(0f, 1f)
            val editorWidth = (availableWidth * actualEditorWeight).fastRoundToInt()
            val viewerWidth = availableWidth - editorWidth

            val editorPlaceable = editorMeasurable?.measure(Constraints.fixed(editorWidth, height))
            val viewerPlaceable = viewerMeasurable?.measure(Constraints.fixed(viewerWidth, height))

            layout(width, height) {
                editorPlaceable?.placeRelative(0, 0)
                handlePlaceable?.placeRelative(editorWidth, 0)
                viewerPlaceable?.placeRelative(editorWidth + handleWidth, 0)
            }
        } else {
            val editorPlaceable = editorMeasurable?.measure(constraints)
            val viewerPlaceable = viewerMeasurable?.measure(constraints)

            layout(width, height) {
                val currentEditorX = -(width * pageOffset).fastRoundToInt()
                val currentViewerX = (width * (1f - pageOffset)).fastRoundToInt()

                editorPlaceable?.placeRelative(currentEditorX, 0)
                viewerPlaceable?.placeRelative(currentViewerX, 0)
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
    isSyntaxHighlightingEnabled: Boolean,
    headerRange: IntRange?,
    findAndReplaceState: FindAndReplaceState,
    onScroll: (firstVisibleCharPositon: Int) -> Unit
) = Editor(
    modifier = modifier,
    textFieldModifier = when (noteType) {
        NoteType.MARKDOWN -> Modifier.markdownKeyEvents(textFieldState)
        else -> Modifier
    },
    textFieldState = textFieldState,
    scrollState = scrollState,
    findAndReplaceState = findAndReplaceState,
    readOnly = readOnly,
    isLineNumberVisible = isLineNumberVisible,
    lint = if (isLintingEnabled) {
        when (noteType) {
            NoteType.MARKDOWN -> MarkdownLint()
            NoteType.TODO -> TodoLint()
            else -> null
        }
    } else null,
    headerRange = headerRange,
    outputTransformation = if (isSyntaxHighlightingEnabled) {
        when (noteType) {
            NoteType.MARKDOWN -> MarkdownTransformation()
            NoteType.TODO -> TodoTransformation()
            NoteType.PLAIN_TEXT -> PlainTextTransformation()
            else -> null
        }
    } else null,
    onScroll = onScroll
)

@Composable
fun AdaptiveViewer(
    modifier: Modifier,
    noteType: NoteType,
    textFieldState: TextFieldState,
    firstVisibleCharPosition: Int,
    isSheetVisible: Boolean,
    printTrigger: MutableState<Boolean>
) = when (noteType) {
    NoteType.MARKDOWN -> {
        MarkdownViewer(
            modifier = modifier,
            textFieldState = textFieldState,
            firstVisibleCharPositon = firstVisibleCharPosition,
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