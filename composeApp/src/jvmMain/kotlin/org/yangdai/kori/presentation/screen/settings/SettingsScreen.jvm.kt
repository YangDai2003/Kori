@file:OptIn(ExperimentalComposeUiApi::class)

package org.yangdai.kori.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import kotlinx.coroutines.launch

@OptIn(markerClass = [ExperimentalMaterial3AdaptiveApi::class])
@Composable
actual fun SettingsListDetailScaffold(
    modifier: Modifier,
    navigator: ThreePaneScaffoldNavigator<Int>,
    listPane: @Composable (ThreePaneScaffoldPaneScope.() -> Unit),
    detailPane: @Composable (ThreePaneScaffoldPaneScope.() -> Unit)
) {
    val coroutineScope = rememberCoroutineScope()

    key(navigator) {
        BackHandler(enabled = navigator.canNavigateBack()) {
            coroutineScope.launch {
                navigator.navigateBack()
            }
        }
    }

    ListDetailPaneScaffold(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = listPane,
        detailPane = detailPane
    )
}