package org.yangdai.kori.presentation.component.setting.detail

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.color_platte
import org.yangdai.kori.R
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.component.setting.SettingsHeader
import org.yangdai.kori.presentation.screen.settings.AppColor
import org.yangdai.kori.presentation.screen.settings.AppColor.Companion.toInt
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.util.Constants

@Composable
actual fun StylePane(settingsViewModel: SettingsViewModel) {

    val stylePaneState by settingsViewModel.stylePaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(Modifier.verticalScroll(rememberScrollState())) {

        StyledPaletteImage()

        SettingsHeader(org.jetbrains.compose.resources.stringResource(Res.string.color_platte))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DetailPaneItem(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                title = stringResource(R.string.dynamic_only_android_12),
                icon = Icons.Default.Colorize,
                trailingContent = {
                    Switch(
                        checked = stylePaneState.color == AppColor.DYNAMIC,
                        onCheckedChange = { checked ->
                            if (checked) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                settingsViewModel.putPreferenceValue(
                                    Constants.Preferences.APP_COLOR,
                                    AppColor.DYNAMIC.toInt()
                                )
                            } else {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                                settingsViewModel.putPreferenceValue(
                                    Constants.Preferences.APP_COLOR,
                                    AppColor.PURPLE.toInt()
                                )
                            }
                        }
                    )
                }
            )
        }

        AnimatedVisibility(stylePaneState.color != AppColor.DYNAMIC || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ColorPlatteRow(stylePaneState, settingsViewModel)
        }

        CommonStylePane(stylePaneState, settingsViewModel)
    }
}