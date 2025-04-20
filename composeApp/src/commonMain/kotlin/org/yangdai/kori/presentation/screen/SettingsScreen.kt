package org.yangdai.kori.presentation.screen

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import kotlinx.coroutines.launch
import org.yangdai.kori.presentation.component.setting.SettingsDetailPane
import org.yangdai.kori.presentation.component.setting.SettingsListPane

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(navigateUp: () -> Unit) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val coroutineScope = rememberCoroutineScope()
    val selectedItem = navigator.currentDestination?.contentKey
    val isExpanded by remember {
        derivedStateOf {
            navigator.scaffoldValue.primary == PaneAdaptedValue.Expanded
                    && navigator.scaffoldValue.secondary == PaneAdaptedValue.Expanded
        }
    }

    key(navigator) {
        BackHandler(enabled = navigator.canNavigateBack()) {
            coroutineScope.launch {
                navigator.navigateBack()
            }
        }
    }

    ListDetailPaneScaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane(
                enterTransition = slideInHorizontally { -it },
                exitTransition = slideOutHorizontally { -it }
            ) {
                SettingsListPane(selectedItem = selectedItem, navigateUp = navigateUp) {
                    coroutineScope.launch {
                        navigator.navigateTo(
                            ListDetailPaneScaffoldRole.Detail,
                            it
                        )
                    }
                }
            }
        },
        paneExpansionDragHandle = {},
        detailPane = {
            AnimatedPane(
                enterTransition = slideInHorizontally { it },
                exitTransition = slideOutHorizontally { it }
            ) {
                SettingsDetailPane(selectedItem = selectedItem, isExpanded = isExpanded) {
                    if (navigator.canNavigateBack())
                        coroutineScope.launch {
                            navigator.navigateBack()
                        }
                }
            }
        }
    )
}
