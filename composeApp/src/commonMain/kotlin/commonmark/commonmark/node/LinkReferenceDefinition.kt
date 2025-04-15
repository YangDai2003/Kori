package commonmark.commonmark.node

/**
 * A link reference definition, e.g.:
 * <pre><code>
 * [foo]: /url "title"
 * </code></pre>
 * <p>
 * They can be referenced anywhere else in the document to produce a link using <code>[foo]</code>. The definitions
 * themselves are usually not rendered in the final output.
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#link-reference-definition">CommonMark Spec</a>
 */
class LinkReferenceDefinition(
    private var label: String = "",
    private var destination: String = "",
    private var title: String = ""
) : Block() {

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }

    fun getLabel(): String = label
    fun setLabel(label: String) {
        this.label = label
    }

    fun getDestination(): String = destination
    fun setDestination(destination: String) {
        this.destination = destination
    }

    fun getTitle(): String = title
    fun setTitle(title: String) {
        this.title = title
    }
}