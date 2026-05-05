package knet.ai.providers

import ai.koog.prompt.executor.clients.google.GoogleModels
import androidx.compose.runtime.Stable

@Stable
object Google {
    val googleModelMap = mapOf(
        GoogleModels.Gemini2_5Pro.id to GoogleModels.Gemini2_5Pro,
        GoogleModels.Gemini2_5Flash.id to GoogleModels.Gemini2_5Flash,
        GoogleModels.Gemini2_5FlashLite.id to GoogleModels.Gemini2_5FlashLite,
        GoogleModels.Gemini3_Pro_Preview.id to GoogleModels.Gemini3_Pro_Preview,
        GoogleModels.Gemini3_Flash_Preview.id to GoogleModels.Gemini3_Flash_Preview
    )

    fun getModel(id: String) = googleModelMap[id] ?: googleModelMap.values.first()
}