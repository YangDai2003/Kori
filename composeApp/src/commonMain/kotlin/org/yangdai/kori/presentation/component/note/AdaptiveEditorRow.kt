package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.control
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownEditorRow
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextEditorRow
import org.yangdai.kori.presentation.component.note.todo.TodoTextEditorRow

interface EditorRowScope {
    @Composable
    fun EditorRowSection(content: @Composable RowScope.() -> Unit)

    @Composable
    fun EditorRowButton(
        icon: ImageVector,
        hint: String = "",
        actionText: String = "",
        enabled: Boolean = true,
        onClick: () -> Unit
    )

    @Composable
    fun EditorRowButton(
        icon: Painter,
        hint: String = "",
        actionText: String = "",
        enabled: Boolean = true,
        onClick: () -> Unit
    )
}

class EditorRowScopeImpl(val showElevation: Boolean) : EditorRowScope {
    @Composable
    override fun EditorRowSection(content: @Composable RowScope.() -> Unit) {
        val color by animateColorAsState(
            targetValue = if (showElevation) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceContainerLow,
            label = "EditorRowSectionColorAnimation"
        )
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 4.dp)
                .focusProperties { canFocus = false },
            shape = MaterialTheme.shapes.medium,
            color = color
        ) {
            Row {
                content()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun EditorRowButton(
        icon: ImageVector,
        hint: String,
        actionText: String,
        enabled: Boolean,
        onClick: () -> Unit
    ) = TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            if (actionText.isEmpty() && hint.isEmpty()) return@TooltipBox
            PlainTooltip(
                content = {
                    val platformKeyboardShortCut =
                        if (currentPlatformInfo.operatingSystem == OS.MACOS || currentPlatformInfo.operatingSystem == OS.IOS) "⌘"
                        else stringResource(Res.string.control)
                    val annotatedString = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(hint)
                        }
                        if (hint.isNotEmpty() && actionText.isNotEmpty()) append("\n$platformKeyboardShortCut + ")
                        append(actionText)
                    }
                    Text(annotatedString, textAlign = TextAlign.Center)
                }
            )
        },
        state = rememberTooltipState(),
        focusable = false,
        enableUserInput = enabled
    ) {
        Box(
            modifier = Modifier.fillMaxHeight().aspectRatio(1f).pointerHoverIcon(PointerIcon.Hand)
                .clickable(enabled = enabled, role = Role.Button) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.3f),
                contentDescription = null
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun EditorRowButton(
        icon: Painter,
        hint: String,
        actionText: String,
        enabled: Boolean,
        onClick: () -> Unit
    ) = TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            if (actionText.isEmpty() && hint.isEmpty()) return@TooltipBox
            PlainTooltip(
                content = {
                    val platformKeyboardShortCut =
                        if (currentPlatformInfo.operatingSystem == OS.MACOS || currentPlatformInfo.operatingSystem == OS.IOS) "⌘"
                        else stringResource(Res.string.control)
                    val annotatedString = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(hint)
                        }
                        if (hint.isNotEmpty() && actionText.isNotEmpty()) append("\n$platformKeyboardShortCut + ")
                        append(actionText)
                    }
                    Text(annotatedString, textAlign = TextAlign.Center)
                }
            )
        },
        state = rememberTooltipState(),
        focusable = false,
        enableUserInput = enabled
    ) {
        Box(
            modifier = Modifier.fillMaxHeight().aspectRatio(1f).pointerHoverIcon(PointerIcon.Hand)
                .clickable(enabled = enabled, role = Role.Button) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.3f),
                contentDescription = null
            )
        }
    }
}

/**
 * A composable function that renders an adaptive editor row based on the provided `NoteType`.
 *
 * @param visible Whether the editor row is visible.
 * @param type The type of note, which determines the editor row to be displayed (Markdown, Plain Text...).
 * @param scrollState The scroll state for the editor.
 * @param textFieldState The state of the text field used in the editor.
 * @param paddingValues The paddings at the bottom and start of the editor row.
 * @param isTemplate Whether the editor row is displayed in a template.
 * @param onEditorRowAction A callback function triggered by editor row actions.
 */
@Composable
fun AdaptiveEditorRow(
    visible: Boolean,
    type: NoteType,
    scrollState: ScrollState,
    textFieldState: TextFieldState,
    paddingValues: PaddingValues,
    isTemplate: Boolean = false,
    onEditorRowAction: (EditorRowAction) -> Unit = { _ -> }
) {
    val showElevation by remember(visible) {
        derivedStateOf {
            scrollState.canScrollForward && visible
        }
    }
    val color by animateColorAsState(
        targetValue = if (showElevation) MaterialTheme.colorScheme.surfaceContainerLow
        else MaterialTheme.colorScheme.surface,
        label = "EditorRowColorAnimation"
    )

    val scope = remember(showElevation) { EditorRowScopeImpl(showElevation) }

    Surface(modifier = Modifier.fillMaxWidth(), color = color) {
        Column(Modifier.padding(paddingValues)) {
            AnimatedVisibility(visible) {
                with(scope) {
                    when (type) {
                        NoteType.PLAIN_TEXT ->
                            PlainTextEditorRow(isTemplate, textFieldState, onEditorRowAction)

                        NoteType.MARKDOWN ->
                            MarkdownEditorRow(isTemplate, textFieldState, onEditorRowAction)

                        NoteType.TODO ->
                            TodoTextEditorRow(isTemplate, textFieldState, onEditorRowAction)

                        NoteType.Drawing -> {
                            // 绘图不需要编辑栏
                        }
                    }
                }
            }
        }
    }
}

sealed class EditorRowAction {
    object Images : EditorRowAction()
    object Videos : EditorRowAction()
    object Audio : EditorRowAction()
    object Templates : EditorRowAction()
}