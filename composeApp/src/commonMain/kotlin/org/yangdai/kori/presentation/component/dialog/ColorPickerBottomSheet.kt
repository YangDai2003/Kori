package org.yangdai.kori.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.color_picker
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.util.toHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    oColor: Color? = null,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var color by remember { mutableStateOf(oColor) }
    val controller = rememberColorPickerController()

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = stringResource(Res.string.color_picker),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = color?.toArgb()?.toHexColor().orEmpty(),
                    color = color ?: Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                val haptic = LocalHapticFeedback.current
                FilledIconButton (
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        onConfirm(color?.toArgb() ?: Color.White.toArgb())
                    }
                ) {
                    Icon(Icons.Default.Done, contentDescription = null)
                }
            }

            HsvColorPicker(
                modifier = Modifier.fillMaxWidth(0.7f).height(320.dp),
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    color = colorEnvelope.color // ARGB color value.
                },
                initialColor = oColor
            )

            AlphaSlider(
                modifier = Modifier.fillMaxWidth(0.7f).height(35.dp),
                borderRadius = 16.dp,
                controller = controller,
                initialColor = oColor
            )

            BrightnessSlider(
                modifier = Modifier.fillMaxWidth(0.7f).height(35.dp),
                borderRadius = 16.dp,
                controller = controller,
                initialColor = oColor
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
