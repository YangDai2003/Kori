package kmark.flavours.gfm.table

import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.eatItselfFromString
import kmark.parser.constraints.extendsPrev
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider

class GitHubTableMarkerProvider : MarkerBlockProvider<MarkerProcessor.StateInfo> {
    override fun createMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder,
        stateInfo: MarkerProcessor.StateInfo
    ): List<MarkerBlock> {
        val currentConstraints = stateInfo.currentConstraints
        if (stateInfo.nextConstraints != currentConstraints) {
            return emptyList()
        }

        val currentLineFromPosition = pos.currentLineFromPosition
        if (!currentLineFromPosition.contains('|')) {
            return emptyList()
        }

        val split = GitHubTableMarkerBlock.splitByPipes(currentLineFromPosition)
        val numberOfHeaderCells = split
            .mapIndexed { i, s -> (i > 0 && i < split.lastIndex) || s.isNotBlank() }
            .count { it }
        if (numberOfHeaderCells == 0) {
            return emptyList()
        }
        val nextLine = getNextLineFromConstraints(pos, currentConstraints) ?: return emptyList()
        if (countSecondLineCells(nextLine) == numberOfHeaderCells) {
            return listOf(
                GitHubTableMarkerBlock(
                    pos,
                    currentConstraints,
                    productionHolder,
                    numberOfHeaderCells
                )
            )
        }
        return emptyList()
    }

    override fun interruptsParagraph(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): Boolean {
        return false
    }

    private fun getNextLineFromConstraints(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints
    ): CharSequence? {
        val line = pos.nextLine ?: return null
        val nextLineConstraints = constraints.applyToNextLine(pos.nextLinePosition())
        return if (nextLineConstraints.extendsPrev(constraints)) {
            nextLineConstraints.eatItselfFromString(line)
        } else {
            null
        }
    }

    companion object {
        /**
         * @return number of cells in the separator line
         */
        fun countSecondLineCells(line: CharSequence): Int {
            var offset = passWhiteSpaces(line, 0)
            if (offset < line.length && line[offset] == '|') {
                offset++
            }

            var result = 0
            while (offset < line.length) {
                offset = passWhiteSpaces(line, offset)
                if (offset < line.length && line[offset] == ':') {
                    offset++
                    offset = passWhiteSpaces(line, offset)
                }

                var dashes = 0
                while (offset < line.length && line[offset] == '-') {
                    offset++
                    dashes++
                }

                if (dashes < 1) {
                    return 0
                }
                result++

                offset = passWhiteSpaces(line, offset)
                if (offset < line.length && line[offset] == ':') {
                    offset++
                    offset = passWhiteSpaces(line, offset)
                }

                if (offset < line.length && line[offset] == '|') {
                    offset++
                    offset = passWhiteSpaces(line, offset)
                } else {
                    break
                }
            }

            return if (offset == line.length) {
                result
            } else {
                0
            }
        }

        fun passWhiteSpaces(line: CharSequence, offset: Int): Int {
            var curOffset = offset
            while (curOffset < line.length) {
                if (line[curOffset] != ' ' && line[curOffset] != '\t') {
                    break
                }
                curOffset++
            }
            return curOffset
        }
    }
}