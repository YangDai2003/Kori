package org.yangdai.kori.presentation.navigation

import kotlinx.serialization.Serializable
import org.yangdai.kori.data.local.entity.NoteType

@Serializable
sealed class Screen {
    @Serializable
    data object Main : Screen()

    @Serializable
    data class Note(
        val id: String = "",
        val noteType: Int = NoteType.PLAIN_TEXT.ordinal,
        val folderId: String? = null,
        val sharedContentTitle: String = "",
        val sharedContentText: String = ""
    ) : Screen()

    @Serializable
    data class Template(
        val id: String = "",
        val noteType: Int = NoteType.PLAIN_TEXT.ordinal
    ) : Screen()

    @Serializable
    data class File(val path: String = "") : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object Folders : Screen()
}