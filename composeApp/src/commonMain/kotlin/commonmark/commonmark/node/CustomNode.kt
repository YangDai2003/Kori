package commonmark.commonmark.node

/**
 * A node that extensions can subclass to define custom nodes (not part of the core specification).
 */
abstract class CustomNode: Node() {

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}