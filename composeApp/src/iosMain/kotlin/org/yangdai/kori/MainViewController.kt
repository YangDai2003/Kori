package org.yangdai.kori

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.theme.KoriTheme
import org.yangdai.kori.presentation.util.AppLockManager
import org.yangdai.kori.presentation.util.Constants

@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        parallelRendering = true
        KoinInitializer.init()
    }
) {
    val settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
    val stylePaneState by settingsViewModel.stylePaneState.collectAsStateWithLifecycle()
    val securityPaneState by settingsViewModel.securityPaneState.collectAsStateWithLifecycle()
    val appLockManager = koinInject<AppLockManager>()
    val isUnlocked by appLockManager.isUnlocked.collectAsStateWithLifecycle()

    LaunchedEffect(securityPaneState.keepScreenOn) {
        // iOS 屏幕常亮设置
        val uiApp = platform.UIKit.UIApplication.sharedApplication
        uiApp.setIdleTimerDisabled(securityPaneState.keepScreenOn)
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkMode = remember(isSystemInDarkTheme, stylePaneState.theme) {
        if (stylePaneState.theme == AppTheme.SYSTEM) isSystemInDarkTheme
        else stylePaneState.theme == AppTheme.DARK
    }

    KoriTheme(
        darkMode = darkMode,
        color = stylePaneState.color,
        amoledMode = stylePaneState.isAppInAmoledMode,
        fontScale = stylePaneState.fontSize
    ) {
        Surface {
            val showPassScreen by remember {
                derivedStateOf {
                    (securityPaneState.password.isNotEmpty() && !isUnlocked) ||
                            (securityPaneState.isCreatingPass && !isUnlocked)
                }
            }
            val blur by animateDpAsState(targetValue = if (showPassScreen) 16.dp else 0.dp)

            AppNavHost(modifier = Modifier.blur(blur))
            AnimatedVisibility(
                visible = showPassScreen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NumberLockScreen(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.25f)),
                    storedPassword = securityPaneState.password,
                    isCreatingPassword = securityPaneState.isCreatingPass,
                    onCreatingCanceled = {
                        settingsViewModel.putPreferenceValue(
                            Constants.Preferences.IS_CREATING_PASSWORD,
                            false
                        )
                    },
                    onPassCreated = {
                        settingsViewModel.putPreferenceValue(
                            Constants.Preferences.PASSWORD,
                            it
                        )
                        settingsViewModel.putPreferenceValue(
                            Constants.Preferences.IS_CREATING_PASSWORD,
                            false
                        )
                        appLockManager.unlock()
                    },
                    onAuthenticated = { appLockManager.unlock() }
                )
            }
        }
    }
}