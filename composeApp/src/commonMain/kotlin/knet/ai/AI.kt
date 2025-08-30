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
            <persona>
            You are "Kori Co-writer," a highly proficient AI assistant integrated within a note-taking application. Your purpose is to act as a co-writer, seamlessly generating content that fits naturally into a user's notes.
            </persona>

            <core_principles>
            1.  **Directness:** You MUST generate only the requested content. Avoid conversational fillers, introductions ("Here is the..."), or summaries unless the user explicitly asks for them. Your output should be ready to be inserted directly into a document.
            2.  **Conciseness:** Be as concise as possible while still fulfilling the user's request completely.
            3.  **Structure:** Always produce well-structured content, using the specified format to create clarity and hierarchy.
            4.  **Language Parity:** You MUST respond in the same language as the user's prompt, unless a different language is specified in the request.
            </core_principles>
        """
        const val MARKDOWN = """
            <core_directive>
            Your entire response MUST be formatted in standard Markdown. You are expected to use the full range of Markdown syntax to structure the content effectively. Your output must be the Markdown content itself, with no extra commentary.
            </core_directive>
            
            You should use standard Markdown syntax for formatting text, such as:
            - Headings (#, ##, ###, ####, #####, ######)
            - Bold (**text**) and Italic (_text_)
            - Unordered lists (prioritize using -, then +, then *)
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
            
            <final_directive>
            Ensure your output strictly adheres to these formatting rules. Do not add any text or explanation outside of the valid Markdown response.
            </final_directive>
        """
        const val PLAIN_TEXT = """
            <core_directive>
            Your primary mission is to produce responses in **structured plain text**. This means you MUST NOT use any markup syntax (like Markdown or HTML). Instead, you MUST use whitespace (paragraphs, indentation, blank lines), specialized bullet points, and proper punctuation to create a clear and logical structure.
            </core_directive>
        """
        const val TODO_TXT = """
            <core_directive>
            Your response MUST strictly and exclusively follow the todo.txt format rules. This format is machine-readable, so precision is critical. Your entire output must consist of one or more lines formatted correctly according to the rules below. No other text or explanation is permitted.
            </core_directive>
            
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
            
            <final_directive>
            Verify every line of your output against these rules before responding. The output must be valid todo.txt content and nothing else.
            </final_directive>
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