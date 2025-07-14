package org.yangdai.kori.presentation.component.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPaneScope
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
expect fun PlatformStyleSettingsLayout(
    modifier: Modifier = Modifier.fillMaxSize()
        .background(MaterialTheme.colorScheme.surfaceContainer),
    navigator: ThreePaneScaffoldNavigator<Int>,
    coroutineScope: CoroutineScope,
    listPaneContent: (@Composable AnimatedPaneScope.() -> Unit),
    detailPaneContent: @Composable AnimatedPaneScope.() -> Unit
)