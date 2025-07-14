package org.yangdai.kori.presentation.component.setting

import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.yangdai.kori.presentation.navigation.INITIAL_OFFSET_FACTOR
import org.yangdai.kori.presentation.navigation.sharedAxisXIn
import org.yangdai.kori.presentation.navigation.sharedAxisXOut

@OptIn(markerClass = [ExperimentalMaterial3AdaptiveApi::class])
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
                enterTransition = sharedAxisXIn(initialOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() }),
                exitTransition = sharedAxisXOut(targetOffsetX = { -(it * INITIAL_OFFSET_FACTOR).toInt() }),
                content = listPaneContent
            )
        },
        detailPane = {
            AnimatedPane(
                enterTransition = sharedAxisXIn(initialOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() }),
                exitTransition = sharedAxisXOut(targetOffsetX = { (it * INITIAL_OFFSET_FACTOR).toInt() }),
                content = detailPaneContent
            )
        }
    )
}