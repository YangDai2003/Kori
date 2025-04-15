package org.yangdai.kori.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import org.yangdai.kori.presentation.state.AppColor

fun darkenColor(color: Color, factor: Float): Color = lerp(color, Color.Black, factor)

@Composable
expect fun KoriTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    color: AppColor = AppColor.DYNAMIC,
    fontScale: Float = 1f,
    content: @Composable () -> Unit
)
