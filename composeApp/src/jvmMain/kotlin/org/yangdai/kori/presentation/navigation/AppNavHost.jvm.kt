package org.yangdai.kori.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.NavUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import kfile.PlatformFile
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.yangdai.kori.presentation.screen.file.FileScreen
import org.yangdai.kori.presentation.screen.folders.FoldersScreen
import org.yangdai.kori.presentation.screen.main.MainScreen
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.screen.note.NoteScreen
import org.yangdai.kori.presentation.screen.settings.SettingsScreen
import org.yangdai.kori.presentation.screen.template.TemplateScreen
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.ExternalUriHandler
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun AppNavHost(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    navHostController: NavHostController
) = NavHost(
    modifier = modifier,
    navController = navHostController,
    startDestination = Screen.Main
) {
    composable<Screen.Main> {
        MainScreen(mainViewModel) { screen ->
            navHostController.navigate(screen)
        }
        DisposableEffect(Unit) {
            ExternalUriHandler.listener = { uri ->
                if (uri.startsWith("file://")) navHostController.navigate(Screen.File(uri))
                else navHostController.navigate(NavUri(uri))
            }
            onDispose {
                ExternalUriHandler.listener = null
            }
        }
    }

    composable<Screen.Note>(deepLinks = listOf(navDeepLink<Screen.Note>(basePath = "${Constants.DEEP_LINK}/note"))) {
        NoteScreen(navigateToScreen = { navHostController.navigate(it) }) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.File> {
        val file = File(it.toRoute<Screen.File>().path)
        FileScreen(
            viewModel = koinViewModel { parametersOf(PlatformFile(file)) },
            navigateToScreen = { screen -> navHostController.navigate(screen) }
        ) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.Template>(
        deepLinks = listOf(
            navDeepLink { uriPattern = "${Constants.DEEP_LINK}/template" }
        )
    ) {
        TemplateScreen(navigateToScreen = { navHostController.navigate(it) }) {
            navHostController.navigateUp()
        }
    }

    dialog<Screen.Settings>(
        dialogProperties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            usePlatformInsets = false,
            useSoftwareKeyboardInset = false,
            scrimColor = Color.Black.copy(alpha = 0.32f),
            animateTransition = false
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "${Constants.DEEP_LINK}/settings" }
        )
    ) {
        SettingsScreen(mainViewModel) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.Folders>(
        deepLinks = listOf(
            navDeepLink { uriPattern = "${Constants.DEEP_LINK}/folders" }
        )
    ) {
        FoldersScreen {
            navHostController.navigateUp()
        }
    }
}