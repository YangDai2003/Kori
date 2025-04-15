package commonmark.commonmark.node

/**
 * The root block of a document, containing the top-level blocks.
 */
class Document: Block() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}