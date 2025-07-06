package org.yangdai.kori.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Light Monochrome Palette
private val primaryLight = Color(0xFF212121) // Main interactive elements (dark gray)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFE0E0E0)
private val onPrimaryContainerLight = Color(0xFF212121)
private val secondaryLight = Color(0xFF616161) // Secondary elements (medium gray)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFF5F5F5)
private val onSecondaryContainerLight = Color(0xFF424242)
private val backgroundLight = Color(0xFFFFFFFF) // Pure white background
private val onBackgroundLight = Color(0xFF1C1C1C) // Main text color (near black)
private val surfaceLight = Color(0xFFFFFFFF) // Surface is same as background
private val onSurfaceLight = Color(0xFF1C1C1C)
private val surfaceVariantLight = Color(0xFFEEEEEE) // For elements like cards or chips
private val onSurfaceVariantLight = Color(0xFF424242)
private val outlineLight = Color(0xFF757575) // Standard outlines
private val outlineVariantLight = Color(0xFFBDBDBD) // Lighter outlines
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF313131) // Corresponds to dark theme surface
private val inverseOnSurfaceLight = Color(0xFFF5F5F5) // Corresponds to dark theme text
private val inversePrimaryLight = Color(0xFFE0E0E0)
private val surfaceDimLight = Color(0xFFE0E0E0)
private val surfaceBrightLight = Color(0xFFFFFFFF)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFF2F3F5) // 与 secondaryContainerLight 区分，略微偏冷灰
private val surfaceContainerLight = Color(0xFFEEEEEE)
private val surfaceContainerHighLight = Color(0xFFE0E0E0)
private val surfaceContainerHighestLight = Color(0xFFD6D6D6)

// Dark Monochrome Palette
private val primaryDark = Color(0xFFFFFFFF) // Main interactive elements (white)
private val onPrimaryDark = Color(0xFF121212)
private val primaryContainerDark = Color(0xFF333333)
private val onPrimaryContainerDark = Color(0xFFF5F5F5)
private val secondaryDark = Color(0xFFBDBDBD) // Secondary elements (light gray)
private val onSecondaryDark = Color(0xFF212121)
private val secondaryContainerDark = Color(0xFF424242)
private val onSecondaryContainerDark = Color(0xFFE0E0E0)
private val backgroundDark = Color(0xFF121212) // Classic dark background
private val onBackgroundDark = Color(0xFFE0E0E0) // Main text color (off-white)
private val surfaceDark = Color(0xFF121212) // Surface is same as background
private val onSurfaceDark = Color(0xFFE0E0E0)
private val surfaceVariantDark = Color(0xFF424242) // For elements like cards or chips
private val onSurfaceVariantDark = Color(0xFFBDBDBD)
private val outlineDark = Color(0xFF9E9E9E) // Standard outlines
private val outlineVariantDark = Color(0xFF424242) // Darker outlines
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFE0E0E0) // Corresponds to light theme surface
private val inverseOnSurfaceDark = Color(0xFF313131) // Corresponds to light theme text
private val inversePrimaryDark = Color(0xFF212121)
private val surfaceDimDark = Color(0xFF121212)
private val surfaceBrightDark = Color(0xFF393939)
private val surfaceContainerLowestDark = Color(0xFF0D0D0D)
private val surfaceContainerLowDark = Color(0xFF1A1A1A)
private val surfaceContainerDark = Color(0xFF1F1F1F)
private val surfaceContainerHighDark = Color(0xFF292929)
private val surfaceContainerHighestDark = Color(0xFF333333)

private val tertiaryLight = Color(0xFF705575)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFFAD8FD)
private val onTertiaryContainerLight = Color(0xFF573E5C)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)

private val tertiaryDark = Color(0xFFDDBCE0)
private val onTertiaryDark = Color(0xFF3F2844)
private val tertiaryContainerDark = Color(0xFF573E5C)
private val onTertiaryContainerDark = Color(0xFFFAD8FD)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)

val LightBlackColors = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight, // Unchanged
    onTertiary = onTertiaryLight, // Unchanged
    tertiaryContainer = tertiaryContainerLight, // Unchanged
    onTertiaryContainer = onTertiaryContainerLight, // Unchanged
    error = errorLight, // Unchanged
    onError = onErrorLight, // Unchanged
    errorContainer = errorContainerLight, // Unchanged
    onErrorContainer = onErrorContainerLight, // Unchanged
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inverseSurface = inverseSurfaceLight,
    inversePrimary = inversePrimaryLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
    surfaceBright = surfaceBrightLight,
    surfaceDim = surfaceDimLight,
)


val DarkBlackColors = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark, // Unchanged
    onTertiary = onTertiaryDark, // Unchanged
    tertiaryContainer = tertiaryContainerDark, // Unchanged
    onTertiaryContainer = onTertiaryContainerDark, // Unchanged
    error = errorDark, // Unchanged
    onError = onErrorDark, // Unchanged
    errorContainer = errorContainerDark, // Unchanged
    onErrorContainer = onErrorContainerDark, // Unchanged
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inverseSurface = inverseSurfaceDark,
    inversePrimary = inversePrimaryDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
    surfaceBright = surfaceBrightDark,
    surfaceDim = surfaceDimDark,
)