package knet.ai.providers

import ai.koog.prompt.llm.OllamaModels

object Ollama {
    val modelOptions = mapOf(
        OllamaModels.Meta.LLAMA_3_2_3B.id to OllamaModels.Meta.LLAMA_3_2_3B,
        OllamaModels.Meta.LLAMA_3_2.id to OllamaModels.Meta.LLAMA_3_2,
        OllamaModels.Meta.LLAMA_4_SCOUT.id to OllamaModels.Meta.LLAMA_4_SCOUT,
        OllamaModels.Meta.LLAMA_GUARD_3.id to OllamaModels.Meta.LLAMA_GUARD_3,
        OllamaModels.Alibaba.QWEN_2_5_05B.id to OllamaModels.Alibaba.QWEN_2_5_05B,
        OllamaModels.Alibaba.QWEN_3_06B.id to OllamaModels.Alibaba.QWEN_3_06B,
        OllamaModels.Alibaba.QWQ_32B.id to OllamaModels.Alibaba.QWQ_32B,
        OllamaModels.Alibaba.QWEN_CODER_2_5_32B.id to OllamaModels.Alibaba.QWEN_CODER_2_5_32B,
        OllamaModels.Granite.GRANITE_3_2_VISION.id to OllamaModels.Granite.GRANITE_3_2_VISION
    )

    fun getModel(id: String) = modelOptions[id] ?: modelOptions.values.first()
}