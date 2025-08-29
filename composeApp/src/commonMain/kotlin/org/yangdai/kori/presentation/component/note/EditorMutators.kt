package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.TextRange
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.todayIn

fun TextFieldBuffer.moveCursorLeftStateless() {
    if (selection.min > 0) {
        selection = TextRange(selection.min - 1, selection.min - 1)
    }
}

fun TextFieldBuffer.moveCursorRightStateless() {
    if (selection.max < length) {
        selection = TextRange(selection.max + 1, selection.max + 1)
    }
}

private fun TextFieldBuffer.inlineWrap(
    startWrappedString: String,
    endWrappedString: String = startWrappedString,
    initialSelection: TextRange = selection
) = if (initialSelection.collapsed) {
    // No text selected, insert at cursor position and place cursor in the middle
    replace(initialSelection.min, initialSelection.min, startWrappedString + endWrappedString)
    selection = TextRange(
        initialSelection.min + startWrappedString.length,
        initialSelection.min + startWrappedString.length
    )
} else {
    replace(initialSelection.min, initialSelection.min, startWrappedString)
    replace(
        initialSelection.max + startWrappedString.length,
        initialSelection.max + startWrappedString.length,
        endWrappedString
    )
    selection = TextRange(
        initialSelection.min,
        initialSelection.max + startWrappedString.length + endWrappedString.length
    )
}

fun TextFieldBuffer.bold() = inlineWrap("**")
fun TextFieldBuffer.italic() = inlineWrap("_")
fun TextFieldBuffer.underline() = inlineWrap("<ins>", "</ins>")
fun TextFieldBuffer.strikeThrough() = inlineWrap("~~")
fun TextFieldBuffer.highlight() = inlineWrap("<mark>", "</mark>")
fun TextFieldBuffer.parentheses() = inlineWrap("(", ")")
fun TextFieldBuffer.brackets() = inlineWrap("[", "]")
fun TextFieldBuffer.braces() = inlineWrap("{", "}")
fun TextFieldBuffer.link() = inlineWrap("[", "]()")
fun TextFieldBuffer.inlineCode() = inlineWrap("`")
fun TextFieldBuffer.codeBlock() = inlineWrap("```\n", "\n```\n")
fun TextFieldBuffer.inlineMath() = inlineWrap("$")
fun TextFieldBuffer.mathBlock() = inlineWrap("$$\n", "\n$$\n")
fun TextFieldBuffer.mermaidDiagram() = inlineWrap("<pre class=\"mermaid\">\n", "\n</pre>\n")


fun TextFieldBuffer.quote() {
    val text = toString()
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val initialSelection = selection

    replace(lineStart, lineStart, "> ")
    selection = TextRange(
        initialSelection.min + 2,
        initialSelection.max + 2
    )
}

fun TextFieldBuffer.tab() {
    val text = toString()
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val initialSelection = selection

    replace(lineStart, lineStart, "    ") // 4 spaces
    selection = TextRange(
        initialSelection.min + 4,
        initialSelection.max + 4
    )
}

fun TextFieldBuffer.unTab() {
    val text = toString()
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val tabIndex = text.indexOf("    ", lineStart)
    val initialSelection = selection

    if (tabIndex != -1 && tabIndex < selection.min) {
        replace(tabIndex, tabIndex + 4, "")
        selection = TextRange(
            initialSelection.min - 4,
            initialSelection.max - 4
        )
    }
}

fun TextFieldBuffer.alert(type: String) {
    val text = toString()
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val initialSelection = selection
    val alertType = "> [!$type]"
    replace(lineStart, lineStart, alertType)
    replace(
        lineStart + alertType.length,
        lineStart + alertType.length,
        "\n> "
    )
    selection = TextRange(
        initialSelection.min + type.length + 8,
        initialSelection.max + type.length + 8
    )
}

/**
 *
 * - **标记为完成**: 在行首添加 'x '。
 *   - 如果事项中没有完成日期，则在规定位置添加当前日期。
 *   - 如果已有完成日期（通常意味着它已经是 '完成' 状态，但代码会做健壮性处理），则仅确保 'x ' 存在。
 *
 * - **标记为未完成**: 移除行首的 'x '。
 *   - 如果存在完成日期，则一并移除。
 *
 * @param str 通常是 "x "，代表完成状态的字符串。
 */
@OptIn(FormatStringsInDatetimeFormats::class)
fun TextFieldBuffer.toggleLineStart(str: String) {
    val text = toString()
    if (selection.min > text.length) return

    // 1. 确定当前光标所在行的起始和结束位置
    val lineStart = text.take(selection.min).lastIndexOf('\n').let { if (it != -1) it + 1 else 0 }
    val lineEnd = text.indexOf('\n', lineStart).let { if (it != -1) it else text.length }
    val lineContent = text.substring(lineStart, lineEnd)

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val dateFormat = LocalDate.Format { byUnicodePattern("yyyy-MM-dd") }
    val todayStr = dateFormat.format(today)

    // 正则表达式来匹配优先级和日期
    // 匹配如 "(A) " 的优先级标记
    val priorityRegex = Regex("^\\s*\\(\\w\\)\\s+")
    // 匹配如 "2024-08-21 " 的日期
    val dateRegex = Regex("^\\s*\\d{4}-\\d{2}-\\d{2}\\s+")

    // 2. 判断当前行是否已经标记为完成
    if (lineContent.trim().startsWith(str.trim())) {
        // --- 标记为未完成 ---
        var tempLine = lineContent.substring(str.length)
        val priorityMatch = priorityRegex.find(tempLine)
        var hasPriority = false

        if (priorityMatch != null) {
            // 如果有优先级，检查优先级后面的内容
            tempLine = tempLine.substring(priorityMatch.value.length)
            hasPriority = true
        }

        val dateMatch = dateRegex.find(tempLine)
        if (dateMatch != null) {
            // 如果找到了完成日期，移除它
            tempLine = tempLine.substring(dateMatch.value.length)
        }

        // 重新组合成未完成状态的行
        val newLine = if (hasPriority) priorityMatch!!.value + tempLine else tempLine
        replace(lineStart, lineEnd, newLine)

    } else {
        // --- 标记为完成 ---
        val priorityMatch = priorityRegex.find(lineContent)
        if (priorityMatch != null) {
            // 情况 A: 带有优先级，如 "(A) 2016-04-30 task..."
            val restOfLine = lineContent.substring(priorityMatch.value.length)
            // 组合成: "x (A) yyyy-MM-dd 2016-04-30 task..."
            val newLine = "$str${priorityMatch.value}$todayStr $restOfLine"
            replace(lineStart, lineEnd, newLine)
        } else {
            // 情况 B: 不带优先级，如 "2016-04-30 task..." 或 "task..."
            // 组合成: "x yyyy-MM-dd 2016-04-30 task..."
            val newLine = "$str$todayStr $lineContent"
            replace(lineStart, lineEnd, newLine)
        }
    }
}

fun TextFieldBuffer.addBeforeWithWhiteSpace(str: String) {
    val initialSelection = selection
    val text = toString()
    val needSpace = initialSelection.min > 0 && !text[initialSelection.min - 1].isWhitespace()
    if (needSpace) replace(initialSelection.min, initialSelection.min, " $str")
    else replace(initialSelection.min, initialSelection.min, str)
}

fun TextFieldBuffer.addAfter(str: String) {
    val initialSelection = selection
    replace(initialSelection.max, initialSelection.max, str)
}

fun TextFieldBuffer.addInNewLine(str: String) {
    val text = toString()
    if (selection.max > 0 && text[selection.max - 1] != '\n') {
        // 如果不是换行符，那么就先添加一个换行符
        addAfter("\n")
    }
    addAfter(str)
}

fun TextFieldBuffer.header(level: Int) {
    val heading = "#".repeat(level) + " "
    val text = toString()
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val initialSelection = selection

    replace(lineStart, lineStart, heading)
    selection = TextRange(
        initialSelection.min + heading.length,
        initialSelection.max + heading.length
    )
}

fun TextFieldBuffer.horizontalRule() {
    addInNewLine("\n")
    addAfter("------\n")
    addAfter("\n")
}

fun TextFieldBuffer.bulletList() {
    if (selection.collapsed) {
        // 如果没有选中任何文本，则直接在新行插入 "- "
        addInNewLine("- ")
    } else {
        val start = selection.start
        val end = selection.end
        val allText = toString()
        val selectedText = allText.substring(start, end)

        // 逐行在第一个非空格字符之前插入 "- "
        val transformedText = selectedText
            .lineSequence()
            .map { line ->
                // 找到当前行第一个非空格字符下标
                val index = line.indexOfFirst { !it.isWhitespace() }
                if (index >= 0) {
                    val prefix = line.substring(0, index)
                    val content = line.substring(index)
                    "$prefix- $content"
                } else {
                    // 整行都是空格或为空，则不处理
                    line
                }
            }
            .joinToString("\n")

        // 用处理后的文本替换原选中区域
        replace(start, end, transformedText)
        selection = TextRange(start, start + transformedText.length)
    }
}

fun TextFieldBuffer.numberedList() {
    if (selection.collapsed) {
        // 无选中区域时，直接插入 "1. "
        addInNewLine("1. ")
    } else {
        val start = selection.start
        val end = selection.end
        val allText = this.toString()
        val selectedText = allText.substring(start, end)

        // 计算行首缩进级别，这里简单假设 4 个空格相当于 1 级缩进，tab 视为 4 个空格
        fun getIndentLevel(line: String): Int {
            var count = 0
            for (char in line) {
                when (char) {
                    ' ' -> count++
                    '\t' -> count += 4
                    else -> break
                }
            }
            return count / 4
        }

        // 记录各级缩进对应的编号计数
        val counters = mutableListOf<Int>()

        val transformedText = selectedText
            .lineSequence()
            .map { line ->
                // 获取本行缩进级别
                val indentLevel = getIndentLevel(line)
                // 如果当前 counters 大小不够，则补充到对应级别
                while (counters.size <= indentLevel) {
                    counters.add(0)
                }
                // 如果当前 counters 大小太大，则将不需要的级别弹出
                while (counters.size > indentLevel + 1) {
                    counters.removeAt(counters.size - 1)
                }
                // 给本级缩进对应的编号 +1
                counters[indentLevel] = counters[indentLevel] + 1

                // 将标号插入行首缩进的后面
                val firstNonSpaceIndex = line.indexOfFirst { !it.isWhitespace() }
                if (firstNonSpaceIndex >= 0) {
                    val prefix = line.substring(0, firstNonSpaceIndex)
                    val content = line.substring(firstNonSpaceIndex)
                    "$prefix${counters[indentLevel]}. $content"
                } else {
                    line
                }
            }
            .joinToString("\n")

        replace(start, end, transformedText)
        selection = TextRange(start, start + transformedText.length)
    }
}

fun TextFieldBuffer.taskList() {
    if (selection.collapsed) {
        addInNewLine("- [ ] ")
    } else {
        val start = selection.start
        val end = selection.end
        val allText = toString()
        val selectedText = allText.substring(start, end)

        val transformedText = selectedText
            .lineSequence()
            .map { line ->
                val index = line.indexOfFirst { !it.isWhitespace() }
                if (index >= 0) {
                    val prefix = line.substring(0, index)
                    val content = line.substring(index)
                    "$prefix- [ ] $content"
                } else {
                    line
                }
            }
            .joinToString("\n")

        replace(start, end, transformedText)
        selection = TextRange(start, start + transformedText.length)
    }
}

fun TextFieldBuffer.addImageLinks(names: List<Pair<String, String>>) {
    val markdownImages = names.joinToString(separator = "\n") { "![${it.first}](${it.second})" }
    addInNewLine(markdownImages)
}

fun TextFieldBuffer.addVideoLink(name: Pair<String, String>) {
    val htmlVideo = "<video src=\"${name.second}\" controls>${name.first}</video>\n"
    addInNewLine(htmlVideo)
}

fun TextFieldBuffer.addAudioLink(name: Pair<String, String>) {
    val htmlAudio = "<audio src=\"${name.second}\" controls>${name.first}</audio>\n"
    addInNewLine(htmlAudio)
}