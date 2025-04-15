package commonmark.commonmark.node

/**
 * An indented code block, e.g.:
 * <pre><code>
 * Code follows:
 *
 *     foo
 *     bar
 * </code></pre>
 * <p>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#indented-code-blocks">CommonMark Spec</a>
 */
class IndentedCodeBlock: Block() {

    private var literal: String = ""

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    fun getLiteral(): String = literal

    fun setLiteral(literal: String) {
        this.literal = literal
    }
}