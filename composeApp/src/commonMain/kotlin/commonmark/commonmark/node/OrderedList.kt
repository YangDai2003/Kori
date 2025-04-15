package commonmark.commonmark.node

/**
 * An ordered list, e.g.:
 * <pre><code>
 * 1. One
 * 2. Two
 * 3. Three
 * </code></pre>
 * <p>
 * The children are {@link ListItem} blocks, which contain other blocks (or nested lists).
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#list-items">CommonMark Spec: List items</a>
 */
class OrderedList : ListBlock() {

    private var markerDelimiter: String = ""
    private var markerStartNumber: Int = 0

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    /**
     * @return the start number used in the marker, e.g. {@code 1}, if available, or null otherwise
     */
    fun getMarkerStartNumber(): Int = markerStartNumber
    fun setMarkerStartNumber(markerStartNumber: Int) {
        this.markerStartNumber = markerStartNumber
    }

    /**
     * @return the delimiter used in the marker, e.g. {@code .} or {@code )}, if available, or null otherwise
     */
    fun getMarkerDelimiter(): String = markerDelimiter
    fun setMarkerDelimiter(markerDelimiter: String) {
        this.markerDelimiter = markerDelimiter
    }
}
