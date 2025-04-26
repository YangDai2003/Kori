package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun EditorRowSection(vararg content: @Composable () -> Unit) {
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
fun EditorRowButton(
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
fun EditorRowButton(
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