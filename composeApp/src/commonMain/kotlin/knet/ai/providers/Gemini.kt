package knet.ai.providers

import ai.koog.prompt.executor.clients.google.GoogleModels

object Gemini {
    val modelOptions = mapOf(
        GoogleModels.Gemini2_0Flash.id to GoogleModels.Gemini2_0Flash,
        GoogleModels.Gemini2_0FlashLite.id to GoogleModels.Gemini2_0FlashLite,
        GoogleModels.Gemini2_5Pro.id to GoogleModels.Gemini2_5Pro,
        GoogleModels.Gemini2_5Flash.id to GoogleModels.Gemini2_5Flash
    )

    fun getModel(id: String) = modelOptions[id] ?: modelOptions.values.first()
}