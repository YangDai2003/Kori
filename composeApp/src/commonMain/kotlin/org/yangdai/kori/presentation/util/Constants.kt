package org.yangdai.kori.presentation.util

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
        const val IS_MARKDOWN_LINT_ENABLED = "is_markdown_lint_enabled"
        const val IS_DEFAULT_READING_VIEW = "is_default_reading_view"

        const val DATE_FORMATTER = "date_formatter"
        const val TIME_FORMATTER = "time_formatter"

        const val CARD_SIZE = "card_size"
        const val CLIP_OVERFLOW_TEXT = "clip_overflow_text"

        const val IS_AI_ENABLED = "is_ai_enabled"
        const val AI_FEATURES = "ai_features"
        const val AI_PROVIDER = "ai_provider"

        const val GEMINI_API_KEY = "gemini_api_key"
        const val GEMINI_API_HOST = "gemini_api_host"
        const val GEMINI_MODEL = "gemini_model"

        const val OPENAI_API_KEY = "openai_api_key"
        const val OPENAI_API_HOST = "openai_api_host"
        const val OPENAI_MODEL = "openai_model"

        const val OLLAMA_API_HOST = "ollama_api_host"
        const val OLLAMA_MODEL = "ollama_model"

        const val LMSTUDIO_API_HOST = "lmstudio_api_host"
        const val LMSTUDIO_MODEL = "lmstudio_model"
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

val SampleMarkdownNote = """
    # Markdown Syntax Guide
    
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

    ## Text Formatting

    **Bold text** or __also bold__

    *Italic text* or _also italic_

    ***Bold and italic*** or ___also bold and italic___

    ~~Strikethrough~~ or <del>also strikethrough</del>

    <ins>Underline</ins> and <mark>Highlight</mark>

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

    ```kotlin
    fun createConfetti() {
        val colors = listOf("🔴", "🟠", "🟡", "🟢", "🔵", "🟣")
        repeat(20) {
            val color = colors.random()
            val position = (1..80).random()
            println(" ".repeat(position) + color)
            Thread.sleep(50)
        }
        println("🎉 Surprise! 🎉")
    }
    ```

    ## Math Expressions

    ### Inline Math

    Einstein's equation: ${'$'}E = mc^2$

    The quadratic formula: ${'$'}x = \frac{-b \pm \sqrt{b^2 - 4ac}}{2a}$

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
        graph TD
        A[Client] --> B[Load Balancer]
        B --> C[Server1]
        B --> D[Server2]
    </pre>

    And here is another:
    <pre class="mermaid">
        quadrantChart
            title Reach and engagement of campaigns
            x-axis Low Reach --> High Reach
            y-axis Low Engagement --> High Engagement
            quadrant-1 We should expand
            quadrant-2 Need to promote
            quadrant-3 Re-evaluate
            quadrant-4 May be improved
            Campaign A: [0.3, 0.6]
            Campaign B: [0.45, 0.23]
            Campaign C: [0.57, 0.69]
            Campaign D: [0.78, 0.34]
            Campaign E: [0.40, 0.34]
            Campaign F: [0.35, 0.78]
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