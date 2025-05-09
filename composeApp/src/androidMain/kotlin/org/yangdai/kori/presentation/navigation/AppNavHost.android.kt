package org.yangdai.kori.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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

@Composable
actual fun AppNavHost(
    modifier: Modifier,
    navHostController: NavHostController
) = NavHost(
    modifier = modifier,
    navController = navHostController,
    startDestination = Screen.Main,
    enterTransition = {
        sharedAxisXIn(initialOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() })
    },
    exitTransition = {
        sharedAxisXOut(targetOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() })
    },
    popEnterTransition = {
        sharedAxisXIn(initialOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() })
    },
    popExitTransition = {
        sharedAxisXOut(targetOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() })
    }
) {
    composable<Screen.Main> {
        MainScreen { screen ->
            navHostController.navigate(screen)
        }
    }

    composable<Screen.Note>(deepLinks = listOf(navDeepLink<Screen.Note>(basePath = "${Constants.DEEP_LINK}/note"))) {
        val route = it.toRoute<Screen.Note>()
        NoteScreen(
            noteId = route.id,
            folderId = route.folderId,
            navigateToScreen = { navHostController.navigate(it) }
        ) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.File> {
        val context = LocalContext.current.applicationContext
        val route = it.toRoute<Screen.File>()
        FileScreen(
            file = PlatformFile(context = context, uri = route.path.toUri()),
            navigateToScreen = { navHostController.navigate(it) }
        ) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.Template> {
        val route = it.toRoute<Screen.Template>()
        TemplateScreen(noteId = route.id) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.Settings> {
        SettingsScreen {
            navHostController.navigateUp()
        }
    }

    composable<Screen.Folders> {
        FoldersScreen {
            navHostController.navigateUp()
        }
    }
}