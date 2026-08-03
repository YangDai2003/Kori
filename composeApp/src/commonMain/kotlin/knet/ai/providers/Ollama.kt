package knet.ai.providers

import ai.koog.prompt.executor.ollama.client.OllamaModels
import androidx.compose.runtime.Stable

@Stable
object Ollama {
    val ollamaModelMap = OllamaModels.models.associateBy { it.id }

    fun getModel(id: String) = ollamaModelMap[id] ?: ollamaModelMap.values.first()
}