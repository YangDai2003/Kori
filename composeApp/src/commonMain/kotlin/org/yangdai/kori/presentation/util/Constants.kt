package org.yangdai.kori.presentation.util

import ai.koog.prompt.llm.LLMProvider
import knet.ai.providers.LMStudio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.yangdai.kori.domain.repository.DataStoreRepository

object Constants {
    const val DEEP_LINK = "kori://screen"

    object Preferences {
        const val FOLDER_SORT_TYPE = "folder_sort_type"
        const val NOTE_SORT_TYPE = "note_sort_type"
        const val SEARCH_HISTORY = "search_history"

        const val APP_THEME = "app_theme"
        const val APP_COLOR = "app_color"
        const val IS_APP_IN_AMOLED_MODE = "is_app_in_amoled_mode"
        const val FONT_SIZE = "font_size"

        const val IS_SCREEN_PROTECTED = "is_screen_protected"
        const val PASSWORD = "password"
        const val IS_CREATING_PASSWORD = "is_creating_password"
        const val IS_BIOMETRIC_ENABLED = "is_biometric_enabled"
        const val KEEP_SCREEN_ON = "keep_screen_on"

        const val SHOW_LINE_NUMBER = "show_line_number"
        const val IS_LINTING_ENABLED = "is_markdown_lint_enabled"
        const val IS_DEFAULT_READING_VIEW = "is_default_reading_view"
        const val EDITOR_WEIGHT = "default_editor_weight"

        const val DATE_FORMATTER = "date_formatter"
        const val TIME_FORMATTER = "time_formatter"

        const val CARD_SIZE = "card_size"
        const val CLIP_OVERFLOW_TEXT = "clip_overflow_text"

        const val IS_AI_ENABLED = "is_ai_enabled"
        const val AI_PROVIDER = "ai_provider"
    }

    object LLMConfig {
        const val GEMINI_API_KEY = "gemini_api_key"
        const val GEMINI_BASE_URL = "gemini_base_url"
        const val GEMINI_MODEL = "gemini_model"

        const val OPENAI_API_KEY = "openai_api_key"
        const val OPENAI_BASE_URL = "openai_base_url"
        const val OPENAI_MODEL = "openai_model"

        const val ANTHROPIC_API_KEY = "anthropic_api_key"
        const val ANTHROPIC_BASE_URL = "anthropic_base_url"
        const val ANTHROPIC_MODEL = "anthropic_model"

        const val DEEPSEEK_API_KEY = "deepseek_api_key"
        const val DEEPSEEK_BASE_URL = "deepseek_base_url"
        const val DEEPSEEK_MODEL = "deepseek_model"

        const val OLLAMA_BASE_URL = "ollama_base_url"
        const val OLLAMA_MODEL = "ollama_model"

        const val LM_STUDIO_BASE_URL = "lm_studio_base_url"
        const val LM_STUDIO_MODEL = "lm_studio_model"

        const val ALIBABA_API_KEY = "alibaba_api_key"
        const val ALIBABA_BASE_URL = "alibaba_base_url"
        const val ALIBABA_MODEL = "alibaba_model"

        const val MISTRAL_API_KEY = "mistral_api_key"
        const val MISTRAL_BASE_URL = "mistral_base_url"
        const val MISTRAL_MODEL = "mistral_model"

        // LLM Config: Base URL, Model, API Key
        suspend fun getLLMConfig(
            llmProvider: LLMProvider,
            dataStoreRepository: DataStoreRepository
        ): Triple<String, String, String> {
            return withContext(Dispatchers.IO) {
                when (llmProvider) {
                    LLMProvider.Google -> {
                        val baseUrl = dataStoreRepository.getString(GEMINI_BASE_URL)
                        val model = dataStoreRepository.getString(GEMINI_MODEL)
                        val apiKey = dataStoreRepository.getString(GEMINI_API_KEY)
                        Triple(baseUrl, model, apiKey)
                    }

                    LLMProvider.OpenAI -> {
                        val baseUrl = dataStoreRepository.getString(OPENAI_BASE_URL)
                        val model = dataStoreRepository.getString(OPENAI_MODEL)
                        val apiKey = dataStoreRepository.getString(OPENAI_API_KEY)
                        Triple(baseUrl, model, apiKey)
                    }

                    LLMProvider.Anthropic -> {
                        val baseUrl = dataStoreRepository.getString(ANTHROPIC_BASE_URL)
                        val model = dataStoreRepository.getString(ANTHROPIC_MODEL)
                        val apiKey = dataStoreRepository.getString(ANTHROPIC_API_KEY)
                        Triple(baseUrl, model, apiKey)
                    }

                    LLMProvider.DeepSeek -> {
                        val baseUrl = dataStoreRepository.getString(DEEPSEEK_BASE_URL)
                        val model = dataStoreRepository.getString(DEEPSEEK_MODEL)
                        val apiKey = dataStoreRepository.getString(DEEPSEEK_API_KEY)
                        Triple(baseUrl, model, apiKey)
                    }

                    LLMProvider.Alibaba -> {
                        val baseUrl = dataStoreRepository.getString(ALIBABA_BASE_URL)
                        val model = dataStoreRepository.getString(ALIBABA_MODEL)
                        val apiKey = dataStoreRepository.getString(ALIBABA_API_KEY)
                        Triple(baseUrl, model, apiKey)
                    }

                    LLMProvider.MistralAI -> {
                        val baseUrl = dataStoreRepository.getString(MISTRAL_BASE_URL)
                        val model = dataStoreRepository.getString(MISTRAL_MODEL)
                        val apiKey = dataStoreRepository.getString(MISTRAL_API_KEY)
                        Triple(baseUrl, model, apiKey)
                    }

                    LLMProvider.Ollama -> {
                        val baseUrl = dataStoreRepository.getString(OLLAMA_BASE_URL)
                        val model = dataStoreRepository.getString(OLLAMA_MODEL)
                        Triple(baseUrl, model, "")
                    }

                    LMStudio -> {
                        val baseUrl = dataStoreRepository.getString(LM_STUDIO_BASE_URL)
                        val model = dataStoreRepository.getString(LM_STUDIO_MODEL)
                        Triple(baseUrl, model, "")
                    }

                    else -> {
                        Triple("", "", "")
                    }
                }
            }
        }
    }
}

val SampleTodoNote = """
    2025-01-01 Document +TodoTxt task format
    (A) 2025-06-01 Call Mom @Phone +Family
    (A) Schedule annual checkup +Health
    (B) Outline chapter 5 +Novel @Computer
    (C) Add cover sheets @Office +TPSReports
    Plan backyard herb garden @Home
    Pick up phone @MediaMarkt
    Research self-publishing services +Novel @Computer
    x Download Kori mobile and desktop app @Phone
    x 2025-03-02 2025-03-01 Review Tim's pull request +TodoTxtTouch @github
""".trimIndent()

val SampleMarkdownNote = $$"""
    Markdown Syntax Guide
    ===
    
    - [Headings](#headings)  
    - [Text Formatting](#text-formatting)  
    - [Lists](#lists)  
    - [Links](#links)  
    - [Images](#images)  
    - [Blockquotes](#blockquotes)  
    - [Code](#code)  
    - [Math Expressions](#math-expressions)  
    - [Diagrams with Mermaid](#diagrams-with-mermaid)  
    - [Tables](#tables)  
    - [Horizontal Rule](#horizontal-rule)  
    - [HTML in Markdown](#html-in-markdown)

    ## Headings

    ```
    # Heading 1
    ## Heading 2
    ### Heading 3
    #### Heading 4
    ##### Heading 5
    ###### Heading 6
    ```

    ## Basic Text Formatting

    **Bold** or __bold__
    
    _Italic_ or *italic*
    
    ***Bold and Italic*** or ___Bold and Italic___
    
    ~~Strikethrough~~ & <ins>Underline</ins> & <mark>Highlight</mark>

    ## Lists

    ### Unordered Lists

    * Item 1
    * Item 2
        + Nested item 2.1
        + Nested item 2.2
            - Deeply nested item
    * Item 3

    ### Ordered Lists

    1. First item
    2. Second item
        1. Nested numbered item
        2. Another nested item
    3. Third item

    ### Task Lists

    - [x] Completed task
    - [ ] Incomplete task
    - [ ] Another task
        - [x] Nested completed subtask
        - [ ] Nested incomplete subtask
    - [x] One more completed task

    ## Links
    
    [Kori](https://github.com/YangDai2003/Kori)
    
    <https://play.google.com/store/apps/details?id=org.yangdai.kori>
    
    ## Images

    ![Kotlin](https://kotlinlang.org/docs/images/mascot-in-action.png)

    ## Blockquotes

    > This is a blockquote
    >
    > It can span multiple lines
    >
    > > Nested blockquotes are also possible
    
    ### Github Alerts
    
    > [!NOTE]
    > This is a note block.
    
    > [!TIP]
    > This is a tip block.
    
    > [!WARNING]
    > This is a warning block.
    
    > [!IMPORTANT]
    > This is an important block.
    
    > [!CAUTION]
    > This is a caution block.

    ## Code

    Inline `code` with backticks

    ```c
    #include <stdio.h>

    int main() {
        printf("Hello, world!\n");
        return 0;
    }
    ```

    ## Math Expressions

    ### Inline Math

    Einstein's equation: $E = mc^2$

    The quadratic formula: $x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}$

    ### Math Blocks

    $$
    X = \begin{bmatrix}
    x_{11}&x_{12}&\cdots&x_{1d}&1\\
    x_{21}&x_{22}&\cdots&x_{2d}&1\\
    \vdots&\vdots&\ddots&\vdots&\vdots\\
    x_{n1}&x_{n2}&\cdots&x_{nd}&1\\
    \end{bmatrix}
    $$

    ## Diagrams with Mermaid

    Here is one mermaid diagram:
    <pre class="mermaid">
    graph LR
        A[Square Rect] -- Link text --> B((Circle))
        A --> C(Round Rect)
        B --> D{Rhombus}
        C --> D
    </pre>

    And here is another:
    <pre class="mermaid">
    sequenceDiagram
        participant Alice
        participant Bob
        Alice->>John: Hello John, how are you?
        loop HealthCheck
            John->>John: Fight against hypochondria
        end
        Note right of John: Rational thoughts&lt;br/>prevail...
        John-->>Alice: Great!
        John->>Bob: How about you?
        Bob-->>John: Jolly good!
    </pre>

    ## Tables

    |Header 1|Header 2|Header 3|
    |--------|--------|--------|
    | Cell 1 | Cell 2 | Cell 3 |
    | Cell 4 | Cell 5 | Cell 6 |
    | Cell 7 | Cell 8 | Cell 9 |

    ### Table Alignment

    | Left-aligned | Center-aligned | Right-aligned |
    |:------|:--------:|-------:|
    | Left  |  Center  |  Right |
    | Left  |  Center  |  Right |

    ## Horizontal Rule

    ---

    or

    ***

    or

    ___

    ## HTML in Markdown

    Markdown supports HTML tags when you need more control over formatting:

    <div style="color: red; text-align: center;">
      <p>This text is red and centered using HTML.</p>
    </div>

    <details>
      <summary>Click to expand!</summary>
    This content is hidden by default but can be expanded by clicking.
    </details>
""".trimIndent()