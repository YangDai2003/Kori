package commonmark.commonmark.node

/**
 * HTML block
 *
 * @see <a href="http://spec.commonmark.org/0.31.2/#html-blocks">CommonMark Spec</a>
 */
class HtmlBlock(private var literal: String) : Block() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    fun getLiteral(): String = literal

    fun setLiteral(literal: String) {
        this.literal = literal
    }
}