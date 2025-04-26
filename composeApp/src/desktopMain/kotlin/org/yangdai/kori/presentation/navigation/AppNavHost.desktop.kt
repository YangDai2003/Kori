package org.yangdai.kori.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.yangdai.kori.presentation.screen.folders.FoldersScreen
import org.yangdai.kori.presentation.screen.main.MainScreen
import org.yangdai.kori.presentation.screen.note.NoteScreen
import org.yangdai.kori.presentation.screen.settings.SettingsScreen
import org.yangdai.kori.presentation.screen.template.TemplateScreen

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
    }

    composable<Screen.Note> {
        val route = it.toRoute<Screen.Note>()
        NoteScreen(
            noteId = route.id,
            folderId = route.folderId,
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