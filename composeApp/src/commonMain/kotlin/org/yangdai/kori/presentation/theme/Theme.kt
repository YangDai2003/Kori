package org.yangdai.kori.presentation.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import org.yangdai.kori.presentation.screen.settings.AppColor

// 色彩过渡动画时长常量（毫秒）
private const val COLOR_ANIMATION_DURATION = 700

// 通过对象而不是变量来跟踪首次启动状态，避免多处状态不一致
private var isFirstLaunch = true

/**
 * 使颜色变暗的辅助函数
 * @param color 要变暗的颜色
 * @param factor 变暗因子，范围0-1
 * @return 变暗后的颜色
 */
@Stable
fun darkenColor(color: Color, factor: Float): Color = lerp(color, Color.Black, factor)

/**
 * 为颜色方案中的单个颜色创建动画
 */
@Composable
private fun animateColor(
    targetValue: Color,
    animationSpec: AnimationSpec<Color> = tween(COLOR_ANIMATION_DURATION)
): Color {
    return animateColorAsState(
        targetValue = targetValue,
        animationSpec = animationSpec
    ).value
}

/**
 * 为颜色方案创建平滑过渡动画，优化实现减少重复代码
 * @param targetColorScheme 目标颜色方案
 * @return 具有动画效果的颜色方案
 */
@Composable
fun animateColorSchemeAsState(targetColorScheme: ColorScheme): ColorScheme {
    return ColorScheme(
        primary = animateColor(targetColorScheme.primary),
        onPrimary = animateColor(targetColorScheme.onPrimary),
        primaryContainer = animateColor(targetColorScheme.primaryContainer),
        onPrimaryContainer = animateColor(targetColorScheme.onPrimaryContainer),
        secondary = animateColor(targetColorScheme.secondary),
        onSecondary = animateColor(targetColorScheme.onSecondary),
        secondaryContainer = animateColor(targetColorScheme.secondaryContainer),
        onSecondaryContainer = animateColor(targetColorScheme.onSecondaryContainer),
        tertiary = animateColor(targetColorScheme.tertiary),
        onTertiary = animateColor(targetColorScheme.onTertiary),
        tertiaryContainer = animateColor(targetColorScheme.tertiaryContainer),
        onTertiaryContainer = animateColor(targetColorScheme.onTertiaryContainer),
        error = animateColor(targetColorScheme.error),
        errorContainer = animateColor(targetColorScheme.errorContainer),
        onError = animateColor(targetColorScheme.onError),
        onErrorContainer = animateColor(targetColorScheme.onErrorContainer),
        background = animateColor(targetColorScheme.background),
        onBackground = animateColor(targetColorScheme.onBackground),
        surface = animateColor(targetColorScheme.surface),
        onSurface = animateColor(targetColorScheme.onSurface),
        surfaceVariant = animateColor(targetColorScheme.surfaceVariant),
        onSurfaceVariant = animateColor(targetColorScheme.onSurfaceVariant),
        outline = animateColor(targetColorScheme.outline),
        inverseOnSurface = animateColor(targetColorScheme.inverseOnSurface),
        inverseSurface = animateColor(targetColorScheme.inverseSurface),
        inversePrimary = animateColor(targetColorScheme.inversePrimary),
        surfaceTint = animateColor(targetColorScheme.surfaceTint),
        outlineVariant = animateColor(targetColorScheme.outlineVariant),
        scrim = animateColor(targetColorScheme.scrim),
        surfaceBright = animateColor(targetColorScheme.surfaceBright),
        surfaceContainer = animateColor(targetColorScheme.surfaceContainer),
        surfaceContainerHigh = animateColor(targetColorScheme.surfaceContainerHigh),
        surfaceContainerHighest = animateColor(targetColorScheme.surfaceContainerHighest),
        surfaceContainerLow = animateColor(targetColorScheme.surfaceContainerLow),
        surfaceContainerLowest = animateColor(targetColorScheme.surfaceContainerLowest),
        surfaceDim = animateColor(targetColorScheme.surfaceDim)
    )
}

/**
 * 获取合适的颜色方案，处理首次启动时不使用动画
 */
@Composable
fun getColorSchemeWithAnimation(targetColorScheme: ColorScheme): ColorScheme {
    if (isFirstLaunch) {
        LaunchedEffect(Unit) {
            // 标记首次启动已完成
            isFirstLaunch = false
        }
        // 首次启动，直接使用目标颜色方案，不使用动画
        return targetColorScheme
    }

    // 非首次启动，使用动画过渡
    return animateColorSchemeAsState(targetColorScheme)
}

/**
 * 处理AMOLED模式的颜色调整
 */
@Composable
fun processAmoledMode(
    darkMode: Boolean,
    amoledMode: Boolean,
    colorScheme: ColorScheme
): ColorScheme {
    // 仅在深色模式下处理AMOLED选项
    if (!darkMode) return colorScheme

    val contentTargetFactor by remember(amoledMode) {
        derivedStateOf { if (amoledMode) 0.1f else 0f }
    }
    val contentDimmingFactor by animateFloatAsState(
        targetValue = contentTargetFactor,
        animationSpec = tween(durationMillis = 1000),
        label = "ContentDimming"
    )

    val backgroundTargetFactor by remember(amoledMode) {
        derivedStateOf { if (amoledMode) 1f else 0f }
    }
    val backgroundDimmingFactor by animateFloatAsState(
        targetValue = backgroundTargetFactor,
        animationSpec = tween(durationMillis = 1000),
        label = "BackgroundDimming"
    )

    // 使用remember缓存转换结果，避免每次重组都创建新对象
    return remember(colorScheme, contentDimmingFactor, backgroundDimmingFactor) {
        colorScheme.copy(
            background = darkenColor(colorScheme.background, backgroundDimmingFactor),
            surface = darkenColor(colorScheme.surface, backgroundDimmingFactor),
            surfaceContainer = darkenColor(colorScheme.surfaceContainer, backgroundDimmingFactor),
            surfaceVariant = darkenColor(colorScheme.surfaceVariant, contentDimmingFactor),
            surfaceContainerLowest = darkenColor(
                colorScheme.surfaceContainerLowest,
                contentDimmingFactor
            ),
            surfaceContainerLow = darkenColor(
                colorScheme.surfaceContainerLow,
                contentDimmingFactor
            ),
            surfaceContainerHigh = darkenColor(
                colorScheme.surfaceContainerHigh,
                contentDimmingFactor
            ),
            surfaceContainerHighest = darkenColor(
                colorScheme.surfaceContainerHighest,
                contentDimmingFactor
            ),
            surfaceDim = darkenColor(colorScheme.surfaceDim, contentDimmingFactor),
            surfaceBright = darkenColor(colorScheme.surfaceBright, contentDimmingFactor),
            scrim = darkenColor(colorScheme.scrim, contentDimmingFactor),
            inverseSurface = darkenColor(colorScheme.inverseSurface, contentDimmingFactor),
            errorContainer = darkenColor(colorScheme.errorContainer, contentDimmingFactor),
            tertiaryContainer = darkenColor(colorScheme.tertiaryContainer, contentDimmingFactor),
            secondaryContainer = darkenColor(colorScheme.secondaryContainer, contentDimmingFactor),
            primaryContainer = darkenColor(colorScheme.primaryContainer, contentDimmingFactor)
        )
    }
}

/**
 * 根据fontScale调整Typography，使用缓存优化性能
 */
@Composable
fun getScaledTypography(fontScale: Float): androidx.compose.material3.Typography {
    // 使用remember避免不必要的重组
    return remember(fontScale) {
        Typography.copy(
            displayLarge = Typography.displayLarge.copy(fontSize = Typography.displayLarge.fontSize * fontScale),
            displayMedium = Typography.displayMedium.copy(fontSize = Typography.displayMedium.fontSize * fontScale),
            displaySmall = Typography.displaySmall.copy(fontSize = Typography.displaySmall.fontSize * fontScale),
            headlineLarge = Typography.headlineLarge.copy(fontSize = Typography.headlineLarge.fontSize * fontScale),
            headlineMedium = Typography.headlineMedium.copy(fontSize = Typography.headlineMedium.fontSize * fontScale),
            headlineSmall = Typography.headlineSmall.copy(fontSize = Typography.headlineSmall.fontSize * fontScale),
            titleLarge = Typography.titleLarge.copy(fontSize = Typography.titleLarge.fontSize * fontScale),
            titleMedium = Typography.titleMedium.copy(fontSize = Typography.titleMedium.fontSize * fontScale),
            titleSmall = Typography.titleSmall.copy(fontSize = Typography.titleSmall.fontSize * fontScale),
            bodyLarge = Typography.bodyLarge.copy(fontSize = Typography.bodyLarge.fontSize * fontScale),
            bodyMedium = Typography.bodyMedium.copy(fontSize = Typography.bodyMedium.fontSize * fontScale),
            bodySmall = Typography.bodySmall.copy(fontSize = Typography.bodySmall.fontSize * fontScale),
            labelLarge = Typography.labelLarge.copy(fontSize = Typography.labelLarge.fontSize * fontScale),
            labelMedium = Typography.labelMedium.copy(fontSize = Typography.labelMedium.fontSize * fontScale),
            labelSmall = Typography.labelSmall.copy(fontSize = Typography.labelSmall.fontSize * fontScale)
        )
    }
}

@Composable
expect fun KoriTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    color: AppColor = AppColor.DYNAMIC,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
)
