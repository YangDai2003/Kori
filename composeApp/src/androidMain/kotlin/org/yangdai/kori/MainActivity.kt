package org.yangdai.kori

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.presentation.component.dialog.ProgressDialog
import org.yangdai.kori.presentation.navigation.AppNavHost
import org.yangdai.kori.presentation.screen.LoginOverlayScreen
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.theme.KoriTheme
import org.yangdai.kori.presentation.util.AppLockManager
import org.yangdai.kori.presentation.util.Constants

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView   // 预热 webview

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.loadUrl("about:blank")
        setContent {
            val mainViewModel: MainViewModel = koinViewModel<MainViewModel>()
            val stylePaneState by mainViewModel.stylePaneState.collectAsStateWithLifecycle()
            val securityPaneState by mainViewModel.securityPaneState.collectAsStateWithLifecycle()
            val dataActionState by mainViewModel.dataActionState.collectAsStateWithLifecycle()
            val isUnlocked by AppLockManager.isUnlocked.collectAsStateWithLifecycle()

            LaunchedEffect(securityPaneState.isScreenProtected) {
                window.let { window ->
                    if (securityPaneState.isScreenProtected) {
                        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
            }

            LaunchedEffect(securityPaneState.keepScreenOn) {
                window.let { window ->
                    if (securityPaneState.keepScreenOn) {
                        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
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
                        LoginOverlayScreen(
                            storedPassword = securityPaneState.password,
                            biometricAuthEnabled = securityPaneState.isBiometricEnabled,
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
                            onAuthenticated = {
                                AppLockManager.unlock()
                            },
                            onAuthenticationNotEnrolled = {
                                mainViewModel.putPreferenceValue(
                                    Constants.Preferences.IS_BIOMETRIC_ENABLED,
                                    false
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}