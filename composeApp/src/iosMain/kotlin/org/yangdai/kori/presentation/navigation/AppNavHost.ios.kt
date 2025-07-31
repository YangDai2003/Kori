package org.yangdai.kori.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
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
import platform.Foundation.NSURL

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
                if (uri.contains("settings")) {
                    navHostController.navigate(Screen.Settings)
                } else if (uri.contains("folders")) {
                    navHostController.navigate(Screen.Folders)
                } else if (uri.contains("note")) {
//                    val noteId = uri.substringAfterLast("/").toLongOrNull()
//                    if (noteId != null) {
//                        navHostController.navigate(Screen.Note(noteId))
//                    }
                } else if (uri.contains("template")) {
                    navHostController.navigate(Screen.Template)
                }
            }
            onDispose {
                ExternalUriHandler.listener = null
            }
        }
    }

    composable<Screen.Note>(deepLinks = listOf(navDeepLink<Screen.Note>(basePath = "${Constants.DEEP_LINK_IOS}://note"))) {
        NoteScreen(navigateToScreen = { navHostController.navigate(it) }) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.File> {
        val route = it.toRoute<Screen.File>()
        val url = NSURL.URLWithString(route.path) ?: return@composable
        FileScreen(
            file = PlatformFile(url),
            navigateToScreen = { screen -> navHostController.navigate(screen) }
        ) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.Template>(deepLinks = listOf(navDeepLink<Screen.Template>(basePath = "${Constants.DEEP_LINK_IOS}://template"))) {
        TemplateScreen(navigateToScreen = { navHostController.navigate(it) }) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.Settings>(deepLinks = listOf(navDeepLink<Screen.Settings>(basePath = "${Constants.DEEP_LINK_IOS}://settings"))) {
        SettingsScreen {
            navHostController.navigateUp()
        }
    }

    composable<Screen.Folders>(deepLinks = listOf(navDeepLink<Screen.Folders>(basePath = "${Constants.DEEP_LINK_IOS}://folders"))) {
        FoldersScreen {
            navHostController.navigateUp()
        }
    }
}