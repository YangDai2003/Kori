package org.yangdai.kori.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen() {
    @Serializable
    data object Main : Screen()

    @Serializable
    data class Note(
        val id: String = "",
        val folderId: String? = null,
        val sharedContentTitle: String = "",
        val sharedContentText: String = ""
    ) : Screen()

    @Serializable
    data class Template(val id: String = "") : Screen()

    @Serializable
    data class File(val path: String = "") : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Folders : Screen()
}