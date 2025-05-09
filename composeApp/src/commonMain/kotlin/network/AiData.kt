package network

import kotlinx.serialization.Serializable

@Serializable
data class AiMessage(
    val role: String, // "user", "assistant", "system"
    val content: String
)

// For standard suspend fun response
@Serializable
data class AiResponse(
    val choices: List<AiChoice>,
    val usage: AiUsage? = null, // Optional: token usage info
    val error: AiError? = null
) {
    val firstMessageContent: String?
        get() = choices.firstOrNull()?.message?.content

    val firstDeltaContent: String? // For some models that might use this structure even for non-streaming
        get() = choices.firstOrNull()?.delta?.content
}

@Serializable
data class AiChoice(
    val message: AiMessage? = null, // For full messages
    val delta: AiMessage? = null,   // For streamed deltas
    val index: Int = 0,
    val finish_reason: String? = null
)

@Serializable
data class AiUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null
)

@Serializable
data class AiError(
    val message: String,
    val type: String? = null,
    val param: String? = null,
    val code: String? = null
)

// For Flow<AiStreamChunk> response
@Serializable
data class AiStreamChunk(
    val choices: List<AiChoice> = emptyList(), // Usually one choice with a delta
    val usage: AiUsage? = null, // OpenAI includes this in the last chunk if stream_options are set
    val error: AiError? = null,
    val model: String? = null, // Some stream responses include the model
    val id: String? = null // Stream chunk ID
) {
    val deltaContent: String?
        get() = choices.firstOrNull()?.delta?.content

    val isLastChunk: Boolean
        get() = choices.firstOrNull()?.finish_reason != null
}

// --- Exception ---
class AiClientException(message: String, val statusCode: Int? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)