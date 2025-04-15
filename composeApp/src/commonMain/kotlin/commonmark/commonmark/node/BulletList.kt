package commonmark.commonmark.node

/**
 * A bullet list, e.g.:
 * <pre>
 * - One
 * - Two
 * - Three
 * </pre>
 * <p>
 * The children are {@link ListItem} blocks, which contain other blocks (or nested lists).
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#list-items">CommonMark Spec: List items</a>
 */
class BulletList : ListBlock() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}