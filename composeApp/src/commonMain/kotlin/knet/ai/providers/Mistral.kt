package knet.ai.providers

import ai.koog.prompt.executor.clients.mistralai.MistralAIModels
import androidx.compose.runtime.Stable

@Stable
object Mistral {
    val mistralModelMap = MistralAIModels.models.associateBy { it.id }

    fun getModel(id: String) = mistralModelMap[id] ?: mistralModelMap.values.first()
}