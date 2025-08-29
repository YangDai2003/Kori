package knet.ai.providers

import ai.koog.prompt.executor.clients.openai.OpenAIModels

object OpenAI {
    val modelOptions = mapOf(
        OpenAIModels.Chat.GPT4o.id to OpenAIModels.Chat.GPT4o,
        OpenAIModels.Chat.GPT4_1.id to OpenAIModels.Chat.GPT4_1,
        OpenAIModels.Chat.GPT5.id to OpenAIModels.Chat.GPT5,
        OpenAIModels.Chat.GPT5Mini.id to OpenAIModels.Chat.GPT5Mini,
        OpenAIModels.Chat.GPT5Nano.id to OpenAIModels.Chat.GPT5Nano
    )

    fun getModel(id: String) = modelOptions[id] ?: modelOptions.values.first()
}