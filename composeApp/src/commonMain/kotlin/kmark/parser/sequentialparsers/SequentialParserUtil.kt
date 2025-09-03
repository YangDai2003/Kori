package kmark.parser.sequentialparsers

import kmark.MarkdownTokenTypes
import kmark.html.isPunctuation
import kmark.html.isWhitespace

class SequentialParserUtil {
    companion object {


        fun isWhitespace(info: TokensCache.Iterator, lookup: Int): Boolean {
            return isWhitespace(info.charLookup(lookup))
        }

        fun isPunctuation(info: TokensCache.Iterator, lookup: Int): Boolean {
            return isPunctuation(info.charLookup(lookup))
        }

        fun filterBlockquotes(tokensCache: TokensCache, textRange: IntRange): List<IntRange> {
            val result = ArrayList<IntRange>()
            var lastStart = textRange.first

            val rangeEnd = textRange.last
            for (i in lastStart..rangeEnd - 1) {
                if (tokensCache.Iterator(i).type == MarkdownTokenTypes.BLOCK_QUOTE) {
                    if (lastStart < i) {
                        result.add(lastStart..i - 1)
                    }
                    lastStart = i + 1
                }
            }
            if (lastStart < rangeEnd) {
                result.add(lastStart..rangeEnd)
            }
            return result
        }
    }

}

class RangesListBuilder {
    private val list = ArrayList<IntRange>()
    private var lastStart = -239
    private var lastEnd = -239

    fun put(index: Int) {
        if (lastEnd + 1 == index) {
            lastEnd = index
            return
        }
        if (lastStart != -239) {
            list.add(lastStart..lastEnd)
        }
        lastStart = index
        lastEnd = index
    }

    fun get(): List<IntRange> {
        if (lastStart != -239) {
            list.add(lastStart..lastEnd)
        }
        lastStart = -239
        lastEnd = -239
        return list
    }

}
