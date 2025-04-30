package kmark.parser.markerblocks.impl

import kmark.IElementType
import kmark.MarkdownTokenTypes
import kmark.parser.LookaheadText
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockImpl

class HorizontalRuleMarkerBlock(
    myConstraints: MarkdownConstraints,
    marker: ProductionHolder.Marker
) : MarkerBlockImpl(myConstraints, marker) {
    override fun allowsSubBlocks(): Boolean = false

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean =
        pos.offsetInCurrentLine == -1

    override fun getDefaultAction(): MarkerBlock.ClosingAction {
        return MarkerBlock.ClosingAction.DONE
    }

    override fun doProcessToken(
        pos: LookaheadText.Position,
        currentConstraints: MarkdownConstraints
    ): MarkerBlock.ProcessingResult {
        if (pos.offsetInCurrentLine != -1) {
            return MarkerBlock.ProcessingResult.CANCEL
        }
        return MarkerBlock.ProcessingResult.DEFAULT
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownTokenTypes.HORIZONTAL_RULE
    }
}