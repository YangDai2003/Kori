package kmark.parser.sequentialparsers.impl

import kmark.flavours.gfm.GFMElementTypes
import kmark.flavours.gfm.GFMTokenTypes
import kmark.parser.sequentialparsers.RangesListBuilder
import kmark.parser.sequentialparsers.SequentialParser
import kmark.parser.sequentialparsers.TokensCache

class MathParser : SequentialParser {
    override fun parse(
        tokens: TokensCache,
        rangesToGlue: List<IntRange>
    ): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == GFMTokenTypes.DOLLAR) {

                val endIterator = findOfSize(iterator.advance(), iterator.length)

                if (endIterator != null) {
                    if (iterator.length == 1) {
                        result.withNode(
                            SequentialParser.Node(
                                iterator.index..endIterator.index + 1,
                                GFMElementTypes.INLINE_MATH
                            )
                        )
                    } else {
                        result.withNode(
                            SequentialParser.Node(
                                iterator.index..endIterator.index + 1,
                                GFMElementTypes.BLOCK_MATH
                            )
                        )
                    }
                    iterator = endIterator.advance()
                    continue
                }
            }
            delegateIndices.put(iterator.index)
            iterator = iterator.advance()
        }

        return result.withFurtherProcessing(delegateIndices.get())
    }

    private fun findOfSize(it: TokensCache.Iterator, length: Int): TokensCache.Iterator? {
        var iterator = it
        while (iterator.type != null) {
            if (iterator.type == GFMTokenTypes.DOLLAR) {
                if (iterator.length == length) {
                    return iterator
                }
            }

            iterator = iterator.advance()
        }
        return null
    }
}
