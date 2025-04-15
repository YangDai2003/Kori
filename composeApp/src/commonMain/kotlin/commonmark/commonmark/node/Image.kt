package commonmark.commonmark.node

/**
 * An image, e.g.:
 * <pre>
 * ![foo](/url "title")
 * </pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#images">CommonMark Spec</a>
 */
class Image(
    private var destination: String = "",
    private var title: String = ""
) : Node() {

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