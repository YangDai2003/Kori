package org.yangdai.kori.presentation.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.datetime.Instant
import org.yangdai.kori.Platform
import org.yangdai.kori.currentPlatform
import org.yangdai.kori.data.local.entity.NoteEntity

@Composable
fun rememberIsScreenSizeLarge(): Boolean {
    val windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isLargeScreen by remember(windowSizeClass.windowWidthSizeClass) {
        derivedStateOf {
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
        }
    }
    return isLargeScreen
}

@Composable
fun rememberCurrentPlatform(): Platform = remember { currentPlatform() }

fun Int.toHexColor(): String {
    // 1. 使用 0xFFFFFF 进行位与操作，保留低 24 位 (RGB)
    //    并确保结果为非负数，这对于 toString(16) 很重要，
    //    虽然对于 0xFFFFFF and this 来说，结果总是正数或零。
    val rgb = 0xFFFFFF and this

    // 2. 将 RGB 整数值转换为十六进制字符串
    //    toString(16) 会生成小写字母的十六进制，例如 "ff00ff"
    val hexString = rgb.toString(16)

    // 3. 使用 padStart 确保字符串长度为 6
    //    如果 hexString 长度小于 6（例如颜色值为 0xFF，转换后是 "ff"），
    //    则在前面用 '0' 填充，直到长度达到 6 ("0000ff")。
    val paddedHexString = hexString.padStart(6, '0')

    // 4. 在前面加上 '#' 符号
    return "#$paddedHexString"
}

@Composable
expect fun Modifier.clickToShareText(text: String): Modifier

@Composable
expect fun Modifier.clickToShareFile(noteEntity: NoteEntity): Modifier

expect fun formatInstant(instant: Instant): String

expect fun formatNumber(int: Int): String

expect fun clipEntryOf(string: String): ClipEntry

expect fun shouldShowLanguageSetting(): Boolean

@Composable
expect fun Modifier.clickToLanguageSetting(): Modifier