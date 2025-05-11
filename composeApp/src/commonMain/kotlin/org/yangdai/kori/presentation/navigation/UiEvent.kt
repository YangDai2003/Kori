package org.yangdai.kori.presentation.navigation

sealed interface UiEvent {
    data object NavigateUp : UiEvent
}