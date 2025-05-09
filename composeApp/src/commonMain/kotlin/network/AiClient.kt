package network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json

class AiClient(
    engineFactory: HttpClientEngineFactory<*> = httpClientEngineFactory(),
    private val baseUrl: String,
    private val modelId: String,
    private val adapter: AiModelAdapter,
    private val apiKey: String? = null,
    private val requestTimeoutMillis: Long = 60_000, // 60 seconds
    private val socketTimeoutMillis: Long = 600_000 // 10 minutes for long streams
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false // Usually false for network efficiency
        encodeDefaults = true
        classDiscriminator = "_type" // If you use polymorphic serialization
    }

    private val httpClient = HttpClient(engineFactory) {
        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = this@AiClient.requestTimeoutMillis
            socketTimeoutMillis = this@AiClient.socketTimeoutMillis // For reading stream
            connectTimeoutMillis = this@AiClient.requestTimeoutMillis
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            // Common headers can go here, but API key is adapter-specific
        }
    }

    private fun getFullUrl(stream: Boolean = false): String {
        var url = adapter.getCompletionsUrl(baseUrl, modelId)
        if (adapter.clientProvidesModelInPath && adapter is GeminiAdapter) { // Gemini specific URL mods
            url = if (stream) "$url:streamGenerateContent" else "$url:generateContent"
            apiKey?.let { url += "?key=$it" } // Gemini API key as query param
        }
        return url
    }

    suspend fun generateResponse(
        messages: List<AiMessage>,
        customModelId: String? = null // Override default modelId for this call
    ): AiResponse {
        val currentModelId = customModelId ?: modelId
        val requestBody = adapter.createRequestBody(currentModelId, messages, stream = false)

        try {
            val response: HttpResponse = httpClient.post(getFullUrl(stream = false)) {
                adapter.buildHeaders(this, apiKey)
                setBody(requestBody)
                if (adapter is GeminiAdapter && apiKey != null) {
                    // Gemini API key is in URL, ensure no auth header if not needed
                } else if (adapter !is GeminiAdapter) {
                    apiKey?.let { header("Authorization", "Bearer $it") }
                }
            }

            if (response.status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()
                val errorMessage =
                    "AI API Error: ${response.status.value} ${response.status.description}. Body: $errorBody"
                val parsedError = try {
                    json.decodeFromString<AiError>(errorBody)
                } catch (e: Exception) {
                    AiError(errorMessage)
                }
                return AiResponse(
                    choices = emptyList(),
                    error = parsedError
                ) // Return AiResponse with error
            }

            return adapter.parseStandardResponse(response, json)

        } catch (e: Exception) {
            throw AiClientException("Failed to generate AI response: ${e.message}", cause = e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun generateResponseStream(
        messages: List<AiMessage>,
        customModelId: String? = null // Override default modelId for this call
    ): Flow<AiStreamChunk> = channelFlow { // Use channelFlow for more control over SSE parsing
        val currentModelId = customModelId ?: modelId
        val requestBody = adapter.createRequestBody(currentModelId, messages, stream = true)
        var lastChunkWasTerminal = false

        try {
            httpClient.preparePost(getFullUrl(stream = true)) {
                adapter.buildHeaders(this, apiKey)
                setBody(requestBody)
                if (adapter is GeminiAdapter && apiKey != null) {
                    // Gemini API key is in URL
                } else if (adapter !is GeminiAdapter) {
                    apiKey?.let { header("Authorization", "Bearer $it") }
                }
            }.execute { response ->
                if (response.status != HttpStatusCode.OK) {
                    val errorBody = response.bodyAsText()
                    val errorMessage =
                        "AI API Stream Error: ${response.status.value} ${response.status.description}. Body: $errorBody"
                    val parsedError = try {
                        json.decodeFromString<AiError>(errorBody)
                    } catch (e: Exception) {
                        AiError(errorMessage)
                    }
                    send(AiStreamChunk(error = parsedError))
                    lastChunkWasTerminal = true
                    close() // Close the channel
                    return@execute
                }

                val channel: ByteReadChannel = response.bodyAsChannel()
                var buffer = "" // For multi-line data events (rare for AI but good for general SSE)

                while (!channel.isClosedForRead && !lastChunkWasTerminal) {
                    val line = channel.readUTF8Line() ?: break // End of stream

                    if (line.isBlank()) { // Event boundary
                        if (buffer.isNotBlank()) {
                            adapter.parseStreamChunk(buffer, json)?.let {
                                send(it)
                                if (it.isLastChunk || it.error != null) lastChunkWasTerminal = true
                            }
                            buffer = ""
                        }
                        continue
                    }

                    // Handle different SSE line types
                    // For most AI APIs, it's just "data: <json>"
                    // Ollama native might send JSON directly. Gemini sends JSON array lines.
                    if (adapter.isStreamEndMarker(line)) {
                        lastChunkWasTerminal = true
                        break // OpenAI specific [DONE]
                    }

                    if (line.startsWith("data:")) {
                        buffer += line.substringAfter("data:").trim()
                        // Most AI APIs send one JSON object per "data:" line, so we can process immediately
                        // If an API could send partial JSON across multiple "data:" lines before a blank line,
                        // then parsing should only happen on blank line. For simplicity, assume one JSON per data line.
                        if (buffer.isNotBlank()) {
                            adapter.parseStreamChunk(buffer, json)?.let {
                                send(it)
                                if (it.isLastChunk || it.error != null) lastChunkWasTerminal = true
                            }
                            buffer = "" // Reset buffer after processing
                        }
                    } else if (adapter is OllamaNativeAdapter || adapter is GeminiAdapter) {
                        // Ollama native and Gemini might send JSON directly without "data:" prefix
                        // Gemini sends an array `[ { ... } ]`, then `[ { ... } ]`
                        // We need to handle potentially incomplete JSON lines if they span reads,
                        // but readUTF8Line usually gives complete lines.
                        val chunkDataToParse =
                            if (adapter is GeminiAdapter && line.startsWith("[")) {
                                // Gemini streams an array of responses, usually with one item.
                                // Extract the object from the array string.
                                line.trim().removePrefix("[").removeSuffix("]").trim()
                            } else {
                                line.trim()
                            }

                        if (chunkDataToParse.isNotBlank()) {
                            adapter.parseStreamChunk(chunkDataToParse, json)?.let {
                                send(it)
                                if (it.isLastChunk || it.error != null) lastChunkWasTerminal = true
                            }
                        }
                    } else {
                        // Accumulate if it's not a recognized prefix and not a blank line (could be part of multi-line data)
                        // Or log unknown line: println("SSE: Unknown line: $line")
                    }
                }
            }
        } catch (e: Exception) {
            send(AiStreamChunk(error = AiError("Stream processing failed: ${e.message}")))
            lastChunkWasTerminal = true
            throw AiClientException(
                "Failed to generate AI stream response: ${e.message}",
                cause = e
            )
        } finally {
            if (!lastChunkWasTerminal) {
                // Ensure a final chunk indicating completion if not already sent.
                // Or, rely on flow completion.
                // This part might be tricky if the stream ends abruptly without a 'done' signal.
            }
        }
    }


    fun close() {
        httpClient.close()
    }
}
