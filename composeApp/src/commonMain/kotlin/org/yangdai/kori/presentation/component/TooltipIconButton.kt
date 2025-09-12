package org.yangdai.kori.presentation.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.control
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    hint: String = "",
    actionText: String = "",
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    shape: Shape = IconButtonDefaults.standardShape,
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
    tooltip = {
        if (actionText.isEmpty() && hint.isEmpty()) return@TooltipBox
        PlainTooltip {
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(hint)
                }
                if (hint.isNotEmpty() && actionText.isNotEmpty()) append("\n")
                if (actionText.isNotEmpty()) append("${stringResource(Res.string.control)} + $actionText")
            }
            Text(annotatedString, textAlign = TextAlign.Center)
        }
    },
    state = rememberTooltipState(),
    enableUserInput = enabled
) {
    IconButton(
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        enabled = enabled,
        colors = colors,
        shape = shape,
        onClick = onClick
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipIconButton(
    icon: Painter,
    onClick: () -> Unit,
    hint: String = "",
    actionText: String = "",
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    shape: Shape = IconButtonDefaults.standardShape,
) = TooltipBox(
    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
    tooltip = {
        if (actionText.isEmpty() && hint.isEmpty()) return@TooltipBox
        PlainTooltip {
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(hint)
                }
                if (hint.isNotEmpty() && actionText.isNotEmpty()) append("\n")
                if (actionText.isNotEmpty()) append("${stringResource(Res.string.control)} + $actionText")
            }
            Text(annotatedString, textAlign = TextAlign.Center)
        }
    },
    state = rememberTooltipState(),
    enableUserInput = enabled
) {
    IconButton(
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
        enabled = enabled,
        colors = colors,
        shape = shape,
        onClick = onClick
    ) {
        Icon(painter = icon, contentDescription = null)
    }
}

val KeyEvent.isPlatformActionKeyPressed: Boolean
    get() = if (currentPlatformInfo.operatingSystem == OS.MACOS || currentPlatformInfo.operatingSystem == OS.IOS) isMetaPressed else isCtrlPressed