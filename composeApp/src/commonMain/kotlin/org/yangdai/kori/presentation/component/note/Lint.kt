package org.yangdai.kori.presentation.component.note

data class Issue(
    val startIndex: Int,    // 问题起始索引 (包含)
    val endIndex: Int,      // 问题结束索引 (不包含)
    val ruleId: String     // 对应的规则 ID
)

interface Lint {
    fun validate(text: String): List<Issue>
}