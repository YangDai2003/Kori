package knet.ai.providers

import ai.koog.prompt.executor.clients.dashscope.DashscopeModels
import androidx.compose.runtime.Stable

@Stable
object Alibaba {
    val alibabaModelMap = DashscopeModels.models.associateBy { it.id }

    fun getModel(id: String) = alibabaModelMap[id] ?: alibabaModelMap.values.first()
}