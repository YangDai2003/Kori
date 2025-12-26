package org.yangdai.kori

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.data.di.KoinInitializer
import org.yangdai.kori.presentation.component.dialog.ProgressDialog
import org.yangdai.kori.presentation.component.login.NumberLockScreen
import org.yangdai.kori.presentation.navigation.AppNavHost
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.theme.KoriTheme
import org.yangdai.kori.presentation.util.AppLockManager
import org.yangdai.kori.presentation.util.Constants

fun MainViewController() = ComposeUIViewController(configure = { KoinInitializer.init() }) {
    val mainViewModel: MainViewModel = koinViewModel<MainViewModel>()
    val stylePaneState by mainViewModel.stylePaneState.collectAsStateWithLifecycle()
    val securityPaneState by mainViewModel.securityPaneState.collectAsStateWithLifecycle()
    val dataActionState by mainViewModel.dataActionState.collectAsStateWithLifecycle()
    val isUnlocked by AppLockManager.isUnlocked.collectAsStateWithLifecycle()

    LaunchedEffect(securityPaneState.keepScreenOn) {
        // iOS 屏幕常亮设置
        val uiApp = platform.UIKit.UIApplication.sharedApplication
        uiApp.setIdleTimerDisabled(securityPaneState.keepScreenOn)
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkMode by remember(isSystemInDarkTheme) {
        derivedStateOf {
            when (stylePaneState.theme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
            }
        }
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
                            securityPaneState.isCreatingPass
                }
            }
            val blur by animateDpAsState(targetValue = if (showPassScreen) 16.dp else 0.dp)
            val semanticsModifier =
                if (showPassScreen) Modifier.semantics(mergeDescendants = true) { hideFromAccessibility() }
                else Modifier
            AppNavHost(
                modifier = Modifier
                    .blur(blur)
                    .then(semanticsModifier),
                mainViewModel = mainViewModel
            )
            ProgressDialog(dataActionState) {
                mainViewModel.cancelDataAction()
            }
            if (showPassScreen) {
                NumberLockScreen(
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.25f)
                    ),
                    storedPassword = securityPaneState.password,
                    isCreatingPassword = securityPaneState.isCreatingPass,
                    onCreatingCanceled = {
                        mainViewModel.putPreferenceValue(
                            Constants.Preferences.IS_CREATING_PASSWORD,
                            false
                        )
                    },
                    onPassCreated = {
                        mainViewModel.putPreferenceValue(
                            Constants.Preferences.PASSWORD,
                            it
                        )
                        mainViewModel.putPreferenceValue(
                            Constants.Preferences.IS_CREATING_PASSWORD,
                            false
                        )
                        AppLockManager.unlock()
                    },
                    onAuthenticated = { AppLockManager.unlock() }
                )
            }
        }
    }
}