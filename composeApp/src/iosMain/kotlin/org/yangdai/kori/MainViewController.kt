package org.yangdai.kori

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.data.di.KoinInitializer
import org.yangdai.kori.presentation.component.login.NumberLockScreen
import org.yangdai.kori.presentation.navigation.AppNavHost
import org.yangdai.kori.presentation.state.AppTheme
import org.yangdai.kori.presentation.theme.KoriTheme
import org.yangdai.kori.presentation.util.AppLockManager
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
            val appLockManager = koinInject<AppLockManager>()
            val isUnlocked by appLockManager.isUnlocked.collectAsStateWithLifecycle()
            val blur by animateDpAsState(
                targetValue = if (isUnlocked) 0.dp else 16.dp,
                label = "Blur"
            )
            AppNavHost(modifier = Modifier.blur(blur))
            AnimatedVisibility(
                visible = !isUnlocked,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NumberLockScreen(
                    modifier =  Modifier.background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.25f)),
                    onAuthenticated = { appLockManager.unlock() }
                )
            }
        }
    }
}