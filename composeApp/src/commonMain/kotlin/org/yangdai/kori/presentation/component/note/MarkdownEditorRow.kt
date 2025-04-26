package org.yangdai.kori.presentation.component.note

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.outlined.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataArray
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.StrikethroughS
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.format_h1
import kori.composeapp.generated.resources.format_h2
import kori.composeapp.generated.resources.format_h3
import kori.composeapp.generated.resources.format_h4
import kori.composeapp.generated.resources.format_h5
import kori.composeapp.generated.resources.format_h6
import org.jetbrains.compose.resources.painterResource
import org.yangdai.kori.presentation.util.rememberIsScreenSizeLarge

@Composable
fun EditorSection(vararg content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row {
            content.forEachIndexed { index, item ->
                item()
                if (index < content.size - 1) {
                    VerticalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.surface)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTooltipIconButton(
    tipText: String,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit
) = TooltipBox(
    modifier = Modifier.fillMaxHeight()
        .clickable(enabled = enabled, role = Role.Button) { onClick() },
    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
    tooltip = {
        PlainTooltip(
            content = { Text(tipText) }
        )
    },
    state = rememberTooltipState(),
    focusable = false,
    enableUserInput = enabled
) {
    Box(
        modifier = Modifier.fillMaxHeight().aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.3f
            ),
            contentDescription = null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTooltipIconButton(
    tipText: String,
    icon: Painter,
    enabled: Boolean = true,
    onClick: () -> Unit
) = TooltipBox(
    modifier = Modifier.fillMaxHeight()
        .clickable(enabled = enabled, role = Role.Button) { onClick() },
    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
    tooltip = {
        PlainTooltip(
            content = { Text(tipText) }
        )
    },
    state = rememberTooltipState(),
    focusable = false,
    enableUserInput = enabled
) {
    Box(
        modifier = Modifier.fillMaxHeight().aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = icon,
            tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.3f
            ),
            contentDescription = null
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarkdownEditorRow(textFieldState: TextFieldState) = Row(
    modifier = Modifier.fillMaxWidth().height(48.dp).horizontalScroll(rememberScrollState()),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(
        4.dp,
        if (rememberIsScreenSizeLarge()) Alignment.CenterHorizontally else Alignment.Start
    )
) {

    Spacer(Modifier.width(4.dp))

    EditorSection(
        {
            EditorTooltipIconButton(
                tipText = "Ctrl + Z",
                icon = Icons.AutoMirrored.Outlined.Undo,
                enabled = textFieldState.undoState.canUndo,
                onClick = { textFieldState.undoState.undo() }
            )
        },
        {
            EditorTooltipIconButton(
                tipText = "Ctrl + Y",
                icon = Icons.AutoMirrored.Outlined.Redo,
                enabled = textFieldState.undoState.canRedo,
                onClick = { textFieldState.undoState.redo() }
            )
        }
    )

    var isHeadingSectionExpanded by rememberSaveable { mutableStateOf(false) }

    EditorSection(
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.Title,
                onClick = { isHeadingSectionExpanded = !isHeadingSectionExpanded }
            )
        }
    )

    AnimatedVisibility(visible = isHeadingSectionExpanded) {
        EditorSection(
            {
                EditorTooltipIconButton(
                    tipText = "",
                    icon = painterResource(Res.drawable.format_h1),
                    onClick = { textFieldState.edit { addHeader(1) } }
                )
            },
            {
                EditorTooltipIconButton(
                    tipText = "",
                    icon = painterResource(Res.drawable.format_h2),
                    onClick = { textFieldState.edit { addHeader(2) } }
                )
            },
            {
                EditorTooltipIconButton(
                    tipText = "",
                    icon = painterResource(Res.drawable.format_h3),
                    onClick = { textFieldState.edit { addHeader(3) } }
                )
            },
            {
                EditorTooltipIconButton(
                    tipText = "",
                    icon = painterResource(Res.drawable.format_h4),
                    onClick = { textFieldState.edit { addHeader(4) } }
                )
            },
            {
                EditorTooltipIconButton(
                    tipText = "",
                    icon = painterResource(Res.drawable.format_h5),
                    onClick = { textFieldState.edit { addHeader(5) } }
                )
            },
            {
                EditorTooltipIconButton(
                    tipText = "",
                    icon = painterResource(Res.drawable.format_h6),
                    onClick = { textFieldState.edit { addHeader(6) } }
                )
            }
        )
    }

    EditorSection(
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.FormatBold,
                onClick = { textFieldState.edit { bold() } }
            )
        },
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.FormatItalic,
                onClick = { textFieldState.edit { italic() } }
            )
        },
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.FormatUnderlined,
                onClick = { textFieldState.edit { underline() } }
            )
        },
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.StrikethroughS,
                onClick = { textFieldState.edit { strikeThrough() } }
            )
        },
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.FormatPaint,
                onClick = { textFieldState.edit { highlight() } }
            )
        }
    )

    EditorSection(
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.AutoMirrored.Outlined.FormatIndentIncrease,
                onClick = { textFieldState.edit { tab() } }
            )
        },
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.AutoMirrored.Outlined.FormatIndentDecrease,
                onClick = { textFieldState.edit { unTab() } }
            )
        }
    )

    EditorSection(
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.Code,
                onClick = { textFieldState.edit { inlineCode() } }
            )
        },
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.FormatQuote,
                onClick = { textFieldState.edit { quote() } }
            )
        }
    )

    EditorSection(
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.DataArray,
                onClick = { textFieldState.edit { inlineBrackets() } }
            )
        },
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.DataObject,
                onClick = { textFieldState.edit { inlineBraces() } }
            )
        },
        {
            EditorTooltipIconButton(
                tipText = "",
                icon = Icons.Outlined.HorizontalRule,
                onClick = { textFieldState.edit { addRule() } }
            )
        }
    )

    Spacer(Modifier.width(4.dp))
}