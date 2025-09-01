package org.yangdai.kori.presentation.component.note

import kmark.html.HtmlGenerator
import org.yangdai.kori.presentation.component.note.markdown.MarkdownDefaults

sealed interface ProcessedContent {
    data class Markdown(val html: String) : ProcessedContent
    data class Todo(
        val undoneItems: List<TodoItem>,
        val doneItems: List<TodoItem>
    ) : ProcessedContent

    data object Empty : ProcessedContent
}

data class TodoItem(
    val raw: String,
    val isDone: Boolean,
    val priority: Char?, // A~Z
    val date: String?,   // yyyy-MM-dd
    val sortDate: String?, // 用于排序的日期
)

fun processTodo(lines: List<String>): Pair<List<TodoItem>, List<TodoItem>> {
    val priorityRegex = Regex("""^\(([A-Z])\) """)
    val dateRegex = Regex("""(?<=^|\s)(\d{4}-\d{2}-\d{2})(?=\s|$)""")
    val doneRegex = Regex("""^x\s""")
    val doneDateRegex =
        Regex("""^x\s(?:\(([A-Z])\)\s)?(\d{4}-\d{2}-\d{2})(?:\s(\d{4}-\d{2}-\d{2}))?""")

    val undone = mutableListOf<TodoItem>()
    val done = mutableListOf<TodoItem>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue

        if (doneRegex.containsMatchIn(trimmed)) {
            var priority: Char? = null
            var date: String? = null // 创建日期
            var sortDate: String? = null // 完成日期

            val m = doneDateRegex.find(trimmed)
            if (m != null) {
                // groupValues 索引: [0]是完整匹配, [1]是优先级, [2]是完成日期, [3]是创建日期
                val priorityStr = m.groupValues.getOrNull(1)
                if (!priorityStr.isNullOrEmpty()) {
                    priority = priorityStr[0]
                }
                sortDate = m.groupValues.getOrNull(2) // 完成日期，用于排序
                date = m.groupValues.getOrNull(3)     // 创建日期 (可选)
            }
            done.add(TodoItem(trimmed, true, priority, date, sortDate))
        } else {
            var priority: Char? = null
            var date: String? = null

            val priMatch = priorityRegex.find(trimmed)
            if (priMatch != null) {
                priority = priMatch.groupValues[1][0]
            }

            // 从优先级之后开始查找
            val textToSearchDate =
                if (priMatch != null) trimmed.substring(priMatch.range.last + 1) else trimmed
            val dateMatch = dateRegex.find(textToSearchDate)
            if (dateMatch != null) {
                date = dateMatch.groupValues[1]
            }
            undone.add(TodoItem(trimmed, false, priority, date, date))
        }
    }

    // 排序逻辑 (保持不变)
    val priorityOrder: (Char?) -> Int = { c -> if (c == null) 26 else (c - 'A') }
    val dateOrder: (String?) -> String = { it ?: "9999-99-99" }
    val undoneSorted = undone.sortedWith(
        compareBy<TodoItem> { priorityOrder(it.priority) }
            .thenBy { dateOrder(it.sortDate) }
    )
    val doneSorted = done.sortedWith(
        // 已完成任务通常按完成日期倒序排列，但这里按正序，与您原逻辑保持一致
        compareBy<TodoItem> { dateOrder(it.sortDate) }
            .thenBy { priorityOrder(it.priority) }
    )
    return Pair(undoneSorted, doneSorted)
}

fun processMarkdown(content: String): String {
    val tree = MarkdownDefaults.parser.buildMarkdownTreeFromString(content)
    return HtmlGenerator(content, tree, MarkdownDefaults.flavor, true).generateHtml()
}
