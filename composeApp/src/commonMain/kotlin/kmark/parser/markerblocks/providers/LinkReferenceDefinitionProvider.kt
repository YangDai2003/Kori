package kmark.parser.markerblocks.providers

import kmark.MarkdownElementTypes
import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.markerblocks.impl.LinkReferenceDefinitionMarkerBlock
import kmark.parser.sequentialparsers.SequentialParser

class LinkReferenceDefinitionProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {

        if (!MarkerBlockProvider.isStartOfLineWithConstraints(pos, stateInfo.currentConstraints)) {
            return emptyList()
        }

        val matchResult = matchLinkDefinition(pos.originalText, pos.offset) ?: return emptyList()
        for ((i, range) in matchResult.withIndex()) {
            productionHolder.addProduction(
                listOf(
                    SequentialParser.Node(
                        addToRangeAndWiden(range, 0), when (i) {
                            0 -> MarkdownElementTypes.LINK_LABEL
                            1 -> MarkdownElementTypes.LINK_DESTINATION
                            2 -> MarkdownElementTypes.LINK_TITLE
                            else -> throw AssertionError("There are no more than three groups in this regex")
                        }
                    )
                )
            )
        }

        val matchLength = matchResult.last().last - pos.offset + 1
        val endPosition = pos.nextPosition(matchLength)

        if (endPosition != null && !isEndOfLine(endPosition)) {
            return emptyList()
        }
        return listOf(
            LinkReferenceDefinitionMarkerBlock(
                stateInfo.currentConstraints, productionHolder.mark(),
                pos.offset + matchLength
            )
        )
    }

    override fun interruptsParagraph(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Boolean {
        return false
    }

    companion object {

        fun addToRangeAndWiden(range: IntRange, t: Int): IntRange {
            return IntRange(range.first + t, range.last + t + 1)
        }

        fun isEndOfLine(pos: LookaheadText.Position): Boolean {
            return pos.offsetInCurrentLine == -1 || pos.charsToNonWhitespace() == null
        }

        fun matchLinkDefinition(text: CharSequence, startOffset: Int): List<IntRange>? {
            var offset = MarkerBlockProvider.passSmallIndent(text, startOffset)
            val linkLabel = matchLinkLabel(text, offset) ?: return null
            offset = linkLabel.last + 1
            if (offset >= text.length || text[offset] != ':')
                return null
            offset++

            offset = passOneNewline(text, offset)

            val destination = matchLinkDestination(text, offset) ?: return null
            offset = destination.last + 1
            offset = passOneNewline(text, offset)

            val title = matchLinkTitle(text, offset)

            val result = ArrayList<IntRange>()
            result.add(linkLabel)
            result.add(destination)
            if (title != null) {
                offset = title.last + 1
                while (offset < text.length && isSpace(text[offset]))
                    offset++
                if (offset >= text.length || text[offset] == '\n') {
                    result.add(title)
                }
            }

            return result
        }

        fun matchLinkDestination(text: CharSequence, start: Int): IntRange? {
            if (start >= text.length)
                return null

            var offset = start
            if (text[offset] == '<') {
                offset++
                while (offset < text.length) {
                    val c = text[offset]
                    if (c == '>')
                        return IntRange(start, offset)
                    if (c == '<' || c == '>' || isSpaceOrNewline(c))
                        return null
                    if (c == '\\' && offset + 1 < text.length && !isSpaceOrNewline(text[offset + 1]))
                        offset++

                    offset++
                }
                return null
            } else {
                var hasParens = false
                while (offset < text.length) {
                    val c = text[offset]
                    if (isSpaceOrNewline(c) || c.code <= 27)
                        break
                    if (c == '(') {
                        if (hasParens)
                            break
                        else
                            hasParens = true
                    } else if (c == ')') {
                        if (!hasParens)
                            break
                        else
                            hasParens = false
                    } else if (c == '\\' && offset + 1 < text.length && !isSpaceOrNewline(text[offset + 1]))
                        offset++

                    offset++
                }
                return if (start == offset)
                    null
                else
                    IntRange(start, offset - 1)
            }

        }

        fun matchLinkTitle(text: CharSequence, start: Int): IntRange? {
            if (start >= text.length)
                return null

            val endDelim = when (text[start]) {
                '\'' -> '\''
                '"' -> '"'
                '(' -> ')'
                else -> return null
            }

            var offset = start + 1
            var isBlank = false
            while (offset < text.length) {
                val c = text[offset]
                if (c == endDelim)
                    return IntRange(start, offset)
                if (c == '\n') {
                    if (isBlank)
                        return null
                    else
                        isBlank = true
                } else if (!isSpace(c)) {
                    isBlank = false
                }

                if (c == '\\' && offset + 1 < text.length && !isSpaceOrNewline(text[offset + 1]))
                    offset++

                offset++
            }
            return null
        }

        fun matchLinkLabel(text: CharSequence, start: Int): IntRange? {
            var offset = start
            if (offset >= text.length || text[offset] != '[') {
                return null
            }
            offset++

            var seenNonWhitespace = false

            for (i in 1..999) {
                if (offset >= text.length)
                    return null
                var c = text[offset]
                if (c == '[' || c == ']')
                    break
                if (c == '\\') {
                    offset++
                    if (offset >= text.length)
                        return null
                    c = text[offset]
                }
                if (!c.isWhitespace()) {
                    seenNonWhitespace = true
                }
                offset++
            }
            if (!seenNonWhitespace || offset >= text.length || text[offset] != ']') {
                return null
            }
            return start..offset
        }

        private fun passOneNewline(text: CharSequence, start: Int): Int {
            var offset = start
            while (offset < text.length && isSpace(text[offset]))
                offset++
            if (offset < text.length && text[offset] == '\n') {
                offset++
                while (offset < text.length && isSpace(text[offset]))
                    offset++
            }
            return offset
        }

        private inline fun isSpace(c: Char) = c == ' ' || c == '\t'

        private inline fun isSpaceOrNewline(c: Char) = isSpace(c) || c == '\n'
    }
}
