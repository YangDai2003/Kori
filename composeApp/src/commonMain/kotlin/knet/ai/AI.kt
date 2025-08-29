package knet.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.deepseek.DeepSeekClientSettings
import ai.koog.prompt.executor.clients.deepseek.DeepSeekLLMClient
import ai.koog.prompt.executor.clients.google.GoogleClientSettings
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import knet.ai.providers.Anthropic
import knet.ai.providers.DeepSeek
import knet.ai.providers.Gemini
import knet.ai.providers.LMStudio
import knet.ai.providers.Ollama
import knet.ai.providers.OpenAI
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class TestConnectionResult(
    val success: Boolean = false,
    val message: String = ""
)

@Serializable
data class ModelsResponse(val data: List<Model>)

@Serializable
data class Model(val id: String)

object AI {
    val providers = mapOf(
        LLMProvider.Google.id to LLMProvider.Google,
        LLMProvider.OpenAI.id to LLMProvider.OpenAI,
        LLMProvider.Anthropic.id to LLMProvider.Anthropic,
        LLMProvider.DeepSeek.id to LLMProvider.DeepSeek,
        LLMProvider.Ollama.id to LLMProvider.Ollama,
        LMStudio.id to LMStudio
    )

    fun getAIAgent(
        lLMProvider: LLMProvider,
        model: String,
        apiKey: String = "",
        baseUrl: String = "",
        systemPrompt: String = ""
    ): AIAgent<String, String> {
        val (client, llmModel) = when (lLMProvider) {
            LLMProvider.Google -> {
                val settings =
                    if (baseUrl.isNotBlank()) GoogleClientSettings(baseUrl = baseUrl) else GoogleClientSettings()
                GoogleLLMClient(apiKey, settings) to Gemini.getModel(model)
            }

            LLMProvider.OpenAI -> {
                val settings =
                    if (baseUrl.isNotBlank()) OpenAIClientSettings(baseUrl = baseUrl) else OpenAIClientSettings()
                OpenAILLMClient(apiKey, settings) to OpenAI.getModel(model)
            }

            LLMProvider.Anthropic -> {
                val settings =
                    if (baseUrl.isNotBlank()) AnthropicClientSettings(baseUrl = baseUrl) else AnthropicClientSettings()
                AnthropicLLMClient(apiKey, settings) to Anthropic.getModel(model)
            }

            LLMProvider.DeepSeek -> {
                val settings =
                    if (baseUrl.isNotBlank()) DeepSeekClientSettings(baseUrl = baseUrl) else DeepSeekClientSettings()
                DeepSeekLLMClient(apiKey, settings) to DeepSeek.getModel(model)
            }

            LLMProvider.Ollama -> {
                val client =
                    if (baseUrl.isNotBlank()) OllamaClient(baseUrl = baseUrl) else OllamaClient()
                client to Ollama.getModel(model)
            }

            LMStudio -> {
                val settings =
                    if (baseUrl.isNotBlank()) OpenAIClientSettings(baseUrl = baseUrl) else OpenAIClientSettings()
                val llModel = LLModel(
                    provider = LLMProvider.OpenAI,
                    id = model,
                    capabilities = listOf(
                        LLMCapability.Schema.JSON.Basic,
                        LLMCapability.Temperature
                    ),
                    contextLength = 4096L
                )
                OpenAILLMClient(apiKey, settings) to llModel
            }

            else -> {
                throw IllegalArgumentException("Unsupported LLM provider: $lLMProvider")
            }
        }
        return AIAgent(
            executor = SingleLLMPromptExecutor(client),
            llmModel = llmModel,
            systemPrompt = systemPrompt
        )
    }

    suspend fun testConnection(
        lLMProvider: LLMProvider,
        model: String,
        apiKey: String = "",
        baseUrl: String = ""
    ): TestConnectionResult {
        return try {
            val agent = getAIAgent(lLMProvider, model, apiKey, baseUrl)
            val result = agent.run("Hi")
            TestConnectionResult(success = true, message = result)
        } catch (e: Exception) {
            println("Error testing connection: ${e.message}")
            TestConnectionResult(
                success = false,
                message = e.message ?: "An unknown error occurred :("
            )
        }
    }

    suspend fun getAvailableModels(baseUrl: String): List<String> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        try {
            val response: ModelsResponse = client.get("$baseUrl/v1/models").body()
            return response.data.map { it.id }
        } catch (e: Exception) {
            println("Error fetching models: ${e.message}")
            return emptyList()
        } finally {
            client.close()
        }
    }
}