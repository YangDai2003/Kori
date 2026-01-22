package org.yangdai.kori

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.DarkDefaultContextMenuRepresentation
import androidx.compose.foundation.LightDefaultContextMenuRepresentation
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingWindow
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
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
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.skiko.currentSystemTheme
import org.jetbrains.skiko.disableTitleBar
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.data.di.KoinInitializer
import org.yangdai.kori.data.local.entity.NoteType
import org.yangdai.kori.presentation.component.LocalTopAppBarPadding
import org.yangdai.kori.presentation.component.dialog.ProgressDialog
import org.yangdai.kori.presentation.component.login.NumberLockScreen
import org.yangdai.kori.presentation.navigation.AppNavHost
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.screen.settings.AppTheme
import org.yangdai.kori.presentation.theme.KoriTheme
import org.yangdai.kori.presentation.util.AppLockManager
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.ExternalUriHandler
import java.awt.Desktop
import java.awt.Dimension

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
        var isWindowVisible by rememberSaveable { mutableStateOf(true) }

        ApplicationWindow(
            visible = isWindowVisible,
            onCloseRequest = { isWindowVisible = false },
            navHostController = navHostController
        )

        if (!isWindowVisible)
            ApplicationTray(
                navigateToScreen = { screen ->
                    navHostController.navigate(screen)
                    isWindowVisible = true
                },
                onAction = { isWindowVisible = true },
                onExit = ::exitApplication
            )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ApplicationWindow(
    visible: Boolean,
    onCloseRequest: () -> Unit,
    navHostController: NavHostController,
    state: WindowState = rememberWindowState()
) = SwingWindow(
    onCloseRequest = onCloseRequest,
    state = state,
    visible = visible,
    title = stringResource(Res.string.app_name),
    icon = painterResource(Res.drawable.icon),
    onKeyEvent = {
        if (it.isCtrlPressed && it.isShiftPressed && it.type == KeyEventType.KeyDown) {
            when (it.key) {
                Key.L -> {
                    AppLockManager.lock()
                    true
                }

                else -> false
            }
        } else false
    },
    init = { window ->
        with(window.rootPane) {
            putClientProperty("apple.awt.fullWindowContent", true)
            putClientProperty("apple.awt.transparentTitleBar", true)
            putClientProperty("apple.awt.windowTitleVisible", false)
        }
    }
) {
    LaunchedEffect(window) {
        if (currentPlatformInfo.operatingSystem == OS.MACOS)
            window.findSkiaLayer()?.disableTitleBar(56.dp.value)
        window.minimumSize = Dimension(400, 600)
    }
    val mainViewModel: MainViewModel = koinViewModel<MainViewModel>()
    val stylePaneState by mainViewModel.stylePaneState.collectAsStateWithLifecycle()
    val securityPaneState by mainViewModel.securityPaneState.collectAsStateWithLifecycle()
    val dataActionState by mainViewModel.dataActionState.collectAsStateWithLifecycle()
    val isUnlocked by AppLockManager.isUnlocked.collectAsStateWithLifecycle()
    val isSystemInDarkTheme by produceState(initialValue = currentSystemTheme == org.jetbrains.skiko.SystemTheme.DARK) {
        while (true) {
            delay(1000L)
            value = currentSystemTheme == org.jetbrains.skiko.SystemTheme.DARK
        }
    }

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
    val topAppBarPadding by remember(state) {
        derivedStateOf {
            if (currentPlatformInfo.operatingSystem == OS.MACOS && state.placement != WindowPlacement.Fullscreen)
                PaddingValues(start = 80.dp) else TopAppBarDefaults.ContentPadding
        }
    }

    CompositionLocalProvider(
        LocalContextMenuRepresentation provides contextMenuRepresentation,
        LocalTopAppBarPadding provides topAppBarPadding
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
                    mainViewModel = mainViewModel,
                    navHostController = navHostController
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
}

@Composable
private fun ApplicationScope.ApplicationTray(
    navigateToScreen: (Screen) -> Unit,
    onAction: () -> Unit,
    onExit: () -> Unit
) = Tray(
    icon = painterResource(Res.drawable.icon),
    tooltip = stringResource(Res.string.app_name),
    onAction = onAction,
    menu = {
        Menu(stringResource(Res.string.new)) {
            Item(
                text = stringResource(Res.string.plain_text),
                onClick = {
                    navigateToScreen(Screen.Note(noteType = NoteType.PLAIN_TEXT.ordinal))
                }
            )
            Item(
                text = stringResource(Res.string.markdown),
                onClick = {
                    navigateToScreen(Screen.Note(noteType = NoteType.MARKDOWN.ordinal))
                }
            )
            Item(
                text = stringResource(Res.string.todo_text),
                onClick = {
                    navigateToScreen(Screen.Note(noteType = NoteType.TODO.ordinal))
                }
            )
            Item(
                text = stringResource(Res.string.drawing),
                onClick = {
                    navigateToScreen(Screen.Note(noteType = NoteType.Drawing.ordinal))
                }
            )
        }
        Separator()
        Menu(stringResource(Res.string.open)) {
            Item(
                text = stringResource(Res.string.folders),
                onClick = { navigateToScreen(Screen.Folders) }
            )
            Item(
                text = stringResource(Res.string.settings),
                onClick = { navigateToScreen(Screen.Settings) }
            )
        }
        Separator()
        Item(
            text = stringResource(Res.string.exit) + " " + stringResource(Res.string.app_name),
            onClick = onExit
        )
    }
)