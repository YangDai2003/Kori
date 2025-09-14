package org.yangdai.kori.presentation.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.window.core.layout.WindowSizeClass
import org.yangdai.kori.data.local.entity.NoteEntity
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun rememberIsScreenWidthExpanded(): Boolean {
    return currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
}

fun Int.toHexColor(): String =
    (0xFFFFFF and this).toHexString(
        HexFormat {
            upperCase = false
            number {
                minLength = 6
                removeLeadingZeros = true
                prefix = "#"
            }
        }
    )

@Composable
expect fun Modifier.clickToShareText(text: String): Modifier

@Composable
expect fun Modifier.clickToShareFile(noteEntity: NoteEntity): Modifier

@OptIn(ExperimentalTime::class)
expect fun formatInstant(instant: Instant): String

expect fun formatNumber(int: Int): String

expect fun clipEntryOf(string: String): ClipEntry

expect fun shouldShowLanguageSetting(): Boolean

@Composable
expect fun Modifier.clickToLanguageSetting(): Modifier