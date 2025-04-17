package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.yangdai.kori.presentation.component.setting.SettingsHeader
import org.yangdai.kori.presentation.state.AppColor
import org.yangdai.kori.presentation.state.AppColor.Companion.toInt
import org.yangdai.kori.presentation.state.AppTheme
import org.yangdai.kori.presentation.state.AppTheme.Companion.toInt
import org.yangdai.kori.presentation.theme.DarkBlueColors
import org.yangdai.kori.presentation.theme.DarkGreenColors
import org.yangdai.kori.presentation.theme.DarkOrangeColors
import org.yangdai.kori.presentation.theme.DarkPurpleColors
import org.yangdai.kori.presentation.theme.DarkRedColors
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.viewModel.SettingsViewModel

@Composable
actual fun StylePane(settingsViewModel: SettingsViewModel) {

    val stylePaneState by settingsViewModel.stylePaneState.collectAsStateWithLifecycle()

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .sizeIn(maxWidth = 380.dp)
                    .padding(16.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                PaletteImage()
            }
        }

        val modeOptions = listOf(
            "stringResource(R.string.system_default)",
            "stringResource(R.string.light)",
            "stringResource(R.string.dark)"
        )

        val colorSchemes = listOf(
            Pair(AppColor.PURPLE, DarkPurpleColors),
            Pair(AppColor.BLUE, DarkBlueColors),
            Pair(AppColor.GREEN, DarkGreenColors),
            Pair(AppColor.ORANGE, DarkOrangeColors),
            Pair(AppColor.RED, DarkRedColors)
        )

        SettingsHeader(text = "Color Scheme")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(
                8.dp,
                Alignment.Start
            )
        ) {
            Spacer(modifier = Modifier.width(24.dp))
            colorSchemes.forEach { colorSchemePair ->
                SelectableColorPlatte(
                    selected = stylePaneState.color == colorSchemePair.first,
                    colorScheme = colorSchemePair.second
                ) {
                    settingsViewModel.putPreferenceValue(
                        Constants.Preferences.APP_COLOR,
                        colorSchemePair.first.toInt()
                    )
                }
            }
            Spacer(modifier = Modifier.width(32.dp))
        }

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
                    settingsViewModel.putPreferenceValue(
                        Constants.Preferences.IS_APP_IN_AMOLED_MODE,
                        it
                    )
                }
            )
            Text(
                text = "AMOLED",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        SettingsHeader(text = "Dark mode")

        Column(Modifier.selectableGroup()) {
            modeOptions.forEachIndexed { index, text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = (index == stylePaneState.theme.toInt()),
                            onClick = {
                                if (stylePaneState.theme.toInt() != index) {
                                    when (index) {
                                        0 -> {
                                            settingsViewModel.putPreferenceValue(
                                                Constants.Preferences.APP_THEME,
                                                AppTheme.SYSTEM.toInt()
                                            )
                                        }

                                        1 -> {
                                            settingsViewModel.putPreferenceValue(
                                                Constants.Preferences.APP_THEME,
                                                AppTheme.LIGHT.toInt()
                                            )
                                        }

                                        2 -> {
                                            settingsViewModel.putPreferenceValue(
                                                Constants.Preferences.APP_THEME,
                                                AppTheme.DARK.toInt()
                                            )
                                        }
                                    }
                                }
                            },
                            role = Role.RadioButton
                        )
                        .padding(start = 32.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (index == stylePaneState.theme.toInt()),
                        onClick = null
                    )

                    Icon(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        imageVector = when (index) {
                            AppTheme.LIGHT.toInt() -> Icons.Default.LightMode
                            AppTheme.DARK.toInt() -> Icons.Default.DarkMode
                            else -> Icons.Default.BrightnessAuto
                        },
                        tint = MaterialTheme.colorScheme.secondary,
                        contentDescription = null
                    )

                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(start = 16.dp)

                    )
                }
            }
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}