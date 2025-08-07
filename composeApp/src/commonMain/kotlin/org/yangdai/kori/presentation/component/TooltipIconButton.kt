package org.yangdai.kori.presentation.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    buttonModifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    shape: Shape = IconButtonDefaults.standardShape,
    enabled: Boolean = true,
    tipText: String,
    icon: ImageVector,
    onClick: () -> Unit
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
    tooltip = {
        PlainTooltip(
            content = { Text(tipText) }
        )
    },
    state = rememberTooltipState(),
    focusable = false,
    enableUserInput = enabled
) {
    IconButton(
        modifier = buttonModifier,
        enabled = enabled,
        colors = colors,
        shape = shape,
        onClick = onClick
    ) {
        Icon(
            modifier = iconModifier,
            imageVector = icon,
            contentDescription = null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    buttonModifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    shape: Shape = IconButtonDefaults.standardShape,
    enabled: Boolean = true,
    tipText: String,
    icon: Painter,
    onClick: () -> Unit
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
    tooltip = {
        PlainTooltip(
            content = { Text(tipText) }
        )
    },
    state = rememberTooltipState(),
    focusable = false,
    enableUserInput = enabled
) {
    IconButton(
        modifier = buttonModifier,
        enabled = enabled,
        colors = colors,
        shape = shape,
        onClick = onClick
    ) {
        Icon(
            modifier = iconModifier,
            painter = icon,
            contentDescription = null
        )
    }
}