package org.yangdai.kori.presentation.component.setting

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.PredictiveBackHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

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
        PredictiveBackHandler(enabled = navigator.canNavigateBack()) { progress ->
            try {
                progress.collect { backEvent ->
                    navigator.seekBack(fraction = backEvent.progress)
                }
                navigator.navigateBack()
            } catch (_: CancellationException) {
                withContext(NonCancellable) { navigator.seekBack(fraction = 0f) }
            }
        }
    }
    ListDetailPaneScaffold(
        modifier = modifier,
        directive = navigator.scaffoldDirective,
        scaffoldState = navigator.scaffoldState,
        listPane = {
            AnimatedPane(
                enterTransition = slideInHorizontally { -it },
                exitTransition = slideOutHorizontally { -it },
                content = listPaneContent
            )
        },
        detailPane = {
            AnimatedPane(
                enterTransition = slideInHorizontally { it },
                exitTransition = slideOutHorizontally { it },
                content = detailPaneContent
            )
        }
    )
}