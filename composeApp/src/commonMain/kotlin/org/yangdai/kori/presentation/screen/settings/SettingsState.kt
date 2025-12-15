package org.yangdai.kori.presentation.screen.settings

import ai.koog.prompt.llm.LLMProvider
import androidx.compose.runtime.Immutable

@Immutable
data class StylePaneState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val color: AppColor = AppColor.DYNAMIC,
    val isAppInAmoledMode: Boolean = false,
    val fontSize: Float = 1f
)

@Immutable
data class SecurityPaneState(
    val isScreenProtected: Boolean = false,
    val password: String = "",
    val isCreatingPass: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val keepScreenOn: Boolean = false
)

@Immutable
data class EditorPaneState(
    val isLineNumberVisible: Boolean = false,
    val isLintingEnabled: Boolean = false,
    val isDefaultReadingView: Boolean = false,
    val editorWeight: Float = 0.5f
)

@Immutable
data class TemplatePaneState(
    val dateFormatter: String = "",
    val timeFormatter: String = ""
)

@Immutable
data class CardPaneState(
    val cardSize: CardSize = CardSize.DEFAULT,
    val clipOverflow: Boolean = false
)

@Immutable
data class AiPaneState(
    val isAiEnabled: Boolean = false,
    val llmProvider: LLMProvider = LLMProvider.Google
)

enum class CardSize(private val value: Int) {
    DEFAULT(0),
    TITLE_ONLY(1),
    COMPACT(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: DEFAULT
        fun CardSize.toInt() = value
    }
}

enum class AppTheme(private val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: SYSTEM
        fun AppTheme.toInt() = value
    }
}

enum class AppColor(private val value: Int) {
    DYNAMIC(0),
    PURPLE(1),
    BLUE(2),
    GREEN(3),
    ORANGE(4),
    RED(5),
    CYAN(6),
    BLACK(7);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: DYNAMIC
        fun AppColor.toInt() = value
    }
}