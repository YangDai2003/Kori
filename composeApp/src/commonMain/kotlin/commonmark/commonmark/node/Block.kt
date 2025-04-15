package commonmark.commonmark.node

/**
 * Block nodes such as paragraphs, list blocks, code blocks etc.
 */
abstract class Block : Node() {

    override fun getParent(): Block {
        return super.getParent() as Block
    }

    override fun setParent(parent: Node?) {
        if (parent != null && parent !is Block) {
            throw IllegalArgumentException("Parent must be a Block")
        }
        super.setParent(parent)
    }
}
