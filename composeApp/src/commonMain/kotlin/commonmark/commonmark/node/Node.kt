package commonmark.commonmark.node

/**
 * The base class of all CommonMark AST nodes ({@link Block} and inlines).
 * <p>
 * A node can have multiple children, and a parent (except for the root node).
 */
abstract class Node {

    private var parent: Node? = null
    private var firstChild: Node? = null
    private var lastChild: Node? = null
    private var prev: Node? = null
    private var next: Node? = null
    private var sourceSpans: MutableList<SourceSpan>? = null

    abstract fun accept(visitor: Visitor)

    fun getNext(): Node? {
        return next
    }

    fun getPrevious(): Node? {
        return prev
    }

    fun getFirstChild(): Node? {
        return firstChild
    }

    fun getLastChild(): Node? {
        return lastChild
    }

    open fun getParent(): Node? {
        return parent
    }

    protected open fun setParent(parent: Node?) {
        this.parent = parent
    }

    fun appendChild(child: Node) {
        child.unlink()
        child.setParent(this)
        if (this.lastChild != null) {
            this.lastChild!!.next = child
            child.prev = this.lastChild
            this.lastChild = child
        } else {
            this.firstChild = child
            this.lastChild = child
        }
    }

    fun prependChild(child: Node) {
        child.unlink()
        child.setParent(this)
        if (this.firstChild != null) {
            this.firstChild!!.prev = child
            child.next = this.firstChild
            this.firstChild = child
        } else {
            this.firstChild = child
            this.lastChild = child
        }
    }

    fun unlink() {
        if (this.prev != null) {
            this.prev!!.next = this.next
        } else if (this.parent != null) {
            this.parent!!.firstChild = this.next
        }
        if (this.next != null) {
            this.next!!.prev = this.prev
        } else if (this.parent != null) {
            this.parent!!.lastChild = this.prev
        }
        this.parent = null
        this.next = null
        this.prev = null
    }

    /**
     * Inserts the {@code sibling} node after {@code this} node.
     */
    fun insertAfter(sibling: Node) {
        sibling.unlink()
        sibling.next = this.next
        if (sibling.next != null) {
            sibling.next!!.prev = sibling
        }
        sibling.prev = this
        this.next = sibling
        sibling.parent = this.parent
        if (sibling.next == null) {
            sibling.parent!!.lastChild = sibling
        }
    }

    /**
     * Inserts the {@code sibling} node before {@code this} node.
     */
    fun insertBefore(sibling: Node) {
        sibling.unlink()
        sibling.prev = this.prev
        if (sibling.prev != null) {
            sibling.prev!!.next = sibling
        }
        sibling.next = this
        this.prev = sibling
        sibling.parent = this.parent
        if (sibling.prev == null) {
            sibling.parent!!.firstChild = sibling
        }
    }

    /**
     * @return the source spans of this node if included by the parser, an empty list otherwise
     * @since 0.16.0
     */
    fun getSourceSpans(): List<SourceSpan> {
        return sourceSpans?.toList() ?: emptyList()
    }

    /**
     * Replace the current source spans with the provided list.
     *
     * @param sourceSpans the new source spans to set
     * @since 0.16.0
     */
    fun setSourceSpans(sourceSpans: List<SourceSpan>) {
        if (sourceSpans.isEmpty()) {
            this.sourceSpans = null
        } else {
            this.sourceSpans = ArrayList(sourceSpans)
        }
    }

    /**
     * Add a source span to the end of the list.
     *
     * @param sourceSpan the source span to add
     * @since 0.16.0
     */
    fun addSourceSpan(sourceSpan: SourceSpan) {
        if (sourceSpans == null) {
            this.sourceSpans = ArrayList()
        }
        this.sourceSpans!!.add(sourceSpan)
    }

    override fun toString(): String {
        return "{" + toStringAttributes() + "}"
    }

    protected open fun toStringAttributes(): String {
        return ""
    }
}
