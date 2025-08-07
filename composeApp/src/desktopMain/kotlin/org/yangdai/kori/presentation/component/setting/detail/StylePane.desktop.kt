package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.color_platte
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.setting.SettingsHeader
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel

@Composable
actual fun StylePane(settingsViewModel: SettingsViewModel) {

    val stylePaneState by settingsViewModel.stylePaneState.collectAsStateWithLifecycle()

    Column(Modifier.verticalScroll(rememberScrollState())) {

        StyledPaletteImage()

        SettingsHeader(stringResource(Res.string.color_platte))

        ColorPlatteRow(stylePaneState, settingsViewModel)

        CommonStylePane(stylePaneState, settingsViewModel)
    }
}