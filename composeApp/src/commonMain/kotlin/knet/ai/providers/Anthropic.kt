package knet.ai.providers

import ai.koog.prompt.executor.clients.anthropic.AnthropicModels

object Anthropic {
    val modelOptions = mapOf(
        AnthropicModels.Opus_3.id to AnthropicModels.Opus_3,
        AnthropicModels.Haiku_3.id to AnthropicModels.Haiku_3,
        AnthropicModels.Sonnet_3_5.id to AnthropicModels.Sonnet_3_5,
        AnthropicModels.Haiku_3_5.id to AnthropicModels.Haiku_3_5,
        AnthropicModels.Sonnet_3_7.id to AnthropicModels.Sonnet_3_7,
        AnthropicModels.Sonnet_4.id to AnthropicModels.Sonnet_4,
        AnthropicModels.Opus_4.id to AnthropicModels.Opus_4
    )

    fun getModel(id: String) = modelOptions[id] ?: modelOptions.values.first()
}