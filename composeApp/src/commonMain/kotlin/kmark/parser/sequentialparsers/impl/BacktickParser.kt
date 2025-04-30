package kmark.parser.sequentialparsers.impl

import kmark.MarkdownElementTypes
import kmark.MarkdownTokenTypes
import kmark.parser.sequentialparsers.RangesListBuilder
import kmark.parser.sequentialparsers.SequentialParser
import kmark.parser.sequentialparsers.TokensCache

class BacktickParser : SequentialParser {
    override fun parse(
        tokens: TokensCache,
        rangesToGlue: List<IntRange>
    ): SequentialParser.ParsingResult {
        val result = SequentialParser.ParsingResultBuilder()
        val delegateIndices = RangesListBuilder()
        var iterator: TokensCache.Iterator = tokens.RangesListIterator(rangesToGlue)

        while (iterator.type != null) {
            if (iterator.type == MarkdownTokenTypes.BACKTICK || iterator.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {

                val endIterator = findOfSize(iterator.advance(), getLength(iterator, true))

                if (endIterator != null) {
                    result.withNode(
                        SequentialParser.Node(
                            iterator.index..endIterator.index + 1,
                            MarkdownElementTypes.CODE_SPAN
                        )
                    )
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
            if (iterator.type == MarkdownTokenTypes.BACKTICK || iterator.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {
                if (getLength(iterator, false) == length) {
                    return iterator
                }
            }

            iterator = iterator.advance()
        }
        return null
    }


    private fun getLength(info: TokensCache.Iterator, canEscape: Boolean): Int {
        var toSubtract = 0
        if (info.type == MarkdownTokenTypes.ESCAPED_BACKTICKS) {
            toSubtract = if (canEscape) {
                2
            } else {
                1
            }
        }

        return info.length - toSubtract
    }
}
