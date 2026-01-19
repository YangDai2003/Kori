package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

@Preview
@Composable
private fun DiffTextPreview() {
    Surface {
        DiffText(
            text = """
                This is a test document.
                It has multiple lines.
                Some lines will be added.
                中文测试文字
                aaaa
                bbb
                We are testing the diff functionality.
                这是中文行
                This line is completely new.
                Another line.
                """.trimIndent(),
            oldText = """
                This is a test document.
                It has multiple lines.
                Some lines will be deleted.
                中文測試文字
                We are testing the diff functionality.
                Some different content.
                ddd
                这是中文行
                Another line.
                """.trimIndent(),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 对比文本差异的组件，使用两个LazyColumn分别展示新旧文本及其差异
 * 保持原始行数不变，行号只在有内容的行显示
 *
 * @param text 当前文本
 * @param oldText 旧文本（快照）
 * @param addedColor 新增内容的行背景颜色
 * @param removedColor 删除内容的行背景颜色
 * @param addedCharColor 新增字符的高亮颜色
 * @param removedCharColor 删除字符的高亮颜色
 */
@Composable
fun DiffText(
    text: String,
    oldText: String,
    addedColor: Color = Color(0x4D90EE90), // 较淡的绿色，用于整行新增
    removedColor: Color = Color(0x4DC86767), // 较淡的红色，用于整行删除
    addedCharColor: Color = Color(0xFFA6CFB3), // 较深的绿色，用于字符级高亮
    removedCharColor: Color = Color(0xFFFF8A8A), // 较深的红色，用于字符级高亮
    modifier: Modifier = Modifier
) {
    var diffResult by remember(text, oldText) { mutableStateOf<TwoWayDiffResult?>(null) }

    LaunchedEffect(text, oldText) {
        diffResult = null
        diffResult = withContext(Dispatchers.Default) {
            calculateTwoWayDiff(oldText, text)
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (diffResult == null) {
            CircularProgressIndicator()
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                // 原始文本列
                SelectionContainer(Modifier.weight(1f).padding(end = 4.dp)) {
                    LazyColumn {
                        items(diffResult?.oldTextItems ?: emptyList()) { item ->
                            val backgroundColor = when (item.type) {
                                DiffType.REMOVED -> removedColor
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 只有当左侧有内容时才显示行号
                                if (item.content.isNotEmpty() || item.type == DiffType.REMOVED) {
                                    Text(
                                        text = "${item.originalLineIndex + 1}", // 使用原始行号
                                        modifier = Modifier.padding(end = 8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    // 当左侧无内容时，显示空白占位符以保持对齐
                                    Text(
                                        text = "",
                                        modifier = Modifier.padding(end = 8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // 显示高亮文本，突出显示删除的部分
                                HighlightedText(
                                    fullText = item.content,
                                    highlightRanges = item.highlightRanges,
                                    highlightColor = removedCharColor, // 使用较深的红色进行字符级高亮
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(rememberScrollState())
                                )
                            }
                        }
                    }
                }

                // 新文本列
                SelectionContainer(Modifier.weight(1f).padding(start = 4.dp)) {
                    LazyColumn {
                        items(diffResult?.newTextItems ?: emptyList()) { item ->
                            val backgroundColor = when (item.type) {
                                DiffType.ADDED -> addedColor
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 只有当右侧有内容时才显示行号
                                if (item.content.isNotEmpty() || item.type == DiffType.ADDED) {
                                    Text(
                                        text = "${item.originalLineIndex + 1}", // 使用原始行号
                                        modifier = Modifier.padding(end = 8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    // 当右侧无内容时，显示空白占位符以保持对齐
                                    Text(
                                        text = "",
                                        modifier = Modifier.padding(end = 8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // 显示高亮文本，突出显示新增的部分
                                HighlightedText(
                                    fullText = item.content,
                                    highlightRanges = item.highlightRanges,
                                    highlightColor = addedCharColor, // 使用较深的绿色进行字符级高亮
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(rememberScrollState())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 定义差异类型
enum class DiffType {
    ADDED, REMOVED, UNCHANGED
}

// 表示差异项目
data class DiffItem(
    val content: String,
    val type: DiffType,
    val originalLineIndex: Int = -1, // 记录原始行号
    val highlightRanges: List<IntRange> = emptyList() // 记录需要高亮的字符范围
)

// 存储两个文本的差异结果
data class TwoWayDiffResult(
    val oldTextItems: List<DiffItem>,
    val newTextItems: List<DiffItem>
)

/**
 * 高亮显示文本中特定范围的字符
 */
@Composable
private fun HighlightedText(
    fullText: String,
    highlightRanges: List<IntRange>,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val annotatedString = buildAnnotatedString {
        append(fullText)

        highlightRanges.forEach { range ->
            addStyle(
                style = SpanStyle(background = highlightColor),
                start = range.first,
                end = min(range.last + 1, fullText.length)
            )
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Visible,
        style = textStyle
    )
}

/**
 * 计算两个文本之间的双向差异 - 确保保持原始行数，不增加行数
 */
private fun calculateTwoWayDiff(oldText: String, newText: String): TwoWayDiffResult {
    val oldLines = oldText.split("\n")
    val newLines = newText.split("\n")

    // 使用最长公共子序列(LCS)算法来计算差异
    val m = oldLines.size
    val n = newLines.size

    // 创建DP表来计算LCS
    val dp = Array(m + 1) { IntArray(n + 1) }

    // 填充DP表
    for (i in 0..m) {
        for (j in 0..n) {
            if (i == 0 || j == 0) {
                dp[i][j] = 0
            } else if (oldLines[i - 1] == newLines[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1] + 1
            } else {
                dp[i][j] = maxOf(dp[i - 1][j], dp[i][j - 1])
            }
        }
    }

    // 回溯DP表以获取LCS
    var i = m
    var j = n
    val lcs = mutableListOf<Pair<Int, Int>>() // 存储LCS元素的索引对

    while (i > 0 && j > 0) {
        if (oldLines[i - 1] == newLines[j - 1]) {
            lcs.add(Pair(i - 1, j - 1))
            i--
            j--
        } else if (dp[i - 1][j] > dp[i][j - 1]) {
            i--
        } else {
            j--
        }
    }

    lcs.reverse() // 恢复正确顺序

    // 创建结果列表，保持原始行数
    val oldTextItems = mutableListOf<DiffItem>()
    val newTextItems = mutableListOf<DiffItem>()

    var oldIdx = 0
    var newIdx = 0
    var lcsIdx = 0

    // 遍历直到处理完所有行
    while (oldIdx < oldLines.size || newIdx < newLines.size) {
        val isInLcs = lcsIdx < lcs.size &&
                oldIdx < oldLines.size &&
                newIdx < newLines.size &&
                lcs[lcsIdx].first == oldIdx &&
                lcs[lcsIdx].second == newIdx

        if (isInLcs) {
            // 这一行在LCS中，即未更改
            oldTextItems.add(DiffItem(oldLines[oldIdx], DiffType.UNCHANGED, oldIdx))
            newTextItems.add(DiffItem(newLines[newIdx], DiffType.UNCHANGED, newIdx))
            oldIdx++
            newIdx++
            lcsIdx++
        } else {
            // 检查是否是删除
            val isDeletion = oldIdx < oldLines.size &&
                    (lcsIdx >= lcs.size || lcs[lcsIdx].first > oldIdx)

            // 检查是否是新增
            val isAddition = newIdx < newLines.size &&
                    (lcsIdx >= lcs.size || lcs[lcsIdx].second > newIdx)

            if (isDeletion && isAddition) {
                // 这是修改：既有删除又有新增
                // 判断是否为相似行
                if (isSimilar(oldLines[oldIdx], newLines[newIdx])) {
                    // 相似行，计算行内词级差异并生成高亮范围
                    val (oldHighlightRanges, newHighlightRanges) = calculateWordDiff(
                        oldLines[oldIdx],
                        newLines[newIdx]
                    )

                    oldTextItems.add(
                        DiffItem(
                            oldLines[oldIdx],
                            DiffType.REMOVED,
                            oldIdx,
                            oldHighlightRanges
                        )
                    )
                    newTextItems.add(
                        DiffItem(
                            newLines[newIdx],
                            DiffType.ADDED,
                            newIdx,
                            newHighlightRanges
                        )
                    )
                } else {
                    // 不相似行，直接标记整行为新增/删除
                    oldTextItems.add(DiffItem(oldLines[oldIdx], DiffType.REMOVED, oldIdx))
                    newTextItems.add(DiffItem(newLines[newIdx], DiffType.ADDED, newIdx))
                }
                oldIdx++
                newIdx++
            } else if (isDeletion) {
                // 纯删除
                oldTextItems.add(DiffItem(oldLines[oldIdx], DiffType.REMOVED, oldIdx))
                newTextItems.add(DiffItem("", DiffType.UNCHANGED, -1)) // 新文本中无对应行
                oldIdx++
            } else if (isAddition) {
                // 纯新增
                oldTextItems.add(DiffItem("", DiffType.UNCHANGED, -1)) // 旧文本中无对应行
                newTextItems.add(DiffItem(newLines[newIdx], DiffType.ADDED, newIdx))
                newIdx++
            } else {
                // 其他情况，处理LCS中的下一个元素
                oldTextItems.add(DiffItem(oldLines[oldIdx], DiffType.UNCHANGED, oldIdx))
                newTextItems.add(DiffItem(newLines[newIdx], DiffType.UNCHANGED, newIdx))
                oldIdx++
                newIdx++
                lcsIdx++
            }
        }
    }

    return TwoWayDiffResult(oldTextItems, newTextItems)
}

/**
 * 判断两行文本是否相似（用于检测修改）
 * 如果两行有共同子串达到一定比例，则认为它们是相似的
 */
private fun isSimilar(line1: String, line2: String): Boolean {
    if (line1.isEmpty() || line2.isEmpty()) {
        return false
    }

    // 如果完全相同，已经在上面处理过了
    if (line1 == line2) {
        return false
    }

    // 使用最长公共子序列的长度作为相似度指标
    val lcsLength = longestCommonSubsequenceLength(line1, line2)
    val maxLength = max(line1.length, line2.length)

    // 如果公共部分超过较短字符串的一定比例（比如50%），则认为是相似的
    return lcsLength.toDouble() / maxLength > 0.5
}

/**
 * 计算两个字符串的最长公共子序列长度
 */
private fun longestCommonSubsequenceLength(s1: String, s2: String): Int {
    val m = s1.length
    val n = s2.length

    val dp = Array(m + 1) { IntArray(n + 1) }

    for (i in 1..m) {
        for (j in 1..n) {
            if (s1[i - 1] == s2[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1] + 1
            } else {
                dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
            }
        }
    }

    return dp[m][n]
}

/**
 * 计算两行文本之间的词级差异，返回需要高亮的字符范围
 */
private fun calculateWordDiff(
    oldLine: String,
    newLine: String
): Pair<List<IntRange>, List<IntRange>> {
    if (oldLine == newLine) {
        return Pair(emptyList(), emptyList())
    }

    if (oldLine.isEmpty()) {
        return Pair(emptyList(), listOf(IntRange(0, newLine.length - 1)))
    }

    if (newLine.isEmpty()) {
        return Pair(listOf(IntRange(0, oldLine.length - 1)), emptyList())
    }

    // 将行按单词分割
    val oldTokens = tokenizeLine(oldLine)
    val newTokens = tokenizeLine(newLine)

    // 计算单词级别的差异
    val m = oldTokens.size
    val n = newTokens.size

    // 计算最长公共子序列
    val lcsMatrix = Array(m + 1) { IntArray(n + 1) }
    for (i in 1..m) {
        for (j in 1..n) {
            if (oldTokens[i - 1].text == newTokens[j - 1].text) {
                lcsMatrix[i][j] = lcsMatrix[i - 1][j - 1] + 1
            } else {
                lcsMatrix[i][j] = max(lcsMatrix[i - 1][j], lcsMatrix[i][j - 1])
            }
        }
    }

    // 回溯以找到差异
    var i = m
    var j = n
    val oldRemovedTokens = mutableListOf<Token>()
    val newAddedTokens = mutableListOf<Token>()

    while (i > 0 || j > 0) {
        if (i > 0 && j > 0 && oldTokens[i - 1].text == newTokens[j - 1].text) {
            i--
            j--
        } else if (i > 0 && j > 0) {
            if (lcsMatrix[i - 1][j] > lcsMatrix[i][j - 1]) {
                oldRemovedTokens.add(oldTokens[i - 1]) // 从旧行删除
                i--
            } else {
                newAddedTokens.add(newTokens[j - 1]) // 在新行添加
                j--
            }
        } else if (i > 0) {
            oldRemovedTokens.add(oldTokens[i - 1])
            i--
        } else if (j > 0) {
            newAddedTokens.add(newTokens[j - 1])
            j--
        }
    }

    // 将Token位置转换为字符位置范围
    val oldRemovedRanges = tokenToCharRanges(oldRemovedTokens)
    val newAddedRanges = tokenToCharRanges(newAddedTokens)

    return Pair(oldRemovedRanges, newAddedRanges)
}

// Token类用于存储单词及其在原文中的位置
data class Token(val text: String, val startIndex: Int, val endIndex: Int)

/**
 * 将一行文本分解为Token（单词和分隔符），支持多语言包括中文等非空格分隔的语言
 */
private fun tokenizeLine(line: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    val n = line.length

    while (i < n) {
        val start = i

        // 检查当前字符类型
        val char = line[i]

        when {
            // 中文、日文、韩文等表意文字，每个字符作为一个词
            char.isCJKCharacter() -> {
                i++ // 每个汉字/表意文字作为一个单位
            }
            // 英文、数字等字母数字字符，组成一个词
            char.isLetterOrDigit() -> {
                while (i < n && line[i].isLetterOrDigit()) {
                    i++
                }
            }
            // 标点符号、空格等特殊字符，根据是否是空格决定分组
            else -> {
                // 空白字符，作为一个token
                if (char.isWhitespace()) {
                    while (i < n && line[i].isWhitespace()) {
                        i++
                    }
                } else {
                    // 其他符号，单个字符作为一个token
                    i++
                }
            }
        }

        val text = line.substring(start, i)
        tokens.add(Token(text, start, i - 1))
    }

    return tokens
}

/**
 * 检查字符是否为中文、日文、韩文等表意文字
 */
private fun Char.isCJKCharacter(): Boolean =
    (code in 0x4E00..0x9FFF) || // CJK Unified Ideographs
            (code in 0x3400..0x4DBF) || // CJK Extension A
            (code in 0x20000..0x2A6DF) || // CJK Extension B
            (code in 0x2A700..0x2B73F) || // CJK Extension C
            (code in 0x2B740..0x2B81F) || // CJK Extension D
            (code in 0x2B820..0x2CEAF) || // CJK Extension E
            (code in 0x2CEB0..0x2EBEF) || // CJK Extension F
            (code in 0x3040..0x309F) || // Hiragana
            (code in 0x30A0..0x30FF) || // Katakana
            (code in 0xAC00..0xD7AF) || // Hangul Syllables
            (code in 0x1100..0x11FF) || // Hangul Jamo
            (code in 0x3130..0x318F) || // Hangul Compatibility Jamo
            (code in 0xA960..0xA97F) || // Hangul Jamo Extended-A
            (code in 0xD7B0..0xD7FF) || // Hangul Jamo Extended-B
            (code in 0x31F0..0x31FF) || // Katakana Phonetic Extensions
            (code in 0xFF66..0xFF9F) ||  // Halfwidth Katakana
            (code in 0x31A0..0x31BF) ||  // Bopomofo Extensions
            (code in 0x3100..0x312F)     // Bopomofo

/**
 * 将Token列表转换为字符位置范围
 */
private fun tokenToCharRanges(tokens: List<Token>): List<IntRange> {
    return tokens.map { IntRange(it.startIndex, it.endIndex) }.sortedBy { it.first }
}