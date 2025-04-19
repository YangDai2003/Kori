package org.yangdai.kori

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
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
import org.yangdai.kori.presentation.component.login.NumberLockScreen
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
            window.minimumSize = Dimension(400, 600)
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
                    var isLocked by rememberSaveable { mutableStateOf(true) }
                    val blur by animateDpAsState(
                        targetValue = if (isLocked) 16.dp else 0.dp,
                        label = "Blur"
                    )
                    AppNavHost(modifier = Modifier.blur(blur))
                    AnimatedVisibility(
                        visible = isLocked,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        NumberLockScreen(
                            modifier =  Modifier.background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.25f)),
                            onAuthenticated = { isLocked = false }
                        )
                    }
                }
            }
        }
    }
}
