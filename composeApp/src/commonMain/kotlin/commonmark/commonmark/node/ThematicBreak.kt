package commonmark.commonmark.node

/**
 * A thematic break, e.g. between text:
 * <pre>
 * Some text
 *
 * ___
 *
 * Some other text.
 * </pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#thematic-breaks">CommonMark Spec</a>
 */
class ThematicBreak(private var literal: String = "") : Block() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    /**
     * @return the source literal that represents this node, if available
     */
    fun getLiteral(): String = literal

    fun setLiteral(literal: String) {
        this.literal = literal
    }
}