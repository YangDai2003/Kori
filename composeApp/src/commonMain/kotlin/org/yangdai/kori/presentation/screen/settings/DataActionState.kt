package org.yangdai.kori.presentation.screen.settings

data class DataActionState(
    val progress: Float = -1f,
    val infinite: Boolean = false,
    val message: String = ""
)
