package kmark.flavours.gfm

import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.MarkdownTokenTypes
import kmark.ast.ASTNode
import kmark.ast.getParentOfType
import kmark.ast.getTextInNode
import kmark.flavours.commonmark.CommonMarkFlavourDescriptor
import kmark.flavours.gfm.lexer._GFMLexer
import kmark.html.GeneratingProvider
import kmark.html.HtmlGenerator
import kmark.html.SimpleInlineTagProvider
import kmark.html.TrimmingInlineHolderProvider
import kmark.html.URI
import kmark.html.entities.EntityConverter
import kmark.html.makeXssSafeDestination
import kmark.lexer.MarkdownLexer
import kmark.parser.LinkMap
import kmark.parser.MarkerProcessorFactory
import kmark.parser.sequentialparsers.EmphasisLikeParser
import kmark.parser.sequentialparsers.SequentialParser
import kmark.parser.sequentialparsers.SequentialParserManager
import kmark.parser.sequentialparsers.impl.AutolinkParser
import kmark.parser.sequentialparsers.impl.BacktickParser
import kmark.parser.sequentialparsers.impl.EmphStrongDelimiterParser
import kmark.parser.sequentialparsers.impl.ImageParser
import kmark.parser.sequentialparsers.impl.InlineLinkParser
import kmark.parser.sequentialparsers.impl.MathParser
import kmark.parser.sequentialparsers.impl.ReferenceLinkParser

/**
 * GitHub Markdown spec based flavour, to be used as a base for other flavours.
 *
 * @param useSafeLinks `true` if all rendered links should be checked for XSS and `false` otherwise.
 * See [kmark.html.makeXssSafeDestination]
 *
 * @param absolutizeAnchorLinks `true` if anchor links (e.g. `#foo`) should be resolved against `baseURI` and
 * `false` otherwise
 *
 * @param makeHttpsAutoLinks enables use of HTTPS schema for auto links.
 */
open class GFMFlavourDescriptor(
    useSafeLinks: Boolean = false,
    absolutizeAnchorLinks: Boolean = false,
    private val makeHttpsAutoLinks: Boolean = true
) : CommonMarkFlavourDescriptor(useSafeLinks, absolutizeAnchorLinks) {
    override val markerProcessorFactory: MarkerProcessorFactory = GFMMarkerProcessor.Factory

    override fun createInlinesLexer(): MarkdownLexer {
        return MarkdownLexer(_GFMLexer())
    }

    override val sequentialParserManager = object : SequentialParserManager() {
        override fun getParserSequence(): List<SequentialParser> {
            return listOf(
                AutolinkParser(listOf(MarkdownTokenTypes.AUTOLINK, GFMTokenTypes.GFM_AUTOLINK)),
                BacktickParser(),
                MathParser(),
                ImageParser(),
                InlineLinkParser(),
                ReferenceLinkParser(),
                EmphasisLikeParser(
                    EmphStrongDelimiterParser(),
                    StrikeThroughDelimiterParser(),
                    UnderlineDelimiterParser(),
                    HighlightDelimiterParser()
                )
            )
        }
    }

    override fun createHtmlGeneratingProviders(
        linkMap: LinkMap,
        baseURI: URI?
    ): Map<IElementType, GeneratingProvider> {
        return super.createHtmlGeneratingProviders(linkMap, baseURI) + hashMapOf(
            GFMElementTypes.STRIKETHROUGH to SimpleInlineTagProvider("del", 2, -2),

            GFMElementTypes.UNDERLINE to SimpleInlineTagProvider("ins", 2, -2),

            GFMElementTypes.HIGHLIGHT to SimpleInlineTagProvider("mark", 2, -2),

            GFMElementTypes.TABLE to TablesGeneratingProvider(),

            GFMTokenTypes.CELL to TrimmingInlineHolderProvider(),
            MarkdownElementTypes.CODE_SPAN to TableAwareCodeSpanGeneratingProvider(),

            GFMTokenTypes.GFM_AUTOLINK to object : GeneratingProvider {
                override fun processNode(
                    visitor: HtmlGenerator.HtmlGeneratingVisitor,
                    text: String,
                    node: ASTNode
                ) {
                    val linkText = node.getTextInNode(text)

                    // #28: do not render GFM autolinks under link titles
                    // (though it's "OK" according to CommonMark spec)
                    if (node.getParentOfType(
                            MarkdownElementTypes.LINK_LABEL,
                            MarkdownElementTypes.LINK_TEXT
                        ) != null
                    ) {
                        visitor.consumeHtml(linkText)
                        return
                    }

                    // according to GFM_AUTOLINK rule in lexer, link either starts with scheme or with 'www.'
                    val absoluteLink = if (hasSchema(linkText)) linkText else {
                        if (makeHttpsAutoLinks) {
                            "https://$linkText"
                        } else {
                            "http://$linkText"
                        }
                    }

                    val link = EntityConverter.replaceEntities(linkText, true, false)
                    val normalizedDestination =
                        LinkMap.normalizeDestination(absoluteLink, false).let {
                            if (useSafeLinks) makeXssSafeDestination(it) else it
                        }
                    visitor.consumeTagOpen(node, "a", "href=\"$normalizedDestination\"")
                    visitor.consumeHtml(link)
                    visitor.consumeTagClose("a")
                }

                private fun hasSchema(linkText: CharSequence): Boolean {
                    val index = linkText.indexOf('/')
                    if (index == -1) return false
                    return index != 0
                            && index + 1 < linkText.length
                            && linkText[index - 1] == ':'
                            && linkText[index + 1] == '/'
                }
            },

            MarkdownElementTypes.LIST_ITEM to CheckedListItemGeneratingProvider(),

            GFMElementTypes.INLINE_MATH to MathGeneratingProvider(inline = true),
            GFMElementTypes.BLOCK_MATH to MathGeneratingProvider()
        )
    }
}
