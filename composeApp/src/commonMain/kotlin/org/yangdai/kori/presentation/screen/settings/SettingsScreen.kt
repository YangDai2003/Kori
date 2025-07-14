package org.yangdai.kori.presentation.screen.settings

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import kotlinx.coroutines.launch
import org.yangdai.kori.presentation.component.setting.PlatformStyleSettingsLayout
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

    PlatformStyleSettingsLayout(
        navigator = navigator,
        coroutineScope = coroutineScope,
        listPaneContent = {
            SettingsListPane(selectedItem = selectedItem, navigateUp = navigateUp) { itemId ->
                coroutineScope.launch {
                    navigator.navigateTo(
                        ListDetailPaneScaffoldRole.Detail,
                        itemId
                    )
                }
            }
        },
        detailPaneContent = {
            SettingsDetailPane(selectedItem = selectedItem, isExpanded = isExpanded) {
                if (navigator.canNavigateBack()) {
                    coroutineScope.launch {
                        navigator.navigateBack()
                    }
                }
            }
        }
    )
}
