package knet

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface AiModelAdapter {
    val clientProvidesModelInPath: Boolean get() = false // Does the client (e.g. Gemini) put model in URL path?
    fun getCompletionsUrl(baseUrl: String, modelId: String): String
    fun buildHeaders(builder: HttpRequestBuilder, apiKey: String?)
    fun createRequestBody(modelId: String, messages: List<AiMessage>, stream: Boolean): Any

    // For standard (suspend) response
    suspend fun parseStandardResponse(response: HttpResponse, json: Json): AiResponse

    // For stream (Flow) response
    fun parseStreamChunk(
        chunkData: String,
        json: Json
    ): AiStreamChunk? // Null if it's a non-data line or e.g. [DONE]

    fun isStreamEndMarker(line: String): Boolean = line.equals("data: [DONE]", ignoreCase = true)
}

// --- Implementations ---

// OpenAiAdapter.kt (Works for OpenAI, LM Studio, and Ollama's OpenAI-compatible endpoint)
object OpenAiAdapter : AiModelAdapter {
    @Serializable
    data class OpenAiChatRequest(
        val model: String,
        val messages: List<AiMessage>,
        val stream: Boolean = false,
        val temperature: Float? = null, // Add other params as needed
        // val stream_options: StreamOptions? = null // For including usage in stream
    )
    // @Serializable
    // data class StreamOptions(val include_usage: Boolean = true)


    override fun getCompletionsUrl(baseUrl: String, modelId: String): String {
        // modelId is part of the request body for OpenAI, not URL
        return "$baseUrl/chat/completions"
    }

    override fun buildHeaders(builder: HttpRequestBuilder, apiKey: String?) {
        apiKey?.let { builder.headers.append("Authorization", "Bearer $it") }
    }

    override fun createRequestBody(
        modelId: String,
        messages: List<AiMessage>,
        stream: Boolean
    ): Any {
        return OpenAiChatRequest(
            model = modelId,
            messages = messages,
            stream = stream,
            // stream_options = if (stream) StreamOptions() else null
        )
    }

    override suspend fun parseStandardResponse(response: HttpResponse, json: Json): AiResponse {
        return json.decodeFromString(response.bodyAsText())
    }

    override fun parseStreamChunk(chunkData: String, json: Json): AiStreamChunk? {
        if (chunkData.isBlank() || isStreamEndMarker("data: $chunkData")) return null
        return try {
            json.decodeFromString<AiStreamChunk>(chunkData)
        } catch (e: Exception) {
            // Log error or handle; sometimes providers send malformed empty chunks
            // or other non-JSON data during stream that isn't [DONE]
            println("Warning: Could not parse stream chunk: $chunkData. Error: ${e.message}")
            null
        }
    }
}

// OllamaNativeAdapter.kt (For Ollama's native /api/chat endpoint)
object OllamaNativeAdapter : AiModelAdapter {
    @Serializable
    data class OllamaChatRequest(
        val model: String,
        val messages: List<AiMessage>,
        val stream: Boolean = false,
        val options: Map<String, String>? = null // e.g. "temperature": "0.7"
    )

    @Serializable
    data class OllamaStreamResponseChunk( // Ollama's stream chunk is different
        val model: String,
        val created_at: String,
        val message: AiMessage,
        val done: Boolean,
        val total_duration: Long? = null, // Present if done = true
        val load_duration: Long? = null,  // Present if done = true
        val prompt_eval_count: Int? = null, // Present if done = true
        val prompt_eval_duration: Long? = null, // Present if done = true
        val eval_count: Int? = null,      // Present if done = true
        val eval_duration: Long? = null   // Present if done = true
    )

    @Serializable
    data class OllamaStandardResponse( // Ollama's non-stream response
        val model: String,
        val created_at: String,
        val message: AiMessage,
        val done: Boolean,
        // ... other fields similar to the final stream chunk
        val total_duration: Long? = null,
        val load_duration: Long? = null,
        val prompt_eval_count: Int? = null,
        val prompt_eval_duration: Long? = null,
        val eval_count: Int? = null,
        val eval_duration: Long? = null
    )


    override fun getCompletionsUrl(baseUrl: String, modelId: String): String {
        // modelId is part of the request body
        return "$baseUrl/chat" // Native Ollama API endpoint
    }

    override fun buildHeaders(builder: HttpRequestBuilder, apiKey: String?) {
        // No API key typically for local Ollama
    }

    override fun createRequestBody(
        modelId: String,
        messages: List<AiMessage>,
        stream: Boolean
    ): Any {
        return OllamaChatRequest(model = modelId, messages = messages, stream = stream)
    }

    override suspend fun parseStandardResponse(response: HttpResponse, json: Json): AiResponse {
        val ollamaResponse = json.decodeFromString<OllamaStandardResponse>(response.bodyAsText())
        return AiResponse(
            choices = listOf(
                AiChoice(
                    message = ollamaResponse.message,
                    finish_reason = if (ollamaResponse.done) "stop" else null
                )
            ),
            usage = AiUsage(
                prompt_tokens = ollamaResponse.prompt_eval_count,
                completion_tokens = ollamaResponse.eval_count,
                total_tokens = (ollamaResponse.prompt_eval_count ?: 0) + (ollamaResponse.eval_count
                    ?: 0)
            )
        )
    }

    override fun parseStreamChunk(chunkData: String, json: Json): AiStreamChunk? {
        if (chunkData.isBlank()) return null
        return try {
            val ollamaChunk = json.decodeFromString<OllamaStreamResponseChunk>(chunkData)
            AiStreamChunk(
                choices = listOf(
                    AiChoice(
                        delta = AiMessage(
                            role = ollamaChunk.message.role,
                            content = ollamaChunk.message.content
                        ),
                        finish_reason = if (ollamaChunk.done) "stop" else null
                    )
                ),
                model = ollamaChunk.model,
                usage = if (ollamaChunk.done) AiUsage(
                    prompt_tokens = ollamaChunk.prompt_eval_count,
                    completion_tokens = ollamaChunk.eval_count,
                    total_tokens = (ollamaChunk.prompt_eval_count ?: 0) + (ollamaChunk.eval_count
                        ?: 0)
                ) else null
            )
        } catch (e: Exception) {
            println("Warning: Could not parse Ollama stream chunk: $chunkData. Error: ${e.message}")
            null
        }
    }

    // Ollama doesn't use "data: [DONE]", it signals 'done' in the JSON.
    // The SSE line itself is the JSON, not prefixed by "data: " sometimes, or if it is,
    // the content is still a full JSON object.
    override fun isStreamEndMarker(line: String): Boolean = false
}

// GeminiAdapter.kt
object GeminiAdapter : AiModelAdapter {
    @Serializable
    data class GeminiRequest(
        val contents: List<GeminiContent>,
        val generationConfig: GeminiGenerationConfig? = null
    )

    @Serializable
    data class GeminiContent(
        val parts: List<GeminiPart>,
        val role: String? = null
    ) // Role optional for user, required for model

    @Serializable
    data class GeminiPart(val text: String)

    @Serializable
    data class GeminiGenerationConfig(
        val temperature: Float? = null,
        val maxOutputTokens: Int? = null
    )

    @Serializable
    data class GeminiResponse(
        val candidates: List<GeminiCandidate>? = null,
        val promptFeedback: GeminiPromptFeedback? = null,
        // Error structure for Gemini is different, might be directly in response or via HTTP status
        val error: GeminiError? = null // Custom error field if API provides it in JSON
    )

    @Serializable
    data class GeminiCandidate(
        val content: GeminiContent,
        val finishReason: String? = null,
        val index: Int = 0
    )

    @Serializable
    data class GeminiPromptFeedback(
        val blockReason: String? = null,
        val safetyRatings: List<GeminiSafetyRating> = emptyList()
    )

    @Serializable
    data class GeminiSafetyRating(val category: String, val probability: String)

    @Serializable
    data class GeminiError(
        val code: Int,
        val message: String,
        val status: String
    ) // Example, match actual API

    override val clientProvidesModelInPath: Boolean = true

    override fun getCompletionsUrl(baseUrl: String, modelId: String): String {
        // For streaming: :streamGenerateContent, for non-streaming: :generateContent
        // We will differentiate this in the actual call based on the `stream` parameter.
        // The AiClient will append the correct action.
        return "$baseUrl/$modelId"
    }

    override fun buildHeaders(builder: HttpRequestBuilder, apiKey: String?) {
        // Gemini API key is usually passed as a query parameter `key`
        // Handled in AiClient's request building phase for Gemini.
        // No specific headers here unless required.
    }

    override fun createRequestBody(
        modelId: String,
        messages: List<AiMessage>,
        stream: Boolean
    ): Any {
        val geminiContents = messages.map {
            GeminiContent(
                parts = listOf(GeminiPart(it.content)),
                // Gemini expects "user" and "model" roles. System prompt handled differently.
                role = if (it.role == "assistant") "model" else "user"
            )
        }
        return GeminiRequest(contents = geminiContents)
    }

    private fun mapGeminiResponseToAiResponse(
        geminiResponse: GeminiResponse,
        modelId: String
    ): AiResponse {
        if (geminiResponse.error != null) {
            return AiResponse(
                choices = emptyList(),
                error = AiError(
                    message = geminiResponse.error.message,
                    code = geminiResponse.error.code.toString(),
                    type = geminiResponse.error.status
                )
            )
        }
        val choices = geminiResponse.candidates?.map { candidate ->
            AiChoice(
                message = AiMessage(
                    role = candidate.content.role
                        ?: "assistant", // Default to assistant if role not in part
                    content = candidate.content.parts.joinToString("") { it.text }
                ),
                finish_reason = candidate.finishReason,
                index = candidate.index
            )
        } ?: emptyList()
        return AiResponse(choices = choices)
    }

    override suspend fun parseStandardResponse(response: HttpResponse, json: Json): AiResponse {
        val geminiResponse = json.decodeFromString<GeminiResponse>(response.bodyAsText())
        return mapGeminiResponseToAiResponse(
            geminiResponse,
            response.request.url.pathSegments.last { it.isNotBlank() }.substringBefore(':')
        )
    }

    override fun parseStreamChunk(chunkData: String, json: Json): AiStreamChunk? {
        if (chunkData.isBlank()) return null
        return try {
            // Gemini streams an array of responses, usually with one item.
            // And they are not prefixed with "data: " but come as a raw JSON array line by line.
            // This needs custom SSE line processing in AiClient for Gemini or adapter specific handling.
            // For now, assuming chunkData is a single GeminiResponse object from the array.
            val geminiResponse = json.decodeFromString<GeminiResponse>(chunkData)
            val choice = geminiResponse.candidates?.firstOrNull()
            if (choice != null) {
                AiStreamChunk(
                    choices = listOf(
                        AiChoice(
                            delta = AiMessage(
                                role = choice.content.role ?: "assistant",
                                content = choice.content.parts.joinToString("") { it.text }
                            ),
                            finish_reason = choice.finishReason,
                            index = choice.index
                        )
                    ),
                    // model = modelId (can't get it here easily, maybe from request or first chunk)
                    // id = some unique id if provided by gemini
                )
            } else if (geminiResponse.error != null) {
                AiStreamChunk(
                    error = AiError(
                        geminiResponse.error.message,
                        code = geminiResponse.error.code.toString(),
                        type = geminiResponse.error.status
                    )
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("Warning: Could not parse Gemini stream chunk: $chunkData. Error: ${e.message}")
            null
        }
    }

    // Gemini stream doesn't use "data: [DONE]". It just stops, or the last chunk has a finishReason.
    override fun isStreamEndMarker(line: String): Boolean = false
}
