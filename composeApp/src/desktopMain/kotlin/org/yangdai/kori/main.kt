package org.yangdai.kori

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.DarkDefaultContextMenuRepresentation
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LightDefaultContextMenuRepresentation
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import java.awt.Dimension

@OptIn(ExperimentalFoundationApi::class)
fun main() {
    System.setProperty("compose.interop.blending", "true")
    System.setProperty("compose.swing.render.on.graphics", "true")
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
            val securityPaneState by settingsViewModel.securityPaneState.collectAsStateWithLifecycle()
            val appLockManager = koinInject<AppLockManager>()
            val isUnlocked by appLockManager.isUnlocked.collectAsStateWithLifecycle()
            val isSystemInDarkTheme = isSystemInDarkTheme()

            val darkMode by remember(isSystemInDarkTheme, stylePaneState.theme) {
                derivedStateOf {
                    if (stylePaneState.theme == AppTheme.SYSTEM) isSystemInDarkTheme
                    else stylePaneState.theme == AppTheme.DARK
                }
            }

            val contextMenuRepresentation =
                if (darkMode) DarkDefaultContextMenuRepresentation else LightDefaultContextMenuRepresentation

            CompositionLocalProvider(
                LocalContextMenuRepresentation provides contextMenuRepresentation,
//                LocalTextContextMenu provides TextContextMenu.Default
            ) {
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
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surfaceDim.copy(
                                        alpha = 0.25f
                                    )
                                ),
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
        }
    }
}
