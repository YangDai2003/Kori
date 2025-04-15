package commonmark.commonmark.node

/**
 * A hard line break, e.g.:
 * <pre>
 * line\
 * break
 * </pre>
 * <p>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#hard-line-breaks">CommonMark Spec</a>
 */
class HardLineBreak: Node() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}