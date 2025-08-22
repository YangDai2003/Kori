package org.yangdai.kori.presentation.navigation

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
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
import org.yangdai.kori.presentation.util.parseSharedContent

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
        val activity = LocalActivity.current
        val context = LocalContext.current.applicationContext
        SideEffect {
            if (activity == null || activity.intent == null) return@SideEffect
            val destination = activity.intent.extras?.getString("KEY_DESTINATION")
            if (destination != null) {
                if (destination.startsWith("note/"))
                    navHostController.navigate(Screen.Note(id = destination.removePrefix("note/")))
                else
                    navHostController.navigate(
                        Screen.Note(noteType = destination.removePrefix("note?noteType=").toInt())
                    )
            } else {
                activity.intent.apply {
                    when (action) {
                        Intent.ACTION_SEND, Intent.ACTION_VIEW, Intent.ACTION_EDIT -> {
                            val sharedContent = parseSharedContent(context)
                            if (sharedContent.uri != null) {
                                navHostController.navigate(Screen.File(path = sharedContent.uri.toString()))
                            } else if (sharedContent.title.isNotEmpty() || sharedContent.text.isNotEmpty()) {
                                navHostController.navigate(
                                    Screen.Note(
                                        sharedContentTitle = sharedContent.title,
                                        sharedContentText = sharedContent.text,
                                        noteType = sharedContent.type.ordinal
                                    )
                                )
                            }
                        }

                        Intent.ACTION_CREATE_NOTE, "com.google.android.gms.actions.CREATE_NOTE" -> {
                            navHostController.navigate(Screen.Note())
                        }

                        else -> {}
                    }
                }
            }
            activity.intent = null
        }
    }

    composable<Screen.Note>(deepLinks = listOf(navDeepLink<Screen.Note>(basePath = "${Constants.DEEP_LINK}/note"))) {
        NoteScreen(navigateToScreen = { navHostController.navigate(it) }) {
            navHostController.navigateUp()
        }
    }

    composable<Screen.File> {
        val context = LocalContext.current.applicationContext
        val route = it.toRoute<Screen.File>()
        FileScreen(
            file = PlatformFile(context = context, uri = route.path.toUri()),
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
            decorFitsSystemWindows = false
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "${Constants.DEEP_LINK}/settings" }
        )
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.apply {
                setDimAmount(0.32f)
            }
        }
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