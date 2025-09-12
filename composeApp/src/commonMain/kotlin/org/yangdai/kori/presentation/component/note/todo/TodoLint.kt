package org.yangdai.kori.presentation.component.note.todo

import org.yangdai.kori.presentation.component.note.Issue
import org.yangdai.kori.presentation.component.note.Lint

class TodoLint : Lint {

    companion object {
        object RuleIds {
            // 'x' 必须是小写
            const val COMPLETION_MARKER_NOT_LOWERCASE = "TODO001"

            // 'x' 后面必须有且仅有一个空格
            const val INVALID_SPACE_AFTER_COMPLETION_MARKER = "TODO002"

            // 'x' 位置错误
            const val INVALID_COMPLETION_MARKER_POSITION = "TODO003"

            // 优先级 '(A-Z)' 格式错误或位置错误
            const val INVALID_PRIORITY_FORMAT_OR_PLACEMENT = "TODO004"
        }

        val INVALID_COMPLETION_MARKER_REGEX = Regex(" x +", RegexOption.IGNORE_CASE)

        // 正则表达式: 匹配优先级标记，后面紧跟一个或多个空格
        val PRIORITY_REGEX = Regex("\\(([A-Z])\\) +")

        val INVALID_PRIORITY_REGEX = Regex("\\(([a-z])\\) +")
    }

    override fun validate(text: String): List<Issue> {
        val issues = mutableListOf<Issue>()
        var currentOffset = 0 // 用于跟踪当前字符在整个文本中的索引

        text.lineSequence().forEach { line ->
            val lineStartOffset = currentOffset

            // 跳过空行
            if (line.isNotBlank()) {
                // 检查 'x' 是否为小写且后跟一个空格
                if (line.startsWith("x", ignoreCase = true)) {
                    // 检查 'x' 是否为小写
                    if (line.first() != 'x') {
                        issues.add(
                            Issue(
                                startIndex = lineStartOffset,
                                endIndex = lineStartOffset + 1,
                                ruleId = RuleIds.COMPLETION_MARKER_NOT_LOWERCASE
                            )
                        )
                    }
                    // 检查 'x' 后面是否有多余一个的空格
                    if (line.length > 2 && line[2] == ' ') { // 多于一个空格
                        issues.add(
                            Issue(
                                startIndex = lineStartOffset + 1, // 从第一个空格开始
                                endIndex = lineStartOffset + line.takeWhile { it == 'x' || it == ' ' }.length, // 标记所有连续的空格
                                ruleId = RuleIds.INVALID_SPACE_AFTER_COMPLETION_MARKER
                            )
                        )
                    }
                }

                // 检查完成标记错误位置
                val invalidCompletionMarkerMatch = INVALID_COMPLETION_MARKER_REGEX.find(line)
                if (invalidCompletionMarkerMatch != null) {
                    issues.add(
                        Issue(
                            startIndex = lineStartOffset + invalidCompletionMarkerMatch.range.first,
                            endIndex = lineStartOffset + invalidCompletionMarkerMatch.range.last + 1,
                            ruleId = RuleIds.INVALID_COMPLETION_MARKER_POSITION
                        )
                    )
                }

                // 检查 '(A-Z)' 是否在行首或在 'x ' 后面
                val priorityMatch = PRIORITY_REGEX.find(line)
                if (priorityMatch != null) {
                    val priorityStartIndexInLine = priorityMatch.range.first

                    // 检查优先级是否在允许的位置
                    val isAtStart = priorityStartIndexInLine == 0
                    val isAfterCompletion = line.startsWith("x ") && priorityStartIndexInLine == 2

                    if (!isAtStart && !isAfterCompletion) {
                        issues.add(
                            Issue(
                                startIndex = lineStartOffset + priorityStartIndexInLine,
                                endIndex = lineStartOffset + priorityMatch.range.last + 1,
                                ruleId = RuleIds.INVALID_PRIORITY_FORMAT_OR_PLACEMENT
                            )
                        )
                    }
                }

                // 检查小写优先级
                val invalidPriorityMatch = INVALID_PRIORITY_REGEX.find(line)
                if (invalidPriorityMatch != null) {
                    issues.add(
                        Issue(
                            startIndex = lineStartOffset + invalidPriorityMatch.range.first,
                            endIndex = lineStartOffset + invalidPriorityMatch.range.last + 1,
                            ruleId = RuleIds.INVALID_PRIORITY_FORMAT_OR_PLACEMENT
                        )
                    )
                }
            }

            // 更新偏移量，为下一行做准备 (+1 是为了换行符)
            currentOffset += line.length + 1
        }
        return issues
    }
}