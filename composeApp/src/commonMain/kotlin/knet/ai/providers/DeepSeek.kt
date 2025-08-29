package knet.ai.providers

import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels

object DeepSeek {
    val modelOptions = mapOf(
        DeepSeekModels.DeepSeekChat.id to DeepSeekModels.DeepSeekChat
    )

    fun getModel(id: String) = modelOptions[id] ?: modelOptions.values.first()
}