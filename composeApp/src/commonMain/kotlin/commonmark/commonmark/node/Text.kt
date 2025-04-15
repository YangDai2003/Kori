package commonmark.commonmark.node

/**
 * A text node, e.g. in:
 * <pre>
 * foo *bar*
 * </pre>
 * <p>
 * The <code>foo </code> is a text node, and the <code>bar</code> inside the emphasis is also a text node.
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#textual-content">CommonMark Spec</a>
 */
class Text(private var literal: String = "") : Node() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    fun getLiteral(): String = literal
    fun setLiteral(literal: String) {
        this.literal = literal
    }

    override fun toStringAttributes(): String {
        return "literal=$literal"
    }
}