package commonmark.commonmark.node

/**
 * A block that extensions can subclass to define custom blocks (not part of the core specification).
 */
abstract class CustomBlock: Block() {

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}