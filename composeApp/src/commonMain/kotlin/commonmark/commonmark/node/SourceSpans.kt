package commonmark.commonmark.node

/**
 * A list of source spans that can be added to. Takes care of merging adjacent source spans.
 *
 * @since 0.16.0
 */
class SourceSpans private constructor() {
    private var sourceSpans: MutableList<SourceSpan>? = null

    /**
     * 获取源代码范围列表
     */
    fun getSourceSpans(): List<SourceSpan> = sourceSpans ?: emptyList()

    /**
     * 从多个节点中添加所有源代码范围
     */
    fun addAllFrom(nodes: Iterable<Node>) {
        nodes.forEach { node ->
            addAll(node.getSourceSpans())
        }
    }

    /**
     * 添加所有源代码范围，会自动合并相邻的范围
     */
    fun addAll(other: List<SourceSpan>) {
        if (other.isEmpty()) {
            return
        }

        if (sourceSpans == null) {
            sourceSpans = mutableListOf()
        }

        if (sourceSpans!!.isEmpty()) {
            sourceSpans!!.addAll(other)
        } else {
            val lastIndex = sourceSpans!!.size - 1
            val a = sourceSpans!![lastIndex]
            val b = other[0]

            if (a.inputIndex + a.length == b.inputIndex) {
                // 合并相邻的源代码范围
                sourceSpans!![lastIndex] = SourceSpan.of(
                    a.lineIndex,
                    a.columnIndex,
                    a.inputIndex,
                    a.length + b.length
                )
                sourceSpans!!.addAll(other.subList(1, other.size))
            } else {
                sourceSpans!!.addAll(other)
            }
        }
    }

    companion object {
        /**
         * 创建一个空的SourceSpans实例
         */
        fun empty(): SourceSpans = SourceSpans()
    }
}