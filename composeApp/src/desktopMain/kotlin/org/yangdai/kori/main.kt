package org.yangdai.kori

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import kori.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.data.di.KoinInitializer
import org.yangdai.kori.presentation.navigation.AppNavHost
import org.yangdai.kori.presentation.state.AppTheme
import org.yangdai.kori.presentation.theme.KoriTheme
import org.yangdai.kori.presentation.viewModel.SettingsViewModel
import java.awt.Dimension

fun main() {
    KoinInitializer.init()
    application {
        val state = rememberWindowState()
        Window(
            onCloseRequest = ::exitApplication,
            state = state,
            title = stringResource(Res.string.app_name),
            icon = painterResource(Res.drawable.compose_multiplatform)
        ) {
            window.minimumSize = Dimension(380, 380)
            val settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
            val stylePaneState by settingsViewModel.stylePaneState.collectAsStateWithLifecycle()
            KoriTheme(
                darkMode =
                    if (stylePaneState.theme == AppTheme.SYSTEM) {
                        isSystemInDarkTheme()
                    } else {
                        stylePaneState.theme == AppTheme.DARK
                    },
                color = stylePaneState.color,
                amoledMode = stylePaneState.isAppInAmoledMode,
            ) {
                Surface {
                    AppNavHost()
                }
            }
        }
    }
}
