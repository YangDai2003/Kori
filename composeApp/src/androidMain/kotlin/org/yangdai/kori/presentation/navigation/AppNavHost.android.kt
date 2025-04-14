package org.yangdai.kori.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.yangdai.kori.presentation.screen.FoldersScreen
import org.yangdai.kori.presentation.screen.MainScreen
import org.yangdai.kori.presentation.screen.NoteScreen
import org.yangdai.kori.presentation.screen.SettingsScreen

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

    composable<Screen.Note> {
        val route = it.toRoute<Screen.Note>()
        NoteScreen(noteId = route.id) {
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