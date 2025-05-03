package kmark.flavours.gfm

import kmark.parser.sequentialparsers.DelimiterParser
import kmark.parser.sequentialparsers.SequentialParser
import kmark.parser.sequentialparsers.TokensCache
import kmark.parser.sequentialparsers.impl.EmphStrongDelimiterParser

class HighlightDelimiterParser : DelimiterParser() {
    override fun scan(
        tokens: TokensCache,
        iterator: TokensCache.Iterator,
        delimiters: MutableList<Info>
    ): Int {
        if (iterator.type != GFMTokenTypes.EQUAL) {
            return 0
        }
        var stepsToAdvance = 1
        var rightIterator = iterator
        for (index in 0 until maxAdvance) {
            if (rightIterator.rawLookup(1) != GFMTokenTypes.EQUAL) {
                break
            }
            rightIterator = rightIterator.advance()
            stepsToAdvance += 1
        }
        val (canOpen, canClose) = canOpenClose(tokens, iterator, rightIterator, canSplitText = true)
        for (index in 0 until stepsToAdvance) {
            val info = Info(
                tokenType = GFMTokenTypes.EQUAL,
                position = iterator.index + index,
                length = 0,
                canOpen = canOpen,
                canClose = canClose,
                marker = '='
            )
            delimiters.add(info)
        }
        return stepsToAdvance
    }

    override fun process(
        tokens: TokensCache,
        iterator: TokensCache.Iterator,
        delimiters: MutableList<Info>,
        result: SequentialParser.ParsingResultBuilder
    ) {
        // Start at the end and move backward, matching tokens
        var index = delimiters.size - 1

        while (index > 0) {
            // Find opening tilde
            if (!delimiters[index].isOpeningTilde()) {
                index -= 1
                continue
            }
            var openerIndex = index
            var closerIndex = delimiters[index].closerIndex

            // Attempt to widen the matched delimiters
            var delimitersMatched = 1
            while (EmphStrongDelimiterParser.Companion.areAdjacentSameMarkers(
                    delimiters,
                    openerIndex,
                    closerIndex
                )
            ) {
                openerIndex -= 1
                closerIndex += 1
                delimitersMatched += 1
            }

            // If 3 or more delimiters are matched, ignore
            if (delimitersMatched < 3) {
                val opener = delimiters[openerIndex]
                val closer = delimiters[closerIndex]

                result.withNode(
                    SequentialParser.Node(
                        opener.position..closer.position + 1,
                        GFMElementTypes.HIGHLIGHT
                    )
                )
            }

            // Update index
            index = openerIndex - 1
        }
    }
}

private fun DelimiterParser.Info.isOpeningTilde(): Boolean =
    tokenType == GFMTokenTypes.EQUAL && closerIndex != -1