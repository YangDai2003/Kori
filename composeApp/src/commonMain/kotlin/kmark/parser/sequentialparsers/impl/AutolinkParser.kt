package kmark.parser.sequentialparsers.impl

import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.MarkdownTokenTypes
import kmark.parser.sequentialparsers.RangesListBuilder
import kmark.parser.sequentialparsers.SequentialParser
import kmark.parser.sequentialparsers.TokensCache

class AutolinkParser(private val typesAfterLT: List<IElementType>) : SequentialParser {
    override fun parse(
        tokens: TokensCache,
        rangesToGlue: List<IntRange>
    ): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.LT && iterator.rawLookup(1)
                    .let { it != null && it in typesAfterLT }
            ) {
                val start = iterator.index
                while (iterator.type != MarkdownTokenTypes.GT && iterator.type != null) {
                    iterator = iterator.advance()
                }
                if (iterator.type == MarkdownTokenTypes.GT) {
                    result.withNode(
                        SequentialParser.Node(
                            start..iterator.index + 1,
                            MarkdownElementTypes.AUTOLINK
                        )
                    )
                }
            } else {
                delegateIndices.put(iterator.index)
            }
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }
}
