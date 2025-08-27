package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kolor.AlphaSlider
import kolor.BrightnessSlider
import kolor.ColorEnvelope
import kolor.HsvColorPicker
import kolor.rememberColorPickerController
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.color_picker
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.util.toHexColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorPickerBottomSheet(
    oColor: Color,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var color by remember { mutableStateOf(oColor) }
    val controller = rememberColorPickerController().apply {
        wheelColor = MaterialTheme.colorScheme.outlineVariant
    }

    ModalBottomSheet(
        sheetState = sheetState,
        sheetGesturesEnabled = false,
        dragHandle = {
            Box(Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(start = 16.dp).align(Alignment.CenterStart),
                    style = MaterialTheme.typography.titleLarge,
                    text = stringResource(Res.string.color_picker)
                )
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Light,
                    text = color.toArgb().toHexColor(),
                    color = color
                )
                val haptic = LocalHapticFeedback.current
                FilledIconButton(
                    modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                        .align(Alignment.CenterEnd)
                        .minimumInteractiveComponentSize()
                        .size(
                            IconButtonDefaults.extraSmallContainerSize(
                                IconButtonDefaults.IconButtonWidthOption.Uniform
                            )
                        ),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onConfirm(color.toArgb())
                    }
                ) {
                    Icon(
                        Icons.Default.Done,
                        contentDescription = null,
                        modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
                    )
                }
            }
        },
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            HsvColorPicker(
                modifier = Modifier.fillMaxWidth(0.7f).height(320.dp),
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    color = colorEnvelope.color // ARGB color value.
                },
                initialColor = oColor
            )

            AlphaSlider(
                modifier = Modifier.fillMaxWidth(0.75f).height(32.dp),
                borderRadius = 16.dp,
                borderSize = 0.dp,
                borderColor = Color.Transparent,
                wheelColor = MaterialTheme.colorScheme.outlineVariant,
                controller = controller,
                initialColor = oColor
            )

            BrightnessSlider(
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(0.75f).height(32.dp),
                borderRadius = 16.dp,
                borderSize = 0.dp,
                borderColor = Color.Transparent,
                wheelColor = MaterialTheme.colorScheme.outlineVariant,
                controller = controller,
                initialColor = oColor
            )
        }
    }
}
