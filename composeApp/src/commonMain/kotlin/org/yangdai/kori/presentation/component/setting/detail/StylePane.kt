package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.dark
import kori.composeapp.generated.resources.font_size
import kori.composeapp.generated.resources.font_size_description
import kori.composeapp.generated.resources.light
import kori.composeapp.generated.resources.system_default
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.screen.settings.AppColor
import org.yangdai.kori.presentation.screen.settings.AppColor.Companion.toInt
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.screen.settings.AppTheme.Companion.toInt
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.screen.settings.StylePaneState
import org.yangdai.kori.presentation.theme.DarkBlackColors
import org.yangdai.kori.presentation.theme.DarkBlueColors
import org.yangdai.kori.presentation.theme.DarkCyanColors
import org.yangdai.kori.presentation.theme.DarkGreenColors
import org.yangdai.kori.presentation.theme.DarkOrangeColors
import org.yangdai.kori.presentation.theme.DarkRedColors
import org.yangdai.kori.presentation.util.Constants

@Composable
expect fun StylePane(settingsViewModel: SettingsViewModel)

@Composable
fun StyledPaletteImage() = Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center
) {
    Box(
        modifier = Modifier
            .sizeIn(maxWidth = 480.dp)
            .padding(16.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        PaletteImage()
    }
}

val colorSchemes = listOf(
    Pair(AppColor.PURPLE, darkColorScheme()),
    Pair(AppColor.BLUE, DarkBlueColors),
    Pair(AppColor.GREEN, DarkGreenColors),
    Pair(AppColor.ORANGE, DarkOrangeColors),
    Pair(AppColor.RED, DarkRedColors),
    Pair(AppColor.CYAN, DarkCyanColors),
    Pair(AppColor.BLACK, DarkBlackColors),
)

@Composable
fun ColorPlatteRow(
    stylePaneState: StylePaneState,
    settingsViewModel: SettingsViewModel
) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    val hapticFeedback = LocalHapticFeedback.current
    Spacer(modifier = Modifier.width(8.dp))
    colorSchemes.forEach { colorSchemePair ->
        SelectableColorPlatte(
            selected = stylePaneState.color == colorSchemePair.first,
            colorScheme = colorSchemePair.second
        ) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
            settingsViewModel.putPreferenceValue(
                Constants.Preferences.APP_COLOR,
                colorSchemePair.first.toInt()
            )
        }
    }
    Spacer(modifier = Modifier.width(16.dp))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FontSizeSlider(settingsViewModel: SettingsViewModel) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        ListItem(
            headlineContent = { Text(stringResource(Res.string.font_size)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Outlined.FormatSize,
                    contentDescription = null
                )
            },
            supportingContent = { Text(stringResource(Res.string.font_size_description)) }
        )

        val hapticFeedback = LocalHapticFeedback.current
        val sliderState =
            rememberSliderState(
                value = settingsViewModel.getFloatValue(Constants.Preferences.FONT_SIZE),
                valueRange = 0.75f..1.25f,
                steps = 9
            )
        sliderState.onValueChange = {
            if (sliderState.isDragging) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                settingsViewModel.putPreferenceValue(
                    Constants.Preferences.FONT_SIZE,
                    it
                )
                sliderState.value = it
            }
        }
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            state = sliderState,
            interactionSource = interactionSource,
            thumb = { SliderDefaults.Thumb(interactionSource = interactionSource) },
            track = { SliderDefaults.CenteredTrack(sliderState = sliderState) }
        )
    }
}


@Composable
fun AppThemeColumn(
    stylePaneState: StylePaneState,
    settingsViewModel: SettingsViewModel
) {

    val modeOptions = listOf(
        stringResource(Res.string.system_default),
        stringResource(Res.string.light),
        stringResource(Res.string.dark)
    )
    val hapticFeedback = LocalHapticFeedback.current
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .clip(MaterialTheme.shapes.large)
            .selectableGroup()
    ) {
        modeOptions.forEachIndexed { index, text ->
            ListItem(
                modifier = Modifier.selectable(
                    selected = (index == stylePaneState.theme.toInt()),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
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
                ),
                headlineContent = { Text(text) },
                leadingContent = {
                    Icon(
                        imageVector = when (index) {
                            AppTheme.LIGHT.toInt() -> Icons.Default.LightMode
                            AppTheme.DARK.toInt() -> Icons.Default.DarkMode
                            else -> Icons.Default.BrightnessAuto
                        },
                        contentDescription = null
                    )
                },
                trailingContent = {
                    RadioButton(
                        selected = (index == stylePaneState.theme.toInt()),
                        onClick = null
                    )
                }
            )

            if (index < modeOptions.size - 1) {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}