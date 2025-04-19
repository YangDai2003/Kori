package org.yangdai.kori.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.yangdai.kori.domain.repository.DataStoreRepository
import org.yangdai.kori.presentation.state.AppColor
import org.yangdai.kori.presentation.state.AppTheme
import org.yangdai.kori.presentation.state.StylePaneState
import org.yangdai.kori.presentation.util.Constants

class SettingsViewModel(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val stylePaneState = combine(
        dataStoreRepository.intFlow(Constants.Preferences.APP_THEME),
        dataStoreRepository.intFlow(Constants.Preferences.APP_COLOR),
        dataStoreRepository.booleanFlow(Constants.Preferences.IS_APP_IN_AMOLED_MODE)
    ) { theme, color, isAppInAmoledMode ->
        StylePaneState(
            theme = AppTheme.fromInt(theme),
            color = AppColor.fromInt(color),
            isAppInAmoledMode = isAppInAmoledMode
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), StylePaneState())

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
