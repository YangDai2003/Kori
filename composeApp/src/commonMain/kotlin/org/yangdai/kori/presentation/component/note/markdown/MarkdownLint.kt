package org.yangdai.kori.presentation.component.note.markdown

import org.yangdai.kori.presentation.component.note.Issue
import org.yangdai.kori.presentation.component.note.Lint

class MarkdownLint : Lint {

    // 规则标识符 (为了清晰度和未来可能的配置)
    companion object {
        object RuleIds {
            const val HEADING_INVALID_FORMAT = "MD001"       // 标题格式无效
            const val HEADING_INVALID_SPACING = "MD002"      // 标题井号后空格无效
            const val HEADING_TRAILING_PUNCTUATION = "MD003" // 标题结尾包含不规范标点
            const val TRAILING_SPACES = "MD004"              // 行尾存在多余空格 (通常>=3个)
            const val LINK_CHINESE_PARENTHESES = "MD005"     // 链接使用了全角括号（）
            const val IMAGE_CHINESE_EXCLAMATION = "MD006"    // 图片链接前使用了全角感叹号！
            const val INLINE_CODE_SURROUNDING_SPACES = "MD007" // 行内代码内容前后有空格
            const val LINK_TEXT_SURROUNDING_SPACES = "MD008"   // 链接/图片文本前后有空格
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

        // 匹配链接或图片的文本部分 `[text]` (非贪婪捕获内容)
        // 用于检查文本内容前后的空格 (Rule MD008)
        val LINK_TEXT_BRACKETS = Regex("""\[([^\[\]]*?)]""")
    }

    // 主验证函数
    override fun validate(text: String): List<Issue> {
        val issues = mutableListOf<Issue>() // 存储发现的问题
        var inFencedCodeBlock = false       // 当前是否处于围栏代码块内部
        var fencedCodeBlockMarker: String? = null // 存储围栏代码块的起始标记 (例如 "```")
        var currentOffset = 0               // 当前处理位置在整个 markdown 字符串中的偏移量

        // 逐行处理 Markdown 文本
        text.lineSequence().forEach { line ->
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
                // --- 非代码块行检查 ---
                // 标题检查 (如果行以 '#' 开头)
                if (line.trimStart().startsWith("#")) {
                    validateHeading(line, lineStartOffset, issues)
                }

                // 行尾空格检查 (检查第3个及以后的空格)
                validateTrailingSpaces(line, lineStartOffset, issues)

                // 行内元素检查 (链接、图片格式、行内代码格式、跳过行内代码内部)
                validateInlineElements(line, lineStartOffset, issues)
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
     * 验证行内的 Markdown 元素，如链接格式、行内代码格式，并跳过行内代码块内容。
     * 新增: MD007 (行内代码前后空格), MD008 (链接文本前后空格)
     */
    private fun validateInlineElements(line: String, lineOffset: Int, issues: MutableList<Issue>) {
        val codeSpans =
            mutableListOf<Triple<Int, Int, Int>>() // list of (start index, end index, tick count)
        var searchIndex = 0
        // 1. 查找所有行内代码块并检查其内部填充 (MD007)
        while (searchIndex < line.length) {
            val codeSpanInfo = findNextInlineCodeSpan(line, searchIndex)
            if (codeSpanInfo != null) {
                codeSpans.add(codeSpanInfo)
                // --- 检查行内代码内容前后是否有空格 (MD007) ---
                validateInlineCodePadding(line, lineOffset, codeSpanInfo, issues)
                searchIndex = codeSpanInfo.second // 继续从代码块之后搜索
            } else {
                break // 未找到更多行内代码块
            }
        }

        // 2. 检查行内代码块 *之间* 的文本段 (以及首尾)
        var currentSegmentStart = 0
        for (codeSpan in codeSpans) {
            val segmentEnd = codeSpan.first // 当前文本段结束于代码块开始之前
            if (currentSegmentStart < segmentEnd) {
                val segment = line.substring(currentSegmentStart, segmentEnd)
                // 在此文本段内检查链接等规则
                checkSegmentInlineRules(segment, lineOffset + currentSegmentStart, issues)
            }
            currentSegmentStart = codeSpan.second // 下一个文本段从当前代码块之后开始
        }

        // 3. 检查最后一个行内代码块之后到行尾的文本段
        if (currentSegmentStart < line.length) {
            val segment = line.substring(currentSegmentStart)
            checkSegmentInlineRules(segment, lineOffset + currentSegmentStart, issues)
        }
    }


    /**
     * 检查从行内代码块中提取出来的内容前后是否有不必要的空格。
     * @param line 包含代码块的原始行
     * @param lineOffset 行在全文中的偏移量
     * @param codeSpanInfo 代码块信息 (起始索引, 结束索引, 反引号数量)
     * @param issues 问题列表
     */
    private fun validateInlineCodePadding(
        line: String,
        lineOffset: Int,
        codeSpanInfo: Triple<Int, Int, Int>,
        issues: MutableList<Issue>
    ) {
        val (spanStart, spanEnd, tickCount) = codeSpanInfo
        val contentStart = spanStart + tickCount
        val contentEnd = spanEnd - tickCount

        // 检查内容是否存在 (例如 ` `` ` contentStart == contentEnd)
        if (contentStart < contentEnd) {
            var firstNonSpaceIndex = contentStart
            while (firstNonSpaceIndex < contentEnd && line[firstNonSpaceIndex].isWhitespace()) {
                firstNonSpaceIndex++
            }

            // 检查前导空格 (如果内容非空且以空格开始)
            if (firstNonSpaceIndex > contentStart) {
                issues.add(
                    Issue(
                        lineOffset + contentStart,       // 空格的起始索引
                        lineOffset + firstNonSpaceIndex,   // 空格的结束索引
                        RuleIds.INLINE_CODE_SURROUNDING_SPACES
                    )
                )
            }

            var lastNonSpaceIndex = contentEnd - 1
            while (lastNonSpaceIndex >= firstNonSpaceIndex && line[lastNonSpaceIndex].isWhitespace()) {
                lastNonSpaceIndex--
            }
            val trailingSpaceStartIndex = lastNonSpaceIndex + 1
            // 检查尾随空格 (如果内容非空且以空格结束)
            // 同时避免对仅包含单个空格的内容 " " 重复报告 (已报告前导空格)
            if (trailingSpaceStartIndex < contentEnd) {
                if (firstNonSpaceIndex != contentEnd || contentStart != trailingSpaceStartIndex) {
                    issues.add(
                        Issue(
                            lineOffset + trailingSpaceStartIndex,
                            lineOffset + contentEnd,
                            RuleIds.INLINE_CODE_SURROUNDING_SPACES
                        )
                    )
                }
            }
        }
    }


    /**
     * 在非行内代码的文本段内，检查特定的行内规则。
     * @param segment 要检查的文本片段
     * @param segmentOffset 片段在全文中的起始偏移量
     * @param issues 问题列表
     */
    private fun checkSegmentInlineRules(
        segment: String,
        segmentOffset: Int,
        issues: MutableList<Issue>
    ) {
        // 检查1：链接/图片文本内容前后是否有空格 (MD008)
        LINK_TEXT_BRACKETS.findAll(segment).forEach { match ->
            // 简单启发式判断：检查 `]` 后面是否紧跟着 `(` 或 `[`，这通常表示链接或引用式链接
            // 同时考虑 `!` 前缀表示图片
            val nextCharIndex = match.range.last + 1
            val isLikelyLink =
                nextCharIndex < segment.length && (segment[nextCharIndex] == '(' || segment[nextCharIndex] == '[')
            val prevCharIndex = match.range.first - 1
            val isLikelyImage = prevCharIndex >= 0 && segment[prevCharIndex] == '!' && !isEscaped(
                prevCharIndex,
                segment
            ) // 检查未转义的!

            if (isLikelyLink || isLikelyImage) {
                val textGroup = match.groups[1] // 获取括号内的文本组
                if (textGroup != null) {
                    val linkText = textGroup.value

                    val absoluteTextStart = segmentOffset + match.range.first + 1
                    val absoluteTextEnd = absoluteTextStart + linkText.length

                    // 检查文本内容是否非空
                    if (linkText.isNotEmpty()) {
                        // 检查前导空格
                        var firstNonSpaceInText = 0
                        while (firstNonSpaceInText < linkText.length && linkText[firstNonSpaceInText].isWhitespace()) {
                            firstNonSpaceInText++
                        }

                        if (firstNonSpaceInText > 0) {
                            issues.add(
                                Issue(
                                    absoluteTextStart,
                                    absoluteTextStart + firstNonSpaceInText,
                                    RuleIds.LINK_TEXT_SURROUNDING_SPACES
                                )
                            )
                        }

                        var lastNonSpaceInText = linkText.length - 1
                        while (lastNonSpaceInText >= firstNonSpaceInText && linkText[lastNonSpaceInText].isWhitespace()) {
                            lastNonSpaceInText--
                        }

                        val trailingSpaceStartInText = lastNonSpaceInText + 1
                        if (trailingSpaceStartInText < linkText.length) {
                            if (firstNonSpaceInText != linkText.length || trailingSpaceStartInText != 0) {
                                issues.add(
                                    Issue(
                                        absoluteTextStart + trailingSpaceStartInText,
                                        absoluteTextEnd,
                                        RuleIds.LINK_TEXT_SURROUNDING_SPACES
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }


        // 检查2：此段内是否存在使用全角括号的链接 [text]（url）(MD005)
        CHINESE_LINK_PATTERN.findAll(segment).forEach { match ->
            val fullMatchText = match.value
            val openParenIndexInMatch = fullMatchText.indexOf('（')
            val closeParenIndexInMatch = fullMatchText.indexOf('）')

            if (openParenIndexInMatch != -1) {
                issues.add(
                    Issue(
                        segmentOffset + match.range.first + openParenIndexInMatch,
                        segmentOffset + match.range.first + openParenIndexInMatch + 1,
                        RuleIds.LINK_CHINESE_PARENTHESES
                    )
                )
            }
            if (closeParenIndexInMatch != -1) {
                issues.add(
                    Issue(
                        segmentOffset + match.range.first + closeParenIndexInMatch,
                        segmentOffset + match.range.first + closeParenIndexInMatch + 1,
                        RuleIds.LINK_CHINESE_PARENTHESES
                    )
                )
            }
        }

        // 检查3：此段内是否存在以全角感叹号开头的图片链接 ！[alt](url) (MD006)
        CHINESE_EXCLAMATION_IMAGE_PATTERN.findAll(segment).forEach { match ->
            val exclamationIndexInMatch = match.value.indexOf('！')
            if (exclamationIndexInMatch != -1) {
                issues.add(
                    Issue(
                        segmentOffset + match.range.first + exclamationIndexInMatch,
                        segmentOffset + match.range.first + exclamationIndexInMatch + 1,
                        RuleIds.IMAGE_CHINESE_EXCLAMATION
                    )
                )
            }
        }
    }


    /**
     * 辅助函数：从 startIndex 开始查找行内的下一个行内代码片段 (`...`)。
     * 会考虑反引号数量匹配和转义。
     * @return Triple(代码片段起始索引(含), 代码片段结束索引(不含), 反引号数量)；如果未找到则返回 null。
     */
    private fun findNextInlineCodeSpan(line: String, startIndex: Int): Triple<Int, Int, Int>? {
        var i = startIndex
        while (i < line.length) {
            if (line[i] == '`' && !isEscaped(i, line)) {
                var tickCount = 1
                while (i + tickCount < line.length && line[i + tickCount] == '`') {
                    tickCount++
                }

                val endTickIndex = findClosingBackticks(line, i + tickCount, tickCount)
                if (endTickIndex != -1) {
                    // 找到了匹配的闭合反引号
                    return Triple(i, endTickIndex + tickCount, tickCount)
                } else {
                    // 未找到闭合反引号，跳过当前的起始反引号序列
                    i += tickCount // 移动 i 到当前反引号序列之后
                    // 注意：这里没有 `continue`，下一轮循环会从 `i` 的新位置开始
                }
            } else {
                i++ // 非反引号或转义的反引号，继续查找
            }
        }
        return null // 未找到更多行内代码片段
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
            if (text[i] == '`' && !isEscaped(i, text)) {
                var matchCount = 1
                while (i + matchCount < text.length && text[i + matchCount] == '`') {
                    matchCount++
                }
                if (matchCount == count) {
                    return i // 找到匹配数量的闭合序列，返回其起始索引
                }
                // 数量不匹配，跳过这个序列继续搜索
                i += matchCount // 移动 i 到当前序列之后
                // 注意：这里没有 `continue`
            } else {
                i++ // 非反引号或转义的反引号，继续搜索
            }
        }
        return -1 // 未找到
    }

    /**
     * 工具函数：检查给定索引处的字符是否被反斜杠转义了。
     */
    private fun isEscaped(index: Int, text: String): Boolean {
        if (index <= 0) return false
        var backslashCount = 0
        var i = index - 1
        while (i >= 0 && text[i] == '\\') {
            backslashCount++
            i--
        }
        return backslashCount % 2 == 1 // 奇数个反斜杠表示转义
    }
}
