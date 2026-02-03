package knet.ai.providers

import ai.koog.prompt.executor.clients.google.GoogleModels
import androidx.compose.runtime.Stable

@Stable
object Google {
    val googleModelMap = mapOf(
        GoogleModels.Gemini2_0Flash.id to GoogleModels.Gemini2_0Flash,
        GoogleModels.Gemini2_0FlashLite.id to GoogleModels.Gemini2_0FlashLite,
        GoogleModels.Gemini2_5Pro.id to GoogleModels.Gemini2_5Pro,
        GoogleModels.Gemini2_5Flash.id to GoogleModels.Gemini2_5Flash,
        GoogleModels.Gemini2_5FlashLite.id to GoogleModels.Gemini2_5FlashLite
    )

    fun getModel(id: String) = googleModelMap[id] ?: googleModelMap.values.first()
}