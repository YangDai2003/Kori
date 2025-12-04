package knet.ai.providers

import ai.koog.prompt.executor.clients.anthropic.AnthropicModels

object Anthropic {
    val anthropicModelMap = mapOf(
        AnthropicModels.Opus_3.id to AnthropicModels.Opus_3,
        AnthropicModels.Haiku_3.id to AnthropicModels.Haiku_3,
        AnthropicModels.Haiku_3_5.id to AnthropicModels.Haiku_3_5,
        AnthropicModels.Sonnet_3_5.id to AnthropicModels.Sonnet_3_5,
        AnthropicModels.Sonnet_3_7.id to AnthropicModels.Sonnet_3_7,
        AnthropicModels.Sonnet_4.id to AnthropicModels.Sonnet_4,
        AnthropicModels.Opus_4.id to AnthropicModels.Opus_4,
        AnthropicModels.Opus_4_1.id to AnthropicModels.Opus_4_1,
        AnthropicModels.Opus_4_5.id to AnthropicModels.Opus_4_5,
        AnthropicModels.Sonnet_4_5.id to AnthropicModels.Sonnet_4_5,
        AnthropicModels.Haiku_4_5.id to AnthropicModels.Haiku_4_5
    )

    fun getModel(id: String) = anthropicModelMap[id] ?: anthropicModelMap.values.first()
}