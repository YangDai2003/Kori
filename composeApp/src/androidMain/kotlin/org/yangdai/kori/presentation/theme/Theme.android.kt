package org.yangdai.kori.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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
        AppColor.PURPLE -> if (darkMode) darkColorScheme() else expressiveLightColorScheme()
        AppColor.BLUE -> if (darkMode) DarkBlueColors else LightBlueColors
        AppColor.GREEN -> if (darkMode) DarkGreenColors else LightGreenColors
        AppColor.ORANGE -> if (darkMode) DarkOrangeColors else LightOrangeColors
        AppColor.RED -> if (darkMode) DarkRedColors else LightRedColors
        AppColor.CYAN -> if (darkMode) DarkCyanColors else LightCyanColors
        AppColor.BLACK -> if (darkMode) DarkBlackColors else LightBlackColors

        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            if (darkMode) darkColorScheme() else expressiveLightColorScheme()
        }
    }

    // 获取带动画的颜色方案（首次启动时无动画）
    val colorScheme = getColorSchemeWithAnimation(targetColorScheme)

    // 处理AMOLED模式
    val finalColorScheme = processAmoledMode(darkMode, amoledMode, colorScheme)

    // 获取缩放后的排版
    val koriTypography = getScaledTypography(fontScale)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkMode
        }
    }

    MaterialExpressiveTheme(
        colorScheme = finalColorScheme,
        typography = koriTypography,
        content = content
    )
}
