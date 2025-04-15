package commonmark.commonmark.node

/**
 * A link with a destination and an optional title; the link text is in child nodes.
 * <p>
 * Example for an inline link in a CommonMark document:
 * <pre><code>
 * [link](/uri "title")
 * </code></pre>
 * <p>
 * The corresponding Link node would look like this:
 * <ul>
 * <li>{@link #getDestination()} returns {@code "/uri"}
 * <li>{@link #getTitle()} returns {@code "title"}
 * <li>A {@link Text} child node with {@link Text#getLiteral() getLiteral} that returns {@code "link"}</li>
 * </ul>
 * <p>
 * Note that the text in the link can contain inline formatting, so it could also contain an {@link Image} or
 * {@link Emphasis}, etc.
 *
 * @see <a href="http://spec.commonmark.org/0.31.2/#links">CommonMark Spec</a>
 */
class Link(
    private var destination: String = "",
    private var title: String = ""
): Node() {

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    fun getDestination(): String = destination
    fun setDestination(destination: String) {
        this.destination = destination
    }

    fun getTitle(): String = title
    fun setTitle(title: String) {
        this.title = title
    }

    override fun toStringAttributes(): String {
        return "destination=$destination, title=$title"
    }
}