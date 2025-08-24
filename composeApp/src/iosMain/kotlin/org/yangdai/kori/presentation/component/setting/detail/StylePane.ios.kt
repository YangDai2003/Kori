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
import org.yangdai.kori.presentation.screen.main.MainViewModel

@Composable
actual fun StylePane(mainViewModel: MainViewModel) {

    val stylePaneState by mainViewModel.stylePaneState.collectAsStateWithLifecycle()

    Column(Modifier.verticalScroll(rememberScrollState())) {

        StyledPaletteImage()

        SettingsHeader(stringResource(Res.string.color_platte))

        ColorPlatteRow(stylePaneState, mainViewModel)

        CommonStylePane(stylePaneState, mainViewModel)
    }
}