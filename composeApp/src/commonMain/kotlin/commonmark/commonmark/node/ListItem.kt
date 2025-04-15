package commonmark.commonmark.node

/**
 * A child of a {@link ListBlock}, containing other blocks (e.g. {@link Paragraph}, other lists, etc).
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#list-items">CommonMark Spec: List items</a>
 */
class ListItem : Block(){

    private var markerIndent : Int = 0
    private var contentIndent : Int = 0

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    /**
     * Returns the indent of the marker such as "-" or "1." in columns (spaces or tab stop of 4) if available, or null
     * otherwise.
     * <p>
     * Some examples and their marker indent:
     * <pre>- Foo</pre>
     * Marker indent: 0
     * <pre> - Foo</pre>
     * Marker indent: 1
     * <pre>  1. Foo</pre>
     * Marker indent: 2
     */
    fun getMarkerIndent(): Int = markerIndent
    fun setMarkerIndent(markerIndent: Int) {
        require(markerIndent >= 0) { "markerIndent needs to be >= 0" }
        this.markerIndent = markerIndent
    }

    /**
     * Returns the indent of the content in columns (spaces or tab stop of 4) if available, or null otherwise.
     * The content indent is counted from the beginning of the line and includes the marker on the first line.
     * <p>
     * Some examples and their content indent:
     * <pre>- Foo</pre>
     * Content indent: 2
     * <pre> - Foo</pre>
     * Content indent: 3
     * <pre>  1. Foo</pre>
     * Content indent: 5
     * <p>
     * Note that subsequent lines in the same list item need to be indented by at least the content indent to be counted
     * as part of the list item.
     */
    fun getContentIndent(): Int = contentIndent
    fun setContentIndent(contentIndent: Int) {
        require(contentIndent >= 0) { "contentIndent needs to be >= 0" }
        this.contentIndent = contentIndent
    }
}