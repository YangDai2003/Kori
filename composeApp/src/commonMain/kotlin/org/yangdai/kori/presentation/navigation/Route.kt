package org.yangdai.kori.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen() {
    @Serializable
    data object Main : Screen()

    @Serializable
    data class Note(val id: String = "") : Screen()

    @Serializable
    data object File : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Folders : Screen()
}