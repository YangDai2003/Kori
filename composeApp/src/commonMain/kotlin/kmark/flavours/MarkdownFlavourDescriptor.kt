package kmark.flavours

import kmark.IElementType
import kmark.html.GeneratingProvider
import kmark.html.URI
import kmark.lexer.MarkdownLexer
import kmark.parser.LinkMap
import kmark.parser.MarkerProcessorFactory
import kmark.parser.sequentialparsers.SequentialParserManager

interface MarkdownFlavourDescriptor {
    val markerProcessorFactory: MarkerProcessorFactory

    val sequentialParserManager: SequentialParserManager

    fun createInlinesLexer(): MarkdownLexer

    fun createHtmlGeneratingProviders(
        linkMap: LinkMap,
        baseURI: URI?
    ): Map<IElementType, GeneratingProvider>
}