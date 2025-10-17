package knet.ai.providers

import ai.koog.prompt.executor.clients.openai.OpenAIModels

object OpenAI {
    val openAIModelMap = mapOf(
        OpenAIModels.Chat.GPT4o.id to OpenAIModels.Chat.GPT4o,
        OpenAIModels.Chat.GPT4_1.id to OpenAIModels.Chat.GPT4_1,
        OpenAIModels.Chat.GPT5.id to OpenAIModels.Chat.GPT5,
        OpenAIModels.Chat.GPT5Mini.id to OpenAIModels.Chat.GPT5Mini,
        OpenAIModels.Chat.GPT5Nano.id to OpenAIModels.Chat.GPT5Nano,
        OpenAIModels.Chat.GPT5Codex.id to OpenAIModels.Chat.GPT5Codex,
        OpenAIModels.CostOptimized.GPT4_1Nano.id to OpenAIModels.CostOptimized.GPT4_1Nano,
        OpenAIModels.CostOptimized.GPT4_1Mini.id to OpenAIModels.CostOptimized.GPT4_1Mini,
        OpenAIModels.CostOptimized.GPT4oMini.id to OpenAIModels.CostOptimized.GPT4oMini
    )

    fun getModel(id: String) = openAIModelMap[id] ?: openAIModelMap.values.first()
}