package org.yangdai.kori.presentation.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import org.yangdai.kori.Platform
import org.yangdai.kori.currentPlatform

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

@Composable
fun rememberCurrentPlatform(): Platform = remember { currentPlatform() }

fun Int.toHexColor(): String {
    val r = (this shr 16) and 0xFF
    val g = (this shr 8) and 0xFF
    val b = this and 0xFF
    val a = (this shr 24) and 0xFF
    return "#${a.toHexByte()}${r.toHexByte()}${g.toHexByte()}${b.toHexByte()}"
}

private fun Int.toHexByte(): String {
    return this.toString(16).padStart(2, '0').uppercase()
}