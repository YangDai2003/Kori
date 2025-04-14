package org.yangdai.kori.presentation.event

sealed interface UiEvent {
    data object NavigateUp : UiEvent
}