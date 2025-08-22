package org.yangdai.kori

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.DarkDefaultContextMenuRepresentation
import androidx.compose.foundation.LightDefaultContextMenuRepresentation
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import javafx.embed.swing.JFXPanel
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import kori.composeapp.generated.resources.drawing
import kori.composeapp.generated.resources.exit
import kori.composeapp.generated.resources.folders
import kori.composeapp.generated.resources.icon
import kori.composeapp.generated.resources.markdown
import kori.composeapp.generated.resources.new
import kori.composeapp.generated.resources.open
import kori.composeapp.generated.resources.plain_text
import kori.composeapp.generated.resources.settings
import kori.composeapp.generated.resources.todo_text
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.data.di.KoinInitializer
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.login.NumberLockScreen
import org.yangdai.kori.presentation.navigation.AppNavHost
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.theme.KoriTheme
import org.yangdai.kori.presentation.util.AppLockManager
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.ExternalUriHandler
import java.awt.Desktop
import java.awt.Dimension

@Suppress("unused")
val fakeJFXPanel = JFXPanel()

fun main() {
    System.setProperty("compose.interop.blending", "true")
    System.setProperty("compose.swing.render.on.graphics", "true")
    KoinInitializer.init()
    try {
        Desktop.getDesktop().setOpenURIHandler { event ->
            ExternalUriHandler.onNewUri(event.uri.toString())
        }
    } catch (_: UnsupportedOperationException) {
        println("setOpenURIHandler is unsupported")
    }
    application {
        val navHostController = rememberNavController()
        var mainWindowVisible by rememberSaveable { mutableStateOf(true) }
        Window(
            onCloseRequest = { mainWindowVisible = false },
            visible = mainWindowVisible,
            title = stringResource(Res.string.app_name),
            icon = painterResource(Res.drawable.icon)
        ) {
            window.minimumSize = Dimension(400, 600)
            val settingsViewModel: SettingsViewModel = koinViewModel<SettingsViewModel>()
            val stylePaneState by settingsViewModel.stylePaneState.collectAsStateWithLifecycle()
            val securityPaneState by settingsViewModel.securityPaneState.collectAsStateWithLifecycle()
            val isUnlocked by AppLockManager.isUnlocked.collectAsStateWithLifecycle()
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

            val contextMenuRepresentation =
                if (darkMode) DarkDefaultContextMenuRepresentation else LightDefaultContextMenuRepresentation

            CompositionLocalProvider(LocalContextMenuRepresentation provides contextMenuRepresentation) {
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
                            navHostController = navHostController
                        )
                        if (showPassScreen) {
                            NumberLockScreen(
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0.25f)
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
                                    AppLockManager.unlock()
                                },
                                onAuthenticated = { AppLockManager.unlock() }
                            )
                        }
                    }
                }
            }
        }

        if (!mainWindowVisible) {
            Tray(
                icon = painterResource(Res.drawable.icon),
                tooltip = stringResource(Res.string.app_name),
                onAction = { mainWindowVisible = true },
                menu = {
                    Menu(stringResource(Res.string.new)) {
                        Item(
                            text = stringResource(Res.string.plain_text),
                            onClick = {
                                mainWindowVisible = true
                                navHostController.navigate(Screen.Note(noteType = NoteType.PLAIN_TEXT.ordinal))
                            }
                        )
                        Item(
                            text = stringResource(Res.string.markdown),
                            onClick = {
                                mainWindowVisible = true
                                navHostController.navigate(Screen.Note(noteType = NoteType.MARKDOWN.ordinal))
                            }
                        )
                        Item(
                            text = stringResource(Res.string.todo_text),
                            onClick = {
                                mainWindowVisible = true
                                navHostController.navigate(Screen.Note(noteType = NoteType.TODO.ordinal))
                            }
                        )
                        Item(
                            text = stringResource(Res.string.drawing),
                            onClick = {
                                mainWindowVisible = true
                                navHostController.navigate(Screen.Note(noteType = NoteType.Drawing.ordinal))
                            }
                        )
                    }
                    Separator()
                    Menu(stringResource(Res.string.open)) {
                        Item(
                            text = stringResource(Res.string.folders),
                            onClick = {
                                mainWindowVisible = true
                                navHostController.navigate(Screen.Folders)
                            }
                        )
                        Item(
                            text = stringResource(Res.string.settings),
                            onClick = {
                                mainWindowVisible = true
                                navHostController.navigate(Screen.Settings)
                            }
                        )
                    }
                    Separator()
                    Item(
                        text = stringResource(Res.string.exit) + " " + stringResource(Res.string.app_name),
                        onClick = { exitApplication() }
                    )
                }
            )
        }
    }
}