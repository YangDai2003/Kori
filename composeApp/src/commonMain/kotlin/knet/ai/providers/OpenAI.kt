package knet.ai.providers

import ai.koog.prompt.executor.clients.openai.OpenAIModels
import androidx.compose.runtime.Stable

@Stable
object OpenAI {
    val openAIModelMap = OpenAIModels.models.associateBy { it.id }

    fun getModel(id: String) = openAIModelMap[id] ?: openAIModelMap.values.first()
}