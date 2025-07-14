package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.ui.text.TextRange

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

fun TextFieldBuffer.toggleLineStart(str: String) {
    val text = toString()
    val lineStart = text.take(selection.min)
        .lastIndexOf('\n')
        .takeIf { it != -1 }
        ?.let { it + 1 }
        ?: 0

    val lineContent = text.substring(lineStart, selection.min)

    if (lineContent.startsWith(str)) {
        // 如果行首已经有 str，则删除它
        replace(lineStart, lineStart + str.length, "")
    } else {
        // 否则添加 str
        replace(lineStart, lineStart, str)
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