package org.yangdai.kori.presentation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

@Composable
fun rememberIsScreenSizeLarge(): Boolean {
    val windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isLargeScreen by remember(windowSizeClass) {
        derivedStateOf {
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED &&
                    windowSizeClass.windowHeightSizeClass != WindowHeightSizeClass.COMPACT
        }
    }
    return isLargeScreen
}