package org.yangdai.kori.presentation.screen.settings

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldPaneScope
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(markerClass = [ExperimentalMaterial3AdaptiveApi::class])
@Composable
actual fun SettingsListDetailScaffold(
    modifier: Modifier,
    navigator: ThreePaneScaffoldNavigator<Int>,
    listPane: @Composable (ThreePaneScaffoldPaneScope.() -> Unit),
    detailPane: @Composable (ThreePaneScaffoldPaneScope.() -> Unit)
) = NavigableListDetailPaneScaffold(
    modifier = modifier,
    navigator = navigator,
    listPane = listPane,
    detailPane = detailPane
)