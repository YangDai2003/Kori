package commonmark.commonmark.node

/**
 * Inline code span, e.g.:
 * <pre>
 * Some `inline code`
 * </pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#code-spans">CommonMark Spec</a>
 */
class Code(private var literal: String = "") : Node() {

    /**
     * @return the literal text in the code span (note that it's not necessarily the raw text between tildes,
     * e.g. when spaces are stripped)
     */
    fun getLiteral(): String {
        return literal
    }

    fun setLiteral(literal: String) {
        this.literal = literal
    }

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}