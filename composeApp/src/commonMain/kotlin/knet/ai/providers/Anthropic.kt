package knet.ai.providers

import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import androidx.compose.runtime.Stable

@Stable
object Anthropic {
    val anthropicModelMap = AnthropicModels.models.associateBy { it.id }

    fun getModel(id: String) = anthropicModelMap[id] ?: anthropicModelMap.values.first()
}