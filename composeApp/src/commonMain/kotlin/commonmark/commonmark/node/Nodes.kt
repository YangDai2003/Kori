package commonmark.commonmark.node

/**
 * Utility class for working with multiple [Node]s.
 *
 * @since 0.16.0
 */
object Nodes {
    /**
     * The nodes between (not including) start and end.
     */
    fun between(start: Node, end: Node): Iterable<Node> {
        return NodeIterable(start.getNext(), end)
    }

    private class NodeIterable(private val first: Node?, private val end: Node) : Iterable<Node> {
        override fun iterator(): Iterator<Node> {
            return NodeIterator(first, end)
        }
    }

    private class NodeIterator(first: Node?, private val end: Node) : MutableIterator<Node> {
        private var node: Node? = first

        override fun hasNext(): Boolean {
            return node != null && node != end
        }

        override fun next(): Node {
            val result = node
            node = node?.getNext()
            return result!!
        }

        override fun remove() {
            throw UnsupportedOperationException("remove")
        }
    }
}