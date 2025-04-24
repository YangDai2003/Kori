package org.yangdai.kori.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.presentation.util.Constants

class SettingsViewModel(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val stylePaneState = combine(
        dataStoreRepository.intFlow(Constants.Preferences.APP_THEME),
        dataStoreRepository.intFlow(Constants.Preferences.APP_COLOR),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_APP_IN_AMOLED_MODE),
        dataStoreRepository.floatFlow(Constants.Preferences.FONT_SIZE)
    ) { theme, color, isAppInAmoledMode, fontSize ->
        StylePaneState(
            theme = AppTheme.Companion.fromInt(theme),
            color = AppColor.Companion.fromInt(color),
            isAppInAmoledMode = isAppInAmoledMode,
            fontSize = fontSize
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), StylePaneState())

    val securityPaneState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_SCREEN_PROTECTED),
        dataStoreRepository.stringFlow(Constants.Preferences.PASSWORD),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_CREATING_PASSWORD),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_BIOMETRIC_ENABLED)
    ) { isScreenProtected, password, isCreatingPass, isBiometricEnabled ->
        SecurityPaneState(
            isScreenProtected = isScreenProtected,
            password = password,
            isCreatingPass = isCreatingPass,
            isBiometricEnabled = isBiometricEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), SecurityPaneState())

    val editorPaneState = combine(
        dataStoreRepository.booleanFlow(Constants.Preferences.SHOW_LINE_NUMBER),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_MARKDOWN_LINT_ENABLED)
    ) { showLineNumber, isMarkdownLintEnabled ->
        EditorPaneState(
            showLineNumber = showLineNumber,
            isMarkdownLintEnabled = isMarkdownLintEnabled
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), EditorPaneState())

    val templatePaneState = combine(
        dataStoreRepository.stringFlow(Constants.Preferences.DATE_FORMATTER),
        dataStoreRepository.stringFlow(Constants.Preferences.TIME_FORMATTER)
    ) { dateFormatter, timeFormatter ->
        TemplatePaneState(
            dateFormatter = dateFormatter,
            timeFormatter = timeFormatter
        )
    }.stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000L), TemplatePaneState())

    fun <T> putPreferenceValue(key: String, value: T) {
        viewModelScope.launch {
            when (value) {
                is Int -> dataStoreRepository.putInt(key, value)
                is Float -> dataStoreRepository.putFloat(key, value)
                is Boolean -> dataStoreRepository.putBoolean(key, value)
                is String -> dataStoreRepository.putString(key, value)
                is Set<*> -> dataStoreRepository.putStringSet(
                    key, value.filterIsInstance<String>().toSet()
                )

                else -> throw IllegalArgumentException("Unsupported value type")
            }
        }
    }
}