package org.yangdai.kori.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import kfile.PlatformFile
import org.yangdai.kori.presentation.screen.file.FileScreen
import org.yangdai.kori.presentation.screen.folders.FoldersScreen
import org.yangdai.kori.presentation.screen.main.MainScreen
import org.yangdai.kori.presentation.screen.note.NoteScreen
import org.yangdai.kori.presentation.screen.settings.SettingsScreen
import org.yangdai.kori.presentation.screen.template.TemplateScreen
import org.yangdai.kori.presentation.util.Constants
import org.yangdai.kori.presentation.util.ExternalUriHandler
import java.io.File

@Composable
actual fun AppNavHost(
    modifier: Modifier,
    navHostController: NavHostController
) = NavHost(
    modifier = modifier,
    navController = navHostController,
    startDestination = Screen.Main
) {
    composable<Screen.Main> {
        MainScreen { screen ->
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
        val route = it.toRoute<Screen.File>()
        FileScreen(
            file = PlatformFile(File(route.path)),
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

    composable<Screen.Settings>(
        deepLinks = listOf(
            navDeepLink { uriPattern = "${Constants.DEEP_LINK}/settings" }
        )
    ) {
        SettingsScreen {
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