package org.yangdai.kori.presentation.component.note.markdown

class MarkdownLint {

    // 规则标识符 (为了清晰度和未来可能的配置)
    companion object Rules {
        object RuleIds {
            const val HEADING_INVALID_FORMAT = "MD001"       // 标题格式无效
            const val HEADING_INVALID_SPACING = "MD002"      // 标题井号后空格无效
            const val HEADING_TRAILING_PUNCTUATION = "MD003" // 标题结尾包含不规范标点
            const val TRAILING_SPACES = "MD004"              // 行尾存在多余空格 (通常>=3个)
            const val LINK_CHINESE_PARENTHESES = "MD005"     // 链接使用了全角括号（）
            const val IMAGE_CHINESE_EXCLAMATION = "MD006"    // 图片链接前使用了全角感叹号！
        }


        // 标题结构: 可选前导空格, #号(1-6个), 必须的空格, 内容
        val HEADING_PATTERN = Regex("^\\s*(#{1,6})(\\s+)(.*)$")

        // 匹配字符串末尾的特定标点符号
        val TRAILING_PUNCTUATION = Regex("[.,;:]$")

        // 使用正向后查找 (?<=...) 来匹配紧跟在两个空格之后的一个或多个空格，直到行尾
        // 这会直接匹配到从第三个空格开始的所有行尾空格
        val EXCESS_TRAILING_SPACES_PATTERN = Regex("(?<=\\s{2})\\s+$")

        // 匹配使用全角括号的链接: [文本]（URL）
        val CHINESE_LINK_PATTERN = Regex("\\[([^\\[\\]]*)]\\s*（([^（）]*)）")

        // 匹配前面带有全角感叹号的图片链接: ！[替代文本](URL)
        val CHINESE_EXCLAMATION_IMAGE_PATTERN = Regex("！\\s*\\[([^\\[\\]]*)]\\([^()]*\\)")

        // 匹配围栏代码块的开始/结束标记 (``` 或 ~~~), 可能带有语言标识符
        val FENCED_CODE_BLOCK_MARKER = Regex("^\\s*(`{3,}|~{3,})([^`~]*)?$")
    }

    // 增强的 Issue 类
    data class Issue(
        val startIndex: Int,    // 问题起始索引 (包含)
        val endIndex: Int,      // 问题结束索引 (不包含)
        val ruleId: String     // 对应的规则 ID
    )

    // 主验证函数
    fun validate(markdown: String): List<Issue> {
        val issues = mutableListOf<Issue>() // 存储发现的问题
        var inFencedCodeBlock = false       // 当前是否处于围栏代码块内部
        var fencedCodeBlockMarker: String? = null // 存储围栏代码块的起始标记 (例如 "```")
        var currentOffset = 0               // 当前处理位置在整个 markdown 字符串中的偏移量

        // 逐行处理 Markdown 文本
        markdown.lineSequence().forEach { line ->
            val lineStartOffset = currentOffset
            // val lineEndOffset = lineStartOffset + line.length // 不再直接需要 lineEndOffset

            // --- 围栏代码块检测 ---
            val fencedCodeMatch =
                FENCED_CODE_BLOCK_MARKER.matchEntire(line.trim()) // 尝试匹配整行是否为代码块标记
            if (fencedCodeMatch != null) {
                val currentMarker = fencedCodeMatch.groupValues[1] // 获取标记符号，如 "```" 或 "~~~"
                if (!inFencedCodeBlock) {
                    // 进入围栏代码块
                    inFencedCodeBlock = true
                    fencedCodeBlockMarker = currentMarker
                } else if (currentMarker.length >= (fencedCodeBlockMarker?.length
                        ?: 0) && currentMarker.first() == fencedCodeBlockMarker?.first()
                ) {
                    // 结束围栏代码块
                    inFencedCodeBlock = false
                    fencedCodeBlockMarker = null
                }
                // 代码块标记行本身通常不进行检查，直接处理下一行
                currentOffset += line.length + 1 // 更新偏移量，计入换行符 '\n'
                return@forEach // 跳过此代码块标记行的后续处理
            }

            if (inFencedCodeBlock) {
                // 当前在围栏代码块内，跳过 Lint 检查
            } else {
                if (line.isNotBlank()) {
                    // --- 以下检查仅在非空行执行 ---
                    // 标题检查 (如果行以 '#' 开头)
                    if (line.trimStart().startsWith("#")) {
                        validateHeading(line, lineStartOffset, issues)
                    }

                    // 行尾空格检查
                    validateTrailingSpaces(line, lineStartOffset, issues)

                    // 行内元素检查 (链接、图片格式、跳过行内代码)
                    validateInlineElements(line, lineStartOffset, issues)
                }
            }

            currentOffset += line.length + 1 // 移动偏移量到下一行的开始
        } // 行处理循环结束

        return issues // 返回所有发现的问题列表
    }

    // --- 验证辅助函数 ---

    /**
     * 验证单行是否为合规的 Markdown 标题。
     * @param line 当前行文本
     * @param lineOffset 当前行在全文中的起始偏移量
     * @param issues 问题列表，用于添加发现的问题
     */
    private fun validateHeading(line: String, lineOffset: Int, issues: MutableList<Issue>) {
        val trimmedLine = line.trimStart() // 去除行首空格
        val leadingSpaces = line.length - trimmedLine.length // 计算行首空格数
        val headingMatch = HEADING_PATTERN.matchEntire(trimmedLine) // 严格匹配标题模式

        if (headingMatch == null) {
            // 如果严格模式匹配失败，但确实以 '#' 开头，则报通用格式错误
            // 例如 "#无空格", "####### 超过6个#"
            if (trimmedLine.startsWith("#")) {
                issues.add(
                    Issue(
                        lineOffset + leadingSpaces, // 问题从第一个 '#' 开始
                        lineOffset + line.length,   // 问题到行尾结束
                        RuleIds.HEADING_INVALID_FORMAT
                    )
                )
            }
            return // 不是有效标题或已报告格式错误
        }

        // 提取匹配组
        val hashes = headingMatch.groupValues[1] // '#' 的序列, e.g., "##"
        val spaces = headingMatch.groupValues[2] // '#' 后的空格序列, e.g., "   "
        val content = headingMatch.groupValues[3].trimEnd() // 标题内容，去除尾部空格

        // 检查点 1: '#' 和标题内容之间必须有且仅有一个空格
        if (spaces.length != 1) {
            issues.add(
                Issue(
                    lineOffset + leadingSpaces + hashes.length + 1, // 定位到多余空格的开始 (第一个空格之后)
                    lineOffset + leadingSpaces + hashes.length + spaces.length, // 定位到多余空格的结束
                    RuleIds.HEADING_INVALID_SPACING
                )
            )
        }

        // 检查点 2: 标题内容末尾不应包含指定的标点符号 (.,;:)
        if (content.isNotEmpty() && TRAILING_PUNCTUATION.containsMatchIn(content)) {
            val punctuationIndex = content.length - 1 // 标点符号在 content 中的索引
            issues.add(
                Issue(
                    // 计算标点符号在 *原始行* 中的绝对索引
                    lineOffset + leadingSpaces + hashes.length + spaces.length + punctuationIndex,
                    lineOffset + leadingSpaces + hashes.length + spaces.length + punctuationIndex + 1, // 结束索引 = 开始索引 + 1
                    RuleIds.HEADING_TRAILING_PUNCTUATION
                )
            )
        }
    }

    /**
     * 验证行尾是否有多余的空格（指第3个及以后的空格）。
     * Markdown中两个行尾空格通常用于强制换行，此规则标记超出此范围的空格。
     * @param line 当前行文本
     * @param lineOffset 当前行在全文中的起始偏移量
     * @param issues 问题列表，用于添加发现的问题
     */
    private fun validateTrailingSpaces(line: String, lineOffset: Int, issues: MutableList<Issue>) {
        // 使用新的正则表达式查找从第三个开始的行尾空格
        // 正向后查找 `(?<=\\s{2})` 确保匹配开始位置的前面是两个空格
        // `\\s+` 匹配一个或多个空格
        // `$` 确保匹配到行尾
        EXCESS_TRAILING_SPACES_PATTERN.find(line)?.let { match ->
            issues.add(
                Issue(
                    lineOffset + match.range.first,     // 问题开始于第3个行尾空格的索引
                    lineOffset + match.range.last + 1,  // 问题结束于最后一个行尾空格之后
                    RuleIds.TRAILING_SPACES
                )
            )
        }
    }

    /**
     * 验证行内的 Markdown 元素，如链接格式，并跳过行内代码块。
     */
    private fun validateInlineElements(line: String, lineOffset: Int, issues: MutableList<Issue>) {
        // 需要跳过行内代码片段 (`...`) 中的检查
        var i = 0 // 当前在行内的扫描位置
        while (i < line.length) {
            // --- 检测下一个行内代码片段 ---
            // findNextInlineCodeSpan 返回代码片段的 (起始索引, 结束索引) 或 null
            val codeSpan = findNextInlineCodeSpan(line, i)

            // 确定下一个要检查的文本段的范围
            // 如果找到了代码片段，检查范围是当前位置 `i` 到代码片段开始 `codeSpan.first`
            // 如果没找到代码片段，检查范围是当前位置 `i` 到行尾 `line.length`
            val nextCheckStart = i // 通常是当前位置
            val nextCheckEnd = codeSpan?.first ?: line.length // 代码块开始处 或 行尾

            // --- 在非代码片段的文本段内执行检查 ---
            if (nextCheckStart < nextCheckEnd) { // 确保有内容需要检查
                val segmentToCheck = line.substring(nextCheckStart, nextCheckEnd) // 提取要检查的子字符串
                val segmentOffset = lineOffset + nextCheckStart                 // 计算子字符串在全文中的偏移量

                // 检查1：此段内是否存在使用全角括号的链接 [text]（url）
                CHINESE_LINK_PATTERN.findAll(segmentToCheck).forEach { match ->
                    val fullMatchText = match.value          // 完整的匹配文本，如 "[链接]（地址）"
                    // 在匹配文本内部查找全角括号的位置
                    val openParenIndexInMatch = fullMatchText.indexOf('（')
                    val closeParenIndexInMatch = fullMatchText.indexOf('）')

                    // 报告左括号问题
                    if (openParenIndexInMatch != -1) {
                        issues.add(
                            Issue(
                                segmentOffset + match.range.first + openParenIndexInMatch, // 计算 '（' 在全文中的绝对位置
                                segmentOffset + match.range.first + openParenIndexInMatch + 1, // 结束位置 = 开始位置 + 1
                                RuleIds.LINK_CHINESE_PARENTHESES
                            )
                        )
                    }
                    // 报告右括号问题
                    if (closeParenIndexInMatch != -1) {
                        issues.add(
                            Issue(
                                segmentOffset + match.range.first + closeParenIndexInMatch, // 计算 '）' 在全文中的绝对位置
                                segmentOffset + match.range.first + closeParenIndexInMatch + 1, // 结束位置 = 开始位置 + 1
                                RuleIds.LINK_CHINESE_PARENTHESES
                            )
                        )
                    }
                }

                // 检查2：此段内是否存在以全角感叹号开头的图片链接 ！[alt](url)
                CHINESE_EXCLAMATION_IMAGE_PATTERN.findAll(segmentToCheck).forEach { match ->
                    val exclamationIndexInMatch = match.value.indexOf('！') // 查找 '！' 在匹配文本中的位置
                    if (exclamationIndexInMatch != -1) { // 通常 '！' 就在开头，所以索引是 0
                        issues.add(
                            Issue(
                                segmentOffset + match.range.first + exclamationIndexInMatch, // 计算 '！' 在全文中的绝对位置
                                segmentOffset + match.range.first + exclamationIndexInMatch + 1, // 结束位置 = 开始位置 + 1
                                RuleIds.IMAGE_CHINESE_EXCLAMATION
                            )
                        )
                    }
                }
            } // --- 段内检查结束 ---

            // --- 移动扫描位置 ---
            // 如果找到了代码片段，将 `i` 移动到代码片段的末尾
            // 如果没找到，说明已检查到行尾，`i` 设为 `line.length` 退出循环
            i = codeSpan?.second ?: line.length
        }
    }

    /**
     * 辅助函数：从 startIndex 开始查找行内的下一个行内代码片段 (`...`)。
     * 会考虑反引号数量匹配和转义。
     * @return Pair(代码片段起始索引(含), 代码片段结束索引(不含))；如果未找到则返回 null。
     */
    private fun findNextInlineCodeSpan(line: String, startIndex: Int): Pair<Int, Int>? {
        var i = startIndex
        while (i < line.length) {
            // 检查是否是未转义的反引号
            if (line[i] == '`' && !isEscaped(i, line)) {
                // 找到潜在的起始反引号，计算连续反引号的数量
                var tickCount = 1
                while (i + tickCount < line.length && line[i + tickCount] == '`') {
                    tickCount++
                }

                // 从起始反引号序列之后开始，查找具有相同数量 `tickCount` 的闭合反引号序列
                val endTickIndex = findClosingBackticks(line, i + tickCount, tickCount)
                if (endTickIndex != -1) {
                    // 找到了匹配的闭合反引号，返回代码片段的范围
                    // 起始索引是 `i` (第一个反引号的位置)
                    // 结束索引是 `endTickIndex + tickCount` (闭合反引号序列之后的位置)
                    return Pair(i, endTickIndex + tickCount)
                } else {
                    // 没有找到匹配的闭合反引号（可能跨行或格式错误）
                    // 为避免无限循环或错误处理，简单地将索引 `i` 移过当前找到的起始反引号序列
                    i += tickCount
                }
            } else {
                // 当前字符不是起始反引号，继续向后查找
                i++
            }
        }
        return null // 没有找到更多的行内代码片段
    }


    /**
     * 辅助函数：从 startIndex 开始查找第一个与 `count` 数量匹配的、未转义的闭合反引号序列。
     * @param text 要搜索的文本 (通常是一行)
     * @param startIndex 开始搜索的索引
     * @param count 需要匹配的连续反引号数量
     * @return 第一个匹配的闭合反引号序列的 *起始* 索引；如果未找到则返回 -1。
     */
    private fun findClosingBackticks(text: String, startIndex: Int, count: Int): Int {
        var i = startIndex
        while (i < text.length) {
            // 检查是否是未转义的反引号
            if (text[i] == '`' && !isEscaped(i, text)) {
                // 计算当前位置连续反引号的数量
                var matchCount = 1
                while (i + matchCount < text.length && text[i + matchCount] == '`') {
                    matchCount++
                }
                // 如果数量与期望的 `count` 相同，则找到闭合序列
                if (matchCount == count) {
                    return i // 返回这个闭合序列的起始索引
                }
                // 如果数量不匹配，则跳过当前找到的这个反引号序列，继续搜索
                // 例如，遇到 `` 时，如果期望是 `，则跳过这两个 ``
                i += matchCount
            } else {
                // 当前字符不是反引号，继续向后搜索
                i++
            }
        }
        return -1 // 未找到匹配的闭合反引号序列
    }

    /**
     * 工具函数：检查给定索引处的字符是否被反斜杠转义了。
     * 它会向前看，计算连续的反斜杠数量，奇数表示被转义。
     */
    private fun isEscaped(index: Int, text: String): Boolean {
        if (index <= 0) return false // 索引0或负数不可能被转义
        var backslashCount = 0
        var i = index - 1
        // 向前回溯，计算连续的反斜杠数量
        while (i >= 0 && text[i] == '\\') {
            backslashCount++
            i--
        }
        // 如果反斜杠数量是奇数，则 index 处的字符被转义
        return backslashCount % 2 == 1
    }
}
