package org.yangdai.kori.presentation.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
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

    BackHandler(enabled = !navigator.canNavigateBack()) {
        navigateUp()
    }

    key(navigator) {
        BackHandler(enabled = navigator.canNavigateBack()) {
            coroutineScope.launch {
                navigator.navigateBack()
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize(0.95f)
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 8.dp,
        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    SettingsListPane(
                        selectedItem = selectedItem,
                        navigateUp = navigateUp
                    ) { itemId ->
                        coroutineScope.launch {
                            navigator.navigateTo(
                                ListDetailPaneScaffoldRole.Detail,
                                itemId
                            )
                        }
                    }
                }
            },
            detailPane = {
                AnimatedPane {
                    SettingsDetailPane(selectedItem = selectedItem, isExpanded = isExpanded) {
                        if (navigator.canNavigateBack()) {
                            coroutineScope.launch {
                                navigator.navigateBack()
                            }
                        }
                    }
                }
            }
        )
    }
}
