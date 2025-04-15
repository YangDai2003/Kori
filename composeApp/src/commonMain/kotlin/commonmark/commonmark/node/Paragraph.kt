package commonmark.commonmark.node

/**
 * A paragraph block, contains inline nodes such as {@link Text}
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#paragraphs">CommonMark Spec</a>
 */
class Paragraph : Block(){

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}