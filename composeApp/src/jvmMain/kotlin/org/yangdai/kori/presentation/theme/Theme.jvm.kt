package org.yangdai.kori.presentation.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import org.yangdai.kori.presentation.screen.settings.AppColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun KoriTheme(
    darkMode: Boolean,
    amoledMode: Boolean,
    color: AppColor,
    fontScale: Float,
    content: @Composable (() -> Unit)
) {
    val targetColorScheme = when (color) {
        AppColor.BLUE -> if (darkMode) DarkBlueColors else LightBlueColors
        AppColor.GREEN -> if (darkMode) DarkGreenColors else LightGreenColors
        AppColor.ORANGE -> if (darkMode) DarkOrangeColors else LightOrangeColors
        AppColor.RED -> if (darkMode) DarkRedColors else LightRedColors
        AppColor.CYAN -> if (darkMode) DarkCyanColors else LightCyanColors
        AppColor.BLACK -> if (darkMode) DarkBlackColors else LightBlackColors
        else -> if (darkMode) darkColorScheme() else expressiveLightColorScheme()
    }

    ExpressiveTheme(
        darkMode = darkMode,
        amoledMode = amoledMode,
        targetColorScheme = targetColorScheme,
        fontScale = fontScale,
        content = content
    )
}
