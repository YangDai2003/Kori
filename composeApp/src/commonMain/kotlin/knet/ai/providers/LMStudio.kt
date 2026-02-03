package knet.ai.providers

import ai.koog.prompt.llm.LLMProvider
import androidx.compose.runtime.Stable

@Stable
data object LMStudio : LLMProvider("lms", "LM Studio")