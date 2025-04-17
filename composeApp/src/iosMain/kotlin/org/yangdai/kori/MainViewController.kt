package org.yangdai.kori

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.data.di.KoinInitializer
import org.yangdai.kori.presentation.navigation.AppNavHost
import org.yangdai.kori.presentation.state.AppTheme
import org.yangdai.kori.presentation.theme.KoriTheme
import org.yangdai.kori.presentation.viewModel.SettingsViewModel

@OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        KoinInitializer.init()
        enableBackGesture = true
        parallelRendering = true
    }
) {
    val settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
    val stylePaneState by settingsViewModel.stylePaneState.collectAsStateWithLifecycle()
    KoriTheme(
        darkMode = if (stylePaneState.theme == AppTheme.SYSTEM) isSystemInDarkTheme()
        else stylePaneState.theme == AppTheme.DARK,
        color = stylePaneState.color,
        amoledMode = stylePaneState.isAppInAmoledMode
    ) {
        Surface {
            AppNavHost()
        }
    }
}