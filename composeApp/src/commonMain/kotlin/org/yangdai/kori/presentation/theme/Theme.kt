package org.yangdai.kori.presentation.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

fun darkenColor(color: Color, factor: Float): Color = lerp(color, Color.Black, factor)

@Composable
fun KoriTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    var colorScheme = if (darkMode) DarkPurpleColors else LightPurpleColors

    val contentTargetFactor = remember(amoledMode) { if (amoledMode) 0.1f else 0f }
    val contentDimmingFactor by animateFloatAsState(
        targetValue = contentTargetFactor,
        animationSpec = tween(durationMillis = 1000) // 1 second duration
    )
    val backgroundTargetFactor = remember(amoledMode) { if (amoledMode) 1f else 0f }
    val backgroundDimmingFactor by animateFloatAsState(
        targetValue = backgroundTargetFactor,
        animationSpec = tween(durationMillis = 1000) // 1 second duration
    )

    if (darkMode) colorScheme = colorScheme.copy(
        background = darkenColor(
            colorScheme.background, backgroundDimmingFactor
        ), surface = darkenColor(
            colorScheme.surface, backgroundDimmingFactor
        ), surfaceContainer = darkenColor(
            colorScheme.surfaceContainer, backgroundDimmingFactor
        ), surfaceVariant = darkenColor(
            colorScheme.surfaceVariant, contentDimmingFactor
        ), surfaceContainerLowest = darkenColor(
            colorScheme.surfaceContainerLowest, contentDimmingFactor
        ), surfaceContainerLow = darkenColor(
            colorScheme.surfaceContainerLow, contentDimmingFactor
        ), surfaceContainerHigh = darkenColor(
            colorScheme.surfaceContainerHigh, contentDimmingFactor
        ), surfaceContainerHighest = darkenColor(
            colorScheme.surfaceContainerHighest, contentDimmingFactor
        ), surfaceDim = darkenColor(
            colorScheme.surfaceDim, contentDimmingFactor
        ), surfaceBright = darkenColor(
            colorScheme.surfaceBright, contentDimmingFactor
        ), scrim = darkenColor(
            colorScheme.scrim, contentDimmingFactor
        ), inverseSurface = darkenColor(
            colorScheme.inverseSurface, contentDimmingFactor
        ), errorContainer = darkenColor(
            colorScheme.errorContainer, contentDimmingFactor
        ), tertiaryContainer = darkenColor(
            colorScheme.tertiaryContainer, contentDimmingFactor
        ), secondaryContainer = darkenColor(
            colorScheme.secondaryContainer, contentDimmingFactor
        ), primaryContainer = darkenColor(
            colorScheme.primaryContainer, contentDimmingFactor
        )
    )

    var koriTypography by remember { mutableStateOf(Typography) }
    LaunchedEffect(fontScale) {
        koriTypography = Typography.copy(
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

    MaterialTheme(
        colorScheme = colorScheme, typography = koriTypography, content = content
    )
}