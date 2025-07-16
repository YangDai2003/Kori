package org.yangdai.kori.presentation.component.setting

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

@OptIn(markerClass = [ExperimentalMaterial3AdaptiveApi::class])
@Composable
actual fun PlatformStyleSettingsLayout(
    modifier: Modifier,
    navigator: ThreePaneScaffoldNavigator<Int>,
    coroutineScope: CoroutineScope,
    listPaneContent: @Composable (AnimatedPaneScope.() -> Unit),
    detailPaneContent: @Composable (AnimatedPaneScope.() -> Unit)
) = NavigableListDetailPaneScaffold(
    modifier = modifier,
    navigator = navigator,
    listPane = { AnimatedPane { listPaneContent() } },
    detailPane = { AnimatedPane { detailPaneContent() } }
)