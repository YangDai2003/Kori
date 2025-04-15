package commonmark.commonmark.node

/**
 * Emphasis, e.g.:
 * <pre>
 * Some *emphasis* or _emphasis_
 * </pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#emphasis-and-strong-emphasis">CommonMark Spec: Emphasis and strong emphasis</a>
 */
class Emphasis(private var delimiter: String = "") : Node(), Delimited {

    fun setDelimiter(delimiter: String) {
        this.delimiter = delimiter
    }

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    override fun getOpeningDelimiter(): String = delimiter

    override fun getClosingDelimiter(): String = delimiter
}