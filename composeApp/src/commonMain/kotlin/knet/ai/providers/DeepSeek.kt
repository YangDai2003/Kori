package knet.ai.providers

import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels

object DeepSeek {
    val deepSeekModelMap = mapOf(
        DeepSeekModels.DeepSeekChat.id to DeepSeekModels.DeepSeekChat
    )

    fun getModel(id: String) = deepSeekModelMap[id] ?: deepSeekModelMap.values.first()
}