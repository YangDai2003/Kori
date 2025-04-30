package kmark.parser

import kmark.ExperimentalApi
import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.MarkdownParsingException
import kmark.MarkdownTokenTypes
import kmark.ast.ASTNode
import kmark.ast.ASTNodeBuilder
import kmark.ast.CompositeASTNode
import kmark.ast.LeafASTNode
import kmark.flavours.MarkdownFlavourDescriptor
import kmark.flavours.gfm.GFMTokenTypes
import kmark.parser.sequentialparsers.LexerBasedTokensCache
import kmark.parser.sequentialparsers.SequentialParser
import kmark.parser.sequentialparsers.SequentialParserUtil

class MarkdownParser @ExperimentalApi constructor(
    private val flavour: MarkdownFlavourDescriptor,
    private val assertionsEnabled: Boolean = true,
    private val cancellationToken: CancellationToken = CancellationToken.NonCancellable
) {
    constructor(flavour: MarkdownFlavourDescriptor) : this(flavour, true)

    @OptIn(ExperimentalApi::class)
    constructor(
        flavour: MarkdownFlavourDescriptor,
        assertionsEnabled: Boolean
    ) : this(flavour, assertionsEnabled, CancellationToken.NonCancellable)

    fun buildMarkdownTreeFromString(text: String): ASTNode {
        return parse(MarkdownElementTypes.MARKDOWN_FILE, text, true)
    }

    fun parse(root: IElementType, text: String, parseInlines: Boolean = true): ASTNode {
        return try {
            doParse(root, text, parseInlines)
        } catch (e: MarkdownParsingException) {
            if (assertionsEnabled)
                throw e
            else
                topLevelFallback(root, text)
        }
    }

    fun parseInline(root: IElementType, text: CharSequence, textStart: Int, textEnd: Int): ASTNode {
        return try {
            doParseInline(root, text, textStart, textEnd)
        } catch (e: MarkdownParsingException) {
            if (assertionsEnabled)
                throw e
            else
                inlineFallback(root, textStart, textEnd)
        }
    }

    @OptIn(ExperimentalApi::class)
    private fun doParse(root: IElementType, text: String, parseInlines: Boolean = true): ASTNode {
        val productionHolder = ProductionHolder()
        val markerProcessor = flavour.markerProcessorFactory.createMarkerProcessor(productionHolder)

        val rootMarker = productionHolder.mark()

        val textHolder = LookaheadText(text)
        var pos: LookaheadText.Position? = textHolder.startPosition
        while (pos != null) {
            cancellationToken.checkCancelled()
            productionHolder.updatePosition(pos.offset)
            pos = markerProcessor.processPosition(pos)
        }

        productionHolder.updatePosition(text.length)
        markerProcessor.flushMarkers()

        rootMarker.done(root)

        val nodeBuilder = if (parseInlines) {
            InlineExpandingASTNodeBuilder(text)
        } else {
            ASTNodeBuilder(text)
        }

        val builder = TopLevelBuilder(nodeBuilder)

        return builder.buildTree(productionHolder.production)
    }

    @OptIn(ExperimentalApi::class)
    private fun doParseInline(
        root: IElementType,
        text: CharSequence,
        textStart: Int,
        textEnd: Int
    ): ASTNode {
        val lexer = flavour.createInlinesLexer()
        lexer.start(text, textStart, textEnd)
        val tokensCache = LexerBasedTokensCache(lexer)

        val wholeRange = 0..tokensCache.filteredTokens.size
        val nodes = flavour.sequentialParserManager.runParsingSequence(
            tokensCache = tokensCache,
            rangesToParse = SequentialParserUtil.filterBlockquotes(tokensCache, wholeRange),
            cancellationToken = cancellationToken
        )

        val builder =
            InlineBuilder(ASTNodeBuilder(text, cancellationToken), tokensCache, cancellationToken)
        return builder.buildTree(nodes + listOf(SequentialParser.Node(wholeRange, root)))
    }

    private fun topLevelFallback(root: IElementType, text: String): ASTNode {
        return CompositeASTNode(
            root, listOf(inlineFallback(MarkdownElementTypes.PARAGRAPH, 0, text.length))
        )
    }

    private fun inlineFallback(root: IElementType, textStart: Int, textEnd: Int): ASTNode {
        return CompositeASTNode(
            root,
            listOf(LeafASTNode(MarkdownTokenTypes.TEXT, textStart, textEnd))
        )
    }

    private inner class InlineExpandingASTNodeBuilder(text: CharSequence) : ASTNodeBuilder(text) {
        override fun createLeafNodes(
            type: IElementType,
            startOffset: Int,
            endOffset: Int
        ): List<ASTNode> {
            return when (type) {
                MarkdownElementTypes.PARAGRAPH,
                MarkdownTokenTypes.ATX_CONTENT,
                MarkdownTokenTypes.SETEXT_CONTENT,
                GFMTokenTypes.CELL ->
                    listOf(parseInline(type, text, startOffset, endOffset))

                else ->
                    super.createLeafNodes(type, startOffset, endOffset)
            }
        }
    }
}
