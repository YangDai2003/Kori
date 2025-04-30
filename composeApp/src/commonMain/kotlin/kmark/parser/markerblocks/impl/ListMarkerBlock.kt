package kmark.parser.markerblocks.impl

import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.lexer.Compat.assert
import kmark.parser.LookaheadText
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.applyToNextLineAndAddModifiers
import kmark.parser.constraints.extendsList
import kmark.parser.markerblocks.MarkdownParserUtil
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockImpl

class ListMarkerBlock(
    myConstraints: MarkdownConstraints,
    marker: ProductionHolder.Marker,
    private val listType: Char
) : MarkerBlockImpl(myConstraints, marker) {
    override fun allowsSubBlocks(): Boolean = true

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean =
        pos.offsetInCurrentLine == -1

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOffset ?: -1
    }

    override fun doProcessToken(
        pos: LookaheadText.Position,
        currentConstraints: MarkdownConstraints
    ): MarkerBlock.ProcessingResult {
        assert(pos.offsetInCurrentLine == -1)

        val eolN = MarkdownParserUtil.calcNumberOfConsequentEols(pos, constraints)
        if (eolN >= 3) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        val nonemptyPos = MarkdownParserUtil.getFirstNonWhitespaceLinePos(pos, eolN)
            ?: return MarkerBlock.ProcessingResult.DEFAULT
        val nextLineConstraints = constraints.applyToNextLineAndAddModifiers(nonemptyPos)
        if (!nextLineConstraints.extendsList(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.PASS
    }

    override fun getDefaultNodeType(): IElementType {
        return if (listType == '-' || listType == '*' || listType == '+')
            MarkdownElementTypes.UNORDERED_LIST
        else
            MarkdownElementTypes.ORDERED_LIST
    }
}
