package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.color_platte
import kori.composeapp.generated.resources.dark_mode
import kori.composeapp.generated.resources.font
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.setting.SettingsHeader
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.util.Constants

@Composable
actual fun StylePane(settingsViewModel: SettingsViewModel) {

    val stylePaneState by settingsViewModel.stylePaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(Modifier.verticalScroll(rememberScrollState())) {

        StyledPaletteImage()

        SettingsHeader(stringResource(Res.string.color_platte))

        ColorPlatteRow(stylePaneState, settingsViewModel)

        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                modifier = Modifier.padding(start = 20.dp),
                checked = stylePaneState.isAppInAmoledMode,
                onCheckedChange = {
                    if (it)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    else
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                    settingsViewModel.putPreferenceValue(
                        Constants.Preferences.IS_APP_IN_AMOLED_MODE,
                        it
                    )
                }
            )
            Text(
                text = "AMOLED",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        SettingsHeader(stringResource(Res.string.font))

        FontSizeSlider(stylePaneState, settingsViewModel)

        SettingsHeader(stringResource(Res.string.dark_mode))

        AppThemeColumn(stylePaneState, settingsViewModel)

        Spacer(Modifier.navigationBarsPadding())
    }
}