package org.yangdai.kori.presentation.component.setting

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun PlatformStyleSettingsLayout(
    modifier: Modifier,
    navigator: ThreePaneScaffoldNavigator<Int>,
    coroutineScope: CoroutineScope,
    listPaneContent: @Composable (AnimatedPaneScope.() -> Unit),
    detailPaneContent: @Composable (AnimatedPaneScope.() -> Unit)
) {
    key(navigator) {
        BackHandler(enabled = navigator.canNavigateBack()) {
            coroutineScope.launch {
                navigator.navigateBack()
            }
        }
    }
    ListDetailPaneScaffold(
        modifier = modifier,
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            AnimatedPane(
                enterTransition = fadeIn(),
                exitTransition = fadeOut(),
                content = listPaneContent
            )
        },
        detailPane = {
            AnimatedPane(
                enterTransition = fadeIn(),
                exitTransition = fadeOut(),
                content = detailPaneContent
            )
        }
    )
}