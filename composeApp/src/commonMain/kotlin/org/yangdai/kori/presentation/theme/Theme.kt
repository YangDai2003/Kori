package org.yangdai.kori.presentation.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import org.yangdai.kori.presentation.screen.settings.AppColor

// 色彩过渡动画时长常量（毫秒）
private const val COLOR_ANIMATION_DURATION = 700

// 通过对象而不是变量来跟踪首次启动状态，避免多处状态不一致
private var isFirstLaunch = true

data class AppConfig(
    val darkMode: Boolean = false,
    val amoledMode: Boolean = false,
    val fontScale: Float = 1f
)

val LocalAppConfig = staticCompositionLocalOf { AppConfig() }

/**
 * 使颜色变暗的辅助函数
 */
private fun Color.darken(factor: Float): Color = lerp(this, Color.Black, factor)

/**
 * 为颜色方案中的单个颜色创建动画
 */
@Composable
private fun animateColor(
    targetValue: Color, animationSpec: AnimationSpec<Color> = tween(COLOR_ANIMATION_DURATION)
): Color = animateColorAsState(targetValue = targetValue, animationSpec = animationSpec).value

/**
 * 为颜色方案创建平滑过渡动画
 * @param targetColorScheme 目标颜色方案
 * @return 具有动画效果的颜色方案
 */
@Composable
private fun animateColorSchemeAsState(targetColorScheme: ColorScheme): ColorScheme {
    return ColorScheme(
        primary = animateColor(targetColorScheme.primary),
        onPrimary = animateColor(targetColorScheme.onPrimary),
        primaryContainer = animateColor(targetColorScheme.primaryContainer),
        onPrimaryContainer = animateColor(targetColorScheme.onPrimaryContainer),
        inversePrimary = animateColor(targetColorScheme.inversePrimary),
        secondary = animateColor(targetColorScheme.secondary),
        onSecondary = animateColor(targetColorScheme.onSecondary),
        secondaryContainer = animateColor(targetColorScheme.secondaryContainer),
        onSecondaryContainer = animateColor(targetColorScheme.onSecondaryContainer),
        tertiary = animateColor(targetColorScheme.tertiary),
        onTertiary = animateColor(targetColorScheme.onTertiary),
        tertiaryContainer = animateColor(targetColorScheme.tertiaryContainer),
        onTertiaryContainer = animateColor(targetColorScheme.onTertiaryContainer),
        background = animateColor(targetColorScheme.background),
        onBackground = animateColor(targetColorScheme.onBackground),
        surface = animateColor(targetColorScheme.surface),
        onSurface = animateColor(targetColorScheme.onSurface),
        surfaceVariant = animateColor(targetColorScheme.surfaceVariant),
        onSurfaceVariant = animateColor(targetColorScheme.onSurfaceVariant),
        surfaceTint = animateColor(targetColorScheme.surfaceTint),
        inverseSurface = animateColor(targetColorScheme.inverseSurface),
        inverseOnSurface = animateColor(targetColorScheme.inverseOnSurface),
        error = animateColor(targetColorScheme.error),
        onError = animateColor(targetColorScheme.onError),
        errorContainer = animateColor(targetColorScheme.errorContainer),
        onErrorContainer = animateColor(targetColorScheme.onErrorContainer),
        outline = animateColor(targetColorScheme.outline),
        outlineVariant = animateColor(targetColorScheme.outlineVariant),
        scrim = animateColor(targetColorScheme.scrim),
        surfaceBright = animateColor(targetColorScheme.surfaceBright),
        surfaceDim = animateColor(targetColorScheme.surfaceDim),
        surfaceContainer = animateColor(targetColorScheme.surfaceContainer),
        surfaceContainerHigh = animateColor(targetColorScheme.surfaceContainerHigh),
        surfaceContainerHighest = animateColor(targetColorScheme.surfaceContainerHighest),
        surfaceContainerLow = animateColor(targetColorScheme.surfaceContainerLow),
        surfaceContainerLowest = animateColor(targetColorScheme.surfaceContainerLowest),
        primaryFixed = animateColor(targetColorScheme.primaryFixed),
        primaryFixedDim = animateColor(targetColorScheme.primaryFixedDim),
        onPrimaryFixed = animateColor(targetColorScheme.onPrimaryFixed),
        onPrimaryFixedVariant = animateColor(targetColorScheme.onPrimaryFixedVariant),
        secondaryFixed = animateColor(targetColorScheme.secondaryFixed),
        secondaryFixedDim = animateColor(targetColorScheme.secondaryFixedDim),
        onSecondaryFixed = animateColor(targetColorScheme.onSecondaryFixed),
        onSecondaryFixedVariant = animateColor(targetColorScheme.onSecondaryFixedVariant),
        tertiaryFixed = animateColor(targetColorScheme.tertiaryFixed),
        tertiaryFixedDim = animateColor(targetColorScheme.tertiaryFixedDim),
        onTertiaryFixed = animateColor(targetColorScheme.onTertiaryFixed),
        onTertiaryFixedVariant = animateColor(targetColorScheme.onTertiaryFixedVariant)
    )
}

/**
 * 扩展函数：根据缩放因子调整排版中的所有字体大小。
 */
private fun Typography.withScaledFontSizes(scale: Float): Typography {
    if (scale == 1f) return this // 如果没有缩放，直接返回原对象，避免不必要的对象创建
    return this.copy(
        displayLarge = displayLarge.scale(scale),
        displayMedium = displayMedium.scale(scale),
        displaySmall = displaySmall.scale(scale),
        headlineLarge = headlineLarge.scale(scale),
        headlineMedium = headlineMedium.scale(scale),
        headlineSmall = headlineSmall.scale(scale),
        titleLarge = titleLarge.scale(scale),
        titleMedium = titleMedium.scale(scale),
        titleSmall = titleSmall.scale(scale),
        bodyLarge = bodyLarge.scale(scale),
        bodyMedium = bodyMedium.scale(scale),
        bodySmall = bodySmall.scale(scale),
        labelLarge = labelLarge.scale(scale),
        labelMedium = labelMedium.scale(scale),
        labelSmall = labelSmall.scale(scale)
    )
}

/**
 * 辅助函数，用于缩放单个 TextStyle
 */
private fun TextStyle.scale(factor: Float): TextStyle = this.copy(fontSize = this.fontSize * factor)

@Composable
expect fun KoriTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    color: AppColor = AppColor.DYNAMIC,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveTheme(
    darkMode: Boolean,
    amoledMode: Boolean,
    targetColorScheme: ColorScheme,
    fontScale: Float,
    content: @Composable () -> Unit
) {
    // 首次启动，直接使用目标颜色方案，不使用动画
    val colorScheme = if (isFirstLaunch) targetColorScheme
    else animateColorSchemeAsState(targetColorScheme)

    // 标记首次启动已完成
    LaunchedEffect(Unit) { isFirstLaunch = false }

    // 仅在深色模式下处理AMOLED选项
    val finalColorScheme = if (darkMode) {
        val contentDimmingFactor by animateFloatAsState(
            targetValue = if (amoledMode) 0.1f else 0f,
            animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION),
            label = "ContentDimming"
        )

        val backgroundDimmingFactor by animateFloatAsState(
            targetValue = if (amoledMode) 1f else 0f,
            animationSpec = tween(durationMillis = COLOR_ANIMATION_DURATION),
            label = "BackgroundDimming"
        )

        // 使用remember缓存转换结果，避免每次重组都创建新对象
        remember(colorScheme, contentDimmingFactor, backgroundDimmingFactor) {
            colorScheme.copy(
                background = colorScheme.background.darken(backgroundDimmingFactor),
                surface = colorScheme.surface.darken(backgroundDimmingFactor),
                surfaceContainer = colorScheme.surfaceContainer.darken(backgroundDimmingFactor),
                // 以下为内容相关颜色
                surfaceVariant = colorScheme.surfaceVariant.darken(contentDimmingFactor),
                surfaceContainerLowest = colorScheme.surfaceContainerLowest.darken(
                    contentDimmingFactor
                ),
                surfaceContainerLow = colorScheme.surfaceContainerLow.darken(contentDimmingFactor),
                surfaceContainerHigh = colorScheme.surfaceContainerHigh.darken(contentDimmingFactor),
                surfaceContainerHighest = colorScheme.surfaceContainerHighest.darken(
                    contentDimmingFactor
                ),
                surfaceDim = colorScheme.surfaceDim.darken(contentDimmingFactor),
                surfaceBright = colorScheme.surfaceBright.darken(contentDimmingFactor),
                scrim = colorScheme.scrim.darken(contentDimmingFactor),
                inverseSurface = colorScheme.inverseSurface.darken(contentDimmingFactor),
                primaryContainer = colorScheme.primaryContainer.darken(contentDimmingFactor),
                secondaryContainer = colorScheme.secondaryContainer.darken(contentDimmingFactor),
                tertiaryContainer = colorScheme.tertiaryContainer.darken(contentDimmingFactor),
                errorContainer = colorScheme.errorContainer.darken(contentDimmingFactor),
            )
        }
    } else colorScheme

    // 获取缩放后的排版
    val koriTypography = remember(fontScale) { Typography.withScaledFontSizes(fontScale) }

    CompositionLocalProvider(LocalAppConfig provides AppConfig(darkMode, amoledMode, fontScale)) {
        MaterialExpressiveTheme(
            colorScheme = finalColorScheme, typography = koriTypography, content = content
        )
    }
}