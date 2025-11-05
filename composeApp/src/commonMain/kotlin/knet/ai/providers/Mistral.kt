package knet.ai.providers

import ai.koog.prompt.executor.clients.mistralai.MistralAIModels

object Mistral {
    val mistralModelMap = mapOf(
        MistralAIModels.Chat.MistralMedium31.id to MistralAIModels.Chat.MistralMedium31,
        MistralAIModels.Chat.MistralLarge21.id to MistralAIModels.Chat.MistralLarge21,
        MistralAIModels.Chat.MistralSmall2.id to MistralAIModels.Chat.MistralSmall2,
        MistralAIModels.Chat.MagistralMedium12.id to MistralAIModels.Chat.MagistralMedium12,
        MistralAIModels.Chat.Codestral.id to MistralAIModels.Chat.Codestral,
        MistralAIModels.Chat.DevstralMedium.id to MistralAIModels.Chat.DevstralMedium
    )

    fun getModel(id: String) = mistralModelMap[id] ?: mistralModelMap.values.first()
}