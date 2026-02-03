package knet.ai.providers

import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import androidx.compose.runtime.Stable

@Stable
object DeepSeek {
    val deepSeekModelMap = mapOf(
        DeepSeekModels.DeepSeekChat.id to DeepSeekModels.DeepSeekChat
    )

    fun getModel(id: String) = deepSeekModelMap[id] ?: deepSeekModelMap.values.first()
}