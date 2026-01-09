package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun DiffTextPreview() {
    Surface {
        DiffText(
            text = """
                This is a test document.
                It has multiple lines.
                Some lines will be added.
                aaaa
                bbb
                We are testing the diff functionality.
                This line is completely new.
                Another line.
                """.trimIndent(),
            oldText = """
                This is a test document.
                It has multiple lines.
                Some lines will be deleted.
                We are testing the diff functionality.
                Some different content.
                ddd
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
 * @param addedColor 新增内容的颜色
 * @param removedColor 删除内容的颜色
 * @param modifiedColor 修改内容的颜色
 */
@Composable
fun DiffText(
    text: String,
    oldText: String,
    addedColor: Color = Color(0xFF9CFF9C), // 浅绿色，类似IDEA和VSCode的添加颜色
    removedColor: Color = Color(0xFFFFCDCD), // 浅红色，类似IDEA和VSCode的删除颜色
    modifiedColor: Color = Color(0xFFFFFFCC), // 浅黄色，类似IDEA和VSCode的修改颜色
    modifier: Modifier = Modifier
) {
    val diffResult = remember(text, oldText) { calculateTwoWayDiff(oldText, text) }

    Column(modifier = modifier) {
        Text(
            text = "Original vs New:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            // 原始文本列
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                items(diffResult.oldTextItems) { item ->
                    val backgroundColor = when (item.type) {
                        DiffType.REMOVED -> removedColor.copy(alpha = 0.3f)
                        DiffType.MODIFIED_OLD -> modifiedColor.copy(alpha = 0.3f)
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
                        if (item.content.isNotEmpty() || item.type == DiffType.REMOVED || item.type == DiffType.MODIFIED_OLD) {
                            Text(
                                text = "${item.originalLineIndex + 1}", // 使用原始行号
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .align(Alignment.Top),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // 当左侧无内容时，显示空白占位符以保持对齐
                            Text(
                                text = "",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .align(Alignment.Top),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 单行文本，支持水平滚动
                        Text(
                            text = item.content,
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
            }

            // 新文本列
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            ) {
                items(diffResult.newTextItems) { item ->
                    val backgroundColor = when (item.type) {
                        DiffType.ADDED -> addedColor.copy(alpha = 0.3f)
                        DiffType.MODIFIED_NEW -> modifiedColor.copy(alpha = 0.3f)
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
                        if (item.content.isNotEmpty() || item.type == DiffType.ADDED || item.type == DiffType.MODIFIED_NEW) {
                            Text(
                                text = "${item.originalLineIndex + 1}", // 使用原始行号
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .align(Alignment.Top),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            // 当右侧无内容时，显示空白占位符以保持对齐
                            Text(
                                text = "",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .align(Alignment.Top),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 单行文本，支持水平滚动
                        Text(
                            text = item.content,
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(rememberScrollState()),
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                        )
                    }
                }
            }
        }
    }
}

// 定义差异类型
enum class DiffType {
    ADDED, REMOVED, UNCHANGED, MODIFIED_OLD, MODIFIED_NEW
}

// 表示差异项目
data class DiffItem(
    val content: String,
    val type: DiffType,
    val originalLineIndex: Int = -1 // 记录原始行号
)

// 存储两个文本的差异结果
data class TwoWayDiffResult(
    val oldTextItems: List<DiffItem>,
    val newTextItems: List<DiffItem>
)

/**
 * 计算两个文本之间的双向差异 - 确保保持原始行数，不增加行数
 */
fun calculateTwoWayDiff(oldText: String, newText: String): TwoWayDiffResult {
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
                if (isSimilar(oldLines[oldIdx], newLines[newIdx])) {
                    oldTextItems.add(DiffItem(oldLines[oldIdx], DiffType.MODIFIED_OLD, oldIdx))
                    newTextItems.add(DiffItem(newLines[newIdx], DiffType.MODIFIED_NEW, newIdx))
                } else {
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
fun isSimilar(line1: String, line2: String): Boolean {
    if (line1.isEmpty() || line2.isEmpty()) {
        return false
    }

    // 如果完全相同，已经在上面处理过了
    if (line1 == line2) {
        return false
    }

    // 使用最长公共子序列的长度作为相似度指标
    val lcsLength = longestCommonSubsequenceLength(line1, line2)
    val maxLength = kotlin.math.max(line1.length, line2.length)

    // 如果公共部分超过较短字符串的一定比例（比如50%），则认为是相似的
    return lcsLength.toDouble() / maxLength > 0.5
}

/**
 * 计算两个字符串的最长公共子序列长度
 */
fun longestCommonSubsequenceLength(s1: String, s2: String): Int {
    val m = s1.length
    val n = s2.length

    val dp = Array(m + 1) { IntArray(n + 1) }

    for (i in 1..m) {
        for (j in 1..n) {
            if (s1[i - 1] == s2[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1] + 1
            } else {
                dp[i][j] = kotlin.math.max(dp[i - 1][j], dp[i][j - 1])
            }
        }
    }

    return dp[m][n]
}