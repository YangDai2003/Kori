package org.yangdai.kori.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val primaryLight = Color(0xFF8F4C38)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFFFDBD1)
private val onPrimaryContainerLight = Color(0xFF723523)
private val secondaryLight = Color(0xFF77574E)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFFFDBD1)
private val onSecondaryContainerLight = Color(0xFF5D4037)
private val tertiaryLight = Color(0xFF6C5D2F)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFF5E1A7)
private val onTertiaryContainerLight = Color(0xFF534619)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFFFF8F6)
private val onBackgroundLight = Color(0xFF231917)
private val surfaceLight = Color(0xFFFFF8F6)
private val onSurfaceLight = Color(0xFF231917)
private val surfaceVariantLight = Color(0xFFF5DED8)
private val onSurfaceVariantLight = Color(0xFF53433F)
private val outlineLight = Color(0xFF85736E)
private val outlineVariantLight = Color(0xFFD8C2BC)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF392E2B)
private val inverseOnSurfaceLight = Color(0xFFFFEDE8)
private val inversePrimaryLight = Color(0xFFFFB5A0)
private val surfaceDimLight = Color(0xFFE8D6D2)
private val surfaceBrightLight = Color(0xFFFFF8F6)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFFFF1ED)
private val surfaceContainerLight = Color(0xFFFCEAE5)
private val surfaceContainerHighLight = Color(0xFFF7E4E0)
private val surfaceContainerHighestLight = Color(0xFFF1DFDA)

private val primaryDark = Color(0xFFFFB5A0)
private val onPrimaryDark = Color(0xFF561F0F)
private val primaryContainerDark = Color(0xFF723523)
private val onPrimaryContainerDark = Color(0xFFFFDBD1)
private val secondaryDark = Color(0xFFE7BDB2)
private val onSecondaryDark = Color(0xFF442A22)
private val secondaryContainerDark = Color(0xFF5D4037)
private val onSecondaryContainerDark = Color(0xFFFFDBD1)
private val tertiaryDark = Color(0xFFD8C58D)
private val onTertiaryDark = Color(0xFF3B2F05)
private val tertiaryContainerDark = Color(0xFF534619)
private val onTertiaryContainerDark = Color(0xFFF5E1A7)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF1A110F)
private val onBackgroundDark = Color(0xFFF1DFDA)
private val surfaceDark = Color(0xFF1A110F)
private val onSurfaceDark = Color(0xFFF1DFDA)
private val surfaceVariantDark = Color(0xFF53433F)
private val onSurfaceVariantDark = Color(0xFFD8C2BC)
private val outlineDark = Color(0xFFA08C87)
private val outlineVariantDark = Color(0xFF53433F)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFF1DFDA)
private val inverseOnSurfaceDark = Color(0xFF392E2B)
private val inversePrimaryDark = Color(0xFF8F4C38)
private val surfaceDimDark = Color(0xFF1A110F)
private val surfaceBrightDark = Color(0xFF423734)
private val surfaceContainerLowestDark = Color(0xFF140C0A)
private val surfaceContainerLowDark = Color(0xFF231917)
private val surfaceContainerDark = Color(0xFF271D1B)
private val surfaceContainerHighDark = Color(0xFF322825)
private val surfaceContainerHighestDark = Color(0xFF3D322F)

val LightRedColors = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
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


val DarkRedColors = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
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