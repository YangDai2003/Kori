package org.yangdai.kori.presentation.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import org.yangdai.kori.presentation.state.AppColor

// 色彩过渡动画时长常量（毫秒）
const val COLOR_ANIMATION_DURATION = 700

fun darkenColor(color: Color, factor: Float): Color = lerp(color, Color.Black, factor)

/**
 * 为颜色方案创建平滑过渡动画
 * @param targetColorScheme 目标颜色方案
 * @return 具有动画效果的颜色方案
 */
@Composable
fun animateColorSchemeAsState(targetColorScheme: ColorScheme): ColorScheme {
    return ColorScheme(
        primary = animateColorAsState(
            targetValue = targetColorScheme.primary, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onPrimary = animateColorAsState(
            targetValue = targetColorScheme.onPrimary, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        primaryContainer = animateColorAsState(
            targetValue = targetColorScheme.primaryContainer, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onPrimaryContainer = animateColorAsState(
            targetValue = targetColorScheme.onPrimaryContainer, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        secondary = animateColorAsState(
            targetValue = targetColorScheme.secondary, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onSecondary = animateColorAsState(
            targetValue = targetColorScheme.onSecondary, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        secondaryContainer = animateColorAsState(
            targetValue = targetColorScheme.secondaryContainer, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onSecondaryContainer = animateColorAsState(
            targetValue = targetColorScheme.onSecondaryContainer, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        tertiary = animateColorAsState(
            targetValue = targetColorScheme.tertiary, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onTertiary = animateColorAsState(
            targetValue = targetColorScheme.onTertiary, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        tertiaryContainer = animateColorAsState(
            targetValue = targetColorScheme.tertiaryContainer, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onTertiaryContainer = animateColorAsState(
            targetValue = targetColorScheme.onTertiaryContainer, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        error = animateColorAsState(
            targetValue = targetColorScheme.error, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        errorContainer = animateColorAsState(
            targetValue = targetColorScheme.errorContainer, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onError = animateColorAsState(
            targetValue = targetColorScheme.onError, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onErrorContainer = animateColorAsState(
            targetValue = targetColorScheme.onErrorContainer, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        background = animateColorAsState(
            targetValue = targetColorScheme.background, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onBackground = animateColorAsState(
            targetValue = targetColorScheme.onBackground, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surface = animateColorAsState(
            targetValue = targetColorScheme.surface, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onSurface = animateColorAsState(
            targetValue = targetColorScheme.onSurface, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surfaceVariant = animateColorAsState(
            targetValue = targetColorScheme.surfaceVariant, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        onSurfaceVariant = animateColorAsState(
            targetValue = targetColorScheme.onSurfaceVariant, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        outline = animateColorAsState(
            targetValue = targetColorScheme.outline, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        inverseOnSurface = animateColorAsState(
            targetValue = targetColorScheme.inverseOnSurface, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        inverseSurface = animateColorAsState(
            targetValue = targetColorScheme.inverseSurface, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        inversePrimary = animateColorAsState(
            targetValue = targetColorScheme.inversePrimary, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surfaceTint = animateColorAsState(
            targetValue = targetColorScheme.surfaceTint, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        outlineVariant = animateColorAsState(
            targetValue = targetColorScheme.outlineVariant, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        scrim = animateColorAsState(
            targetValue = targetColorScheme.scrim, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surfaceBright = animateColorAsState(
            targetValue = targetColorScheme.surfaceBright, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surfaceContainer = animateColorAsState(
            targetValue = targetColorScheme.surfaceContainer, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surfaceContainerHigh = animateColorAsState(
            targetValue = targetColorScheme.surfaceContainerHigh, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surfaceContainerHighest = animateColorAsState(
            targetValue = targetColorScheme.surfaceContainerHighest, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surfaceContainerLow = animateColorAsState(
            targetValue = targetColorScheme.surfaceContainerLow, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surfaceContainerLowest = animateColorAsState(
            targetValue = targetColorScheme.surfaceContainerLowest, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value,
        surfaceDim = animateColorAsState(
            targetValue = targetColorScheme.surfaceDim, 
            animationSpec = tween(COLOR_ANIMATION_DURATION)
        ).value
    )
}

@Composable
expect fun KoriTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    color: AppColor = AppColor.DYNAMIC,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
)
