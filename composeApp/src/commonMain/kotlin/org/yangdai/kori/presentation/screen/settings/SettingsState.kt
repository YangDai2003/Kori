package org.yangdai.kori.presentation.screen.settings

import org.yangdai.kori.data.local.entity.NoteType

data class StylePaneState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val color: AppColor = AppColor.DYNAMIC,
    val isAppInAmoledMode: Boolean = false,
    val fontSize: Float = 1f
)

data class SecurityPaneState(
    val isScreenProtected: Boolean = false,
    val password: String = "",
    val isCreatingPass: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

data class EditorPaneState(
    val showLineNumber: Boolean = false,
    val isMarkdownLintEnabled: Boolean = false,
    val isDefaultReadingView: Boolean = false,
    val defaultNoteType: NoteType = NoteType.PLAIN_TEXT,
)

data class TemplatePaneState(
    val dateFormatter: String = "",
    val timeFormatter: String = ""
)

data class CardPaneState(
    val cardSize: CardSize = CardSize.DEFAULT,
    val clipOverflow: Boolean = false
)

data class AiPaneState(
    val isAiEnabled: Boolean = false,
    val aiFeatures: Set<String> = emptySet(),
    val aiProvider: AiProvider = AiProvider.Gemini
)

data class GeminiState(
    val apiKey: String = "",
    val apiHost: String = "",
    val model: String = ""
)

data class OpenAiState(
    val apiKey: String = "",
    val apiHost: String = "",
    val model: String = ""
)

data class OllamaState(
    val apiHost: String = "",
    val model: String = ""
)

data class LmStudioState(
    val apiHost: String = "",
    val model: String = ""
)

enum class AiProvider(val provider: String) {
    Gemini("Gemini"),
    OpenAI("Open AI"),
    Ollama("Ollama"),
    LMStudio("LM Studio");

    companion object {
        fun fromString(value: String) = entries.firstOrNull { it.provider == value } ?: Gemini
    }
}

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
    CYAN(6);


    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: DYNAMIC
        fun AppColor.toInt() = value
    }
}