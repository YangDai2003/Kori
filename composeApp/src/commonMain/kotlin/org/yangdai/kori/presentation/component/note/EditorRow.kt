package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.note.markdown.MarkdownEditorRow
import org.yangdai.kori.presentation.component.note.plaintext.PlainTextEditorRow
import org.yangdai.kori.presentation.component.note.todo.TodoTextEditorRow

@Composable
fun EditorRow(
    visible: Boolean,
    type: NoteType,
    scrollState: ScrollState,
    textFieldState: TextFieldState,
    bottomPadding: Dp,
    onEditorRowAction: (EditorRowAction) -> Unit
) {
    val showElevation by remember(visible) {
        derivedStateOf {
            scrollState.canScrollForward && visible
        }
    }
    val color by animateColorAsState(
        targetValue = if (showElevation) BottomAppBarDefaults.containerColor
        else MaterialTheme.colorScheme.surface,
        label = "EditorRowColorAnimation"
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color
    ) {
        Column(Modifier.padding(bottom = bottomPadding)) {
            AnimatedVisibility(visible) {
                when (type) {
                    NoteType.PLAIN_TEXT -> {
                        PlainTextEditorRow(textFieldState, onEditorRowAction)
                    }

                    NoteType.MARKDOWN -> {
                        MarkdownEditorRow(textFieldState, onEditorRowAction)
                    }

                    NoteType.TODO -> {
                        TodoTextEditorRow(textFieldState, onEditorRowAction)
                    }
                }
            }
        }
    }
}

sealed class EditorRowAction {
    object Templates : EditorRowAction()
}

val platformKeyboardShortCut =
    if (currentPlatformInfo.operatingSystem == OS.MACOS || currentPlatformInfo.operatingSystem == OS.IOS)
        "Cmd"
    else "Ctrl"

@Composable
fun EditorRowSection(content: @Composable RowScope.() -> Unit) =
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp)
            .focusProperties { canFocus = false },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row {
            content()
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorRowButton(
    tipText: String? = null,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
    tooltip = {
        if (tipText == null) return@TooltipBox
        PlainTooltip(
            content = { Text(tipText) }
        )
    },
    state = rememberTooltipState(),
    focusable = false,
    enableUserInput = enabled
) {
    Box(
        modifier = Modifier.fillMaxHeight().aspectRatio(1f)
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
fun EditorRowButton(
    tipText: String? = null,
    icon: Painter,
    enabled: Boolean = true,
    onClick: () -> Unit
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
    tooltip = {
        if (tipText == null) return@TooltipBox
        PlainTooltip(
            content = { Text(tipText) }
        )
    },
    state = rememberTooltipState(),
    focusable = false,
    enableUserInput = enabled
) {
    Box(
        modifier = Modifier.fillMaxHeight().aspectRatio(1f)
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
