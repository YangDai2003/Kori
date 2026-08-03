package knet.ai.providers

import ai.koog.prompt.executor.clients.google.GoogleModels
import androidx.compose.runtime.Stable

@Stable
object Google {
    val googleModelMap = GoogleModels.models.associateBy { it.id }

    fun getModel(id: String) = googleModelMap[id] ?: googleModelMap.values.first()
}