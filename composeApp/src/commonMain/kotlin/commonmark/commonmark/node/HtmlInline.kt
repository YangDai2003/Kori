package commonmark.commonmark.node

/**
 * Inline HTML element.
 *
 * @see <a href="http://spec.commonmark.org/0.31.2/#raw-html">CommonMark Spec</a>
 */
class HtmlInline(private var literal: String) : Node(){

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    fun getLiteral(): String = literal

    fun setLiteral(literal: String) {
        this.literal = literal
    }
}