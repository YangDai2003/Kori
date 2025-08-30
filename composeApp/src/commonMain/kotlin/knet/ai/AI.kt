package knet.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.LLMClient
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
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import knet.ai.providers.Anthropic
import knet.ai.providers.DeepSeek
import knet.ai.providers.Gemini
import knet.ai.providers.LMStudio
import knet.ai.providers.Ollama
import knet.ai.providers.OpenAI
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class GenerationResult {
    @Serializable
    data class Success(val text: String) : GenerationResult()

    @Serializable
    data class Error(val errorMessage: String) : GenerationResult()
}

@Serializable
data class ModelsResponse(val data: List<Model>)

@Serializable
data class Model(val id: String)

object AI {
    object SystemPrompt {
        const val SYSTEM = """
            You are a helpful assistant for a note-taking application.
            Your primary role is to generate content that will be directly inserted into a user's note.
            Therefore, your responses should be concise, well-structured, and formatted as if they are part of a larger document.
            Avoid conversational language, introductions, or summaries unless explicitly asked for.
            Generate only the requested content in a direct, written style suitable for notes.
            Unless the user specifies a different language, you must respond in the same language as the user's prompt.
        """
        const val MARKDOWN = """
            Your response must be formatted in Markdown.
            You should use standard Markdown syntax for formatting text, such as:
            - Headings (#, ##, ###, ####, #####, ######)
            - Bold (**text**) and Italic (_text_)
            - Unordered lists (using -, *, or +)
            - Ordered lists (e.g., 1., 2., 3.)
            - Blockquotes (> text)
            - Strikethrough (~~text~~)
            - Task lists (- [ ] text, - [x] text)
            - Code blocks (```language\ncode\n```) and inline code (`code`)
            - Links ([title](https://www.example.com))
            - Images (![alt text](https://www.example.com/image.png))
            - Horizontal rules (--- or *** or ___)
            - HTML tags
            
            For mathematical formulas, you must use LaTeX syntax:
            - For inline formulas, enclose the expression in single dollar signs.
            - For formula blocks, enclose the expression in double dollar signs. For example:
            $$
            \int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
            $$
            
            For diagrams, you must use Mermaid syntax and enclose the entire Mermaid code block within a `<pre class="mermaid"></pre>` tag. For example:
            <pre class="mermaid">
            graph TD;
                A-->B;
                A-->C;
                B-->D;
                C-->D;
            </pre>
            
            Ensure your entire output strictly adheres to these formatting rules.
        """
        const val PLAIN_TEXT = """
            Your response must be in plain text only. Do not use any Markdown or HTML syntax for formatting. 
        """
        const val TODO_TXT = """
            Your response must strictly follow the todo.txt format rules.
            
            Each line in your output must be a single task. A task can have the following components in this specific order:
            1.  An optional completion marker 'x ' at the beginning if the task is done.
            2.  An optional priority level, which is an uppercase letter in parentheses, e.g., `(A)`. This must be followed by a space.
            3.  An optional completion date (YYYY-MM-DD) followed by an optional creation date (YYYY-MM-DD). The completion date is only present if the task is marked as complete ('x ').
            4.  The task description.
            5.  Optional context tags, prefixed with `@`, e.g., `@email`.
            6.  Optional project tags, prefixed with `+`, e.g., `+project-name`.
            
            Example of a valid todo.txt line:
            (A) 2025-12-31 Finalize the project report +knet-ai @work
            
            Another example of a completed task:
            x 2025-08-28 2025-08-26 Call the client to confirm meeting details @calls
            
            Do not add any explanations, introductory text, or formatting beyond what is specified in the todo.txt format. Your entire output must consist of one or more lines formatted correctly according to these rules.
        """
    }

    object EventPrompt {
        const val REWRITE = """
            Your task is to rewrite the following text provided by the user.
            You must adhere to the following instructions:
            1. Preserve the original meaning and core information.
            2. Improve clarity, conciseness, and readability.
            3. Correct any grammatical errors, spelling mistakes, and awkward phrasing.
            4. Maintain a consistent tone and style.
            5. Do not introduce new information or your personal opinions.
            6. The rewritten text must be a direct replacement for the original.
            7. Your output must contain only the rewritten text, without any introductory or concluding phrases like "Here is the rewritten text:".
            
            Now, rewrite the following text:
        """

        const val SUMMARIZE = """
            Your task is to create a concise summary of the following text provided by the user.
            You must adhere to the following instructions:
            1. Identify and extract the main ideas and key points.
            2. The summary must be significantly shorter than the original text but retain its core message.
            3. Do not include minor details, your personal opinions, or information not present in the original text.
            4. The summary must be written in clear and coherent prose.
            5. Your output must contain only the summary, without any introductory or concluding phrases like "Here is the summary:".
            
            Now, summarize the following text:
        """
    }

    val providers = mapOf(
        LLMProvider.Google.id to LLMProvider.Google,
        LLMProvider.OpenAI.id to LLMProvider.OpenAI,
        LLMProvider.Anthropic.id to LLMProvider.Anthropic,
        LLMProvider.DeepSeek.id to LLMProvider.DeepSeek,
        LLMProvider.Ollama.id to LLMProvider.Ollama,
        LMStudio.id to LMStudio
    )

    private fun getClientAndModel(
        lLMProvider: LLMProvider,
        model: String,
        apiKey: String = "",
        baseUrl: String = ""
    ): Pair<LLMClient, LLModel> = when (lLMProvider) {
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

    suspend fun executePrompt(
        lLMProvider: LLMProvider,
        model: String,
        userInput: String,
        apiKey: String = "",
        baseUrl: String = "",
        systemPrompt: String = ""
    ): GenerationResult {
        return try {
            val systemPrompt = SystemPrompt.SYSTEM + "\n" + systemPrompt
            val (client, llmModel) = getClientAndModel(lLMProvider, model, apiKey, baseUrl)
            val agent =
                AIAgent(SingleLLMPromptExecutor(client), llmModel, systemPrompt = systemPrompt)
            val result = agent.run(userInput)
            GenerationResult.Success(result)
        } catch (e: Exception) {
            println("Error generating text: ${e.stackTraceToString()}")
            GenerationResult.Error(
                e.message ?: "An unknown error occurred while generating text :("
            )
        }
    }

    suspend fun testConnection(
        lLMProvider: LLMProvider,
        model: String,
        apiKey: String = "",
        baseUrl: String = ""
    ): GenerationResult {
        return try {
            val (client, llmModel) = getClientAndModel(lLMProvider, model, apiKey, baseUrl)
            val agent = AIAgent(SingleLLMPromptExecutor(client), llmModel)
            val result = agent.run("Hi")
            GenerationResult.Success(result)
        } catch (e: Exception) {
            println("Error testing connection: ${e.stackTraceToString()}")
            GenerationResult.Error(e.message ?: "An unknown error occurred :(")
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