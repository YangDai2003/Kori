package org.yangdai.kori.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.yangdai.kori.presentation.screen.settings.AppColor

@Composable
actual fun KoriTheme(
    darkMode: Boolean,
    amoledMode: Boolean,
    color: AppColor,
    fontScale: Float,
    content: @Composable (() -> Unit)
) {
    val targetColorScheme = when (color) {
        AppColor.PURPLE -> if (darkMode) DarkPurpleColors else LightPurpleColors
        AppColor.BLUE -> if (darkMode) DarkBlueColors else LightBlueColors
        AppColor.GREEN -> if (darkMode) DarkGreenColors else LightGreenColors
        AppColor.ORANGE -> if (darkMode) DarkOrangeColors else LightOrangeColors
        AppColor.RED -> if (darkMode) DarkRedColors else LightRedColors
        AppColor.CYAN -> if (darkMode) DarkCyanColors else LightCyanColors
        else -> if (darkMode) DarkPurpleColors else LightPurpleColors
    }

    // 获取带动画的颜色方案（首次启动时无动画）
    val colorScheme = getColorSchemeWithAnimation(targetColorScheme)

    // 处理AMOLED模式
    val finalColorScheme = processAmoledMode(darkMode, amoledMode, colorScheme)

    // 获取缩放后的排版
    val koriTypography = getScaledTypography(fontScale)

    MaterialTheme(
        colorScheme = finalColorScheme, typography = koriTypography, content = content
    )
}
