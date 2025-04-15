package commonmark.commonmark.node

/**
 * A heading, e.g.:
 * <pre>
 * First heading
 * =============
 *
 * ## Another heading
 * </pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#atx-headings">CommonMark Spec: ATX headings</a>
 * @see <a href="https://spec.commonmark.org/0.31.2/#setext-headings">CommonMark Spec: Setext headings</a>
 */
class Heading(private var level: Int): Block() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    fun getLevel(): Int = level

    fun setLevel(level: Int) {
        require(level in 1..6) { "level needs to be between 1 and 6" }
        this.level = level
    }
}