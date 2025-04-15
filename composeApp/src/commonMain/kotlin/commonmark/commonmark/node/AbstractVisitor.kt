package commonmark.commonmark.node

/**
 * Abstract visitor that visits all children by default.
 * <p>
 * Can be used to only process certain nodes. If you override a method and want visiting to descend into children,
 * call {@link #visitChildren}.
 */
abstract class AbstractVisitor : Visitor {

    override fun visit(blockQuote: BlockQuote) {
        visitChildren(blockQuote)
    }

    override fun visit(bulletList: BulletList) {
        visitChildren(bulletList)
    }

    override fun visit(code: Code) {
        visitChildren(code)
    }

    override fun visit(document: Document) {
        visitChildren(document)
    }

    override fun visit(emphasis: Emphasis) {
        visitChildren(emphasis)
    }

    override fun visit(fencedCodeBlock: FencedCodeBlock) {
        visitChildren(fencedCodeBlock)
    }

    override fun visit(hardLineBreak: HardLineBreak) {
        visitChildren(hardLineBreak)
    }

    override fun visit(heading: Heading) {
        visitChildren(heading)
    }

    override fun visit(thematicBreak: ThematicBreak) {
        visitChildren(thematicBreak)
    }

    override fun visit(htmlInline: HtmlInline) {
        visitChildren(htmlInline)
    }

    override fun visit(htmlBlock: HtmlBlock) {
        visitChildren(htmlBlock)
    }

    override fun visit(image: Image) {
        visitChildren(image)
    }

    override fun visit(indentedCodeBlock: IndentedCodeBlock) {
        visitChildren(indentedCodeBlock)
    }

    override fun visit(link: Link) {
        visitChildren(link)
    }

    override fun visit(listItem: ListItem) {
        visitChildren(listItem)
    }

    override fun visit(orderedList: OrderedList) {
        visitChildren(orderedList)
    }

    override fun visit(paragraph: Paragraph) {
        visitChildren(paragraph)
    }

    override fun visit(softLineBreak: SoftLineBreak) {
        visitChildren(softLineBreak)
    }

    override fun visit(strongEmphasis: StrongEmphasis) {
        visitChildren(strongEmphasis)
    }

    override fun visit(text: Text) {
        visitChildren(text)
    }

    override fun visit(linkReferenceDefinition: LinkReferenceDefinition) {
        visitChildren(linkReferenceDefinition)
    }

    override fun visit(customBlock: CustomBlock) {
        visitChildren(customBlock)
    }

    override fun visit(customNode: CustomNode) {
        visitChildren(customNode)
    }


    /**
     * Visit the child nodes.
     *
     * @param parent the parent node whose children should be visited
     */
    protected fun visitChildren(parent: Node) {
        var node = parent.getFirstChild()
        while (node != null) {
            // A subclass of this visitor might modify the node, resulting in getNext returning a different node or no
            // node after visiting it. So get the next node before visiting.
            val next = node.getNext()
            node.accept(this)
            node = next
        }
    }
}