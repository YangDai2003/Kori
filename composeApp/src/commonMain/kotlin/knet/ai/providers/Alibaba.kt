package knet.ai.providers

import ai.koog.prompt.executor.clients.dashscope.DashscopeModels

object Alibaba {
    val alibabaModelMap = mapOf(
        DashscopeModels.QWEN_FLASH.id to DashscopeModels.QWEN_FLASH,
        DashscopeModels.QWEN3_OMNI_FLASH.id to DashscopeModels.QWEN3_OMNI_FLASH,
        DashscopeModels.QWEN_PLUS.id to DashscopeModels.QWEN_PLUS,
        DashscopeModels.QWEN_PLUS_LATEST.id to DashscopeModels.QWEN_PLUS_LATEST,
        DashscopeModels.QWEN3_CODER_PLUS.id to DashscopeModels.QWEN3_CODER_PLUS,
        DashscopeModels.QWEN3_CODER_FLASH.id to DashscopeModels.QWEN3_CODER_FLASH,
        DashscopeModels.QWEN3_MAX.id to DashscopeModels.QWEN3_MAX
    )

    fun getModel(id: String) = alibabaModelMap[id] ?: alibabaModelMap.values.first()
}