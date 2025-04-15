package commonmark.commonmark.node

/**
 * Strong emphasis, e.g.:
 * <pre><code>
 * Some **strong emphasis** or __strong emphasis__
 * </code></pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#emphasis-and-strong-emphasis">CommonMark Spec: Emphasis and strong emphasis</a>
 */
class StrongEmphasis(private var delimiter: String = "") : Node(), Delimited {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    override fun getOpeningDelimiter(): String = delimiter

    override fun getClosingDelimiter(): String = delimiter

    fun setDelimiter(delimiter: String) {
        this.delimiter = delimiter
    }
}