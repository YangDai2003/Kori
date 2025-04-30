package kmark.parser.markerblocks.impl

import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.lexer.Compat.assert
import kmark.parser.LookaheadText
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.applyToNextLineAndAddModifiers
import kmark.parser.constraints.extendsPrev
import kmark.parser.markerblocks.MarkdownParserUtil
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockImpl

class ListItemMarkerBlock(
    myConstraints: MarkdownConstraints,
    marker: ProductionHolder.Marker
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
        if (!nextLineConstraints.extendsPrev(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.LIST_ITEM
    }
}
