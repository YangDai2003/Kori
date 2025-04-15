package commonmark.commonmark.node

/**
 * Node visitor.
 *
 *
 * Implementations should subclass [AbstractVisitor] instead of implementing this directly.
 */
interface Visitor {
    fun visit(blockQuote: BlockQuote)

    fun visit(bulletList: BulletList)

    fun visit(code: Code)

    fun visit(document: Document)

    fun visit(emphasis: Emphasis)

    fun visit(fencedCodeBlock: FencedCodeBlock)

    fun visit(hardLineBreak: HardLineBreak)

    fun visit(heading: Heading)

    fun visit(thematicBreak: ThematicBreak)

    fun visit(htmlInline: HtmlInline)

    fun visit(htmlBlock: HtmlBlock)

    fun visit(image: Image)

    fun visit(indentedCodeBlock: IndentedCodeBlock)

    fun visit(link: Link)

    fun visit(listItem: ListItem)

    fun visit(orderedList: OrderedList)

    fun visit(paragraph: Paragraph)

    fun visit(softLineBreak: SoftLineBreak)

    fun visit(strongEmphasis: StrongEmphasis)

    fun visit(text: Text)

    fun visit(linkReferenceDefinition: LinkReferenceDefinition)

    fun visit(customBlock: CustomBlock)

    fun visit(customNode: CustomNode)
}
