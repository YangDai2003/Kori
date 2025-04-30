package kmark.parser.markerblocks.impl

import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.MarkdownTokenTypes
import kmark.parser.LookaheadText
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockImpl
import kmark.parser.sequentialparsers.SequentialParser

class SetextHeaderMarkerBlock(
    myConstraints: MarkdownConstraints,
    private val productionHolder: ProductionHolder
) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
    override fun allowsSubBlocks(): Boolean = false

    private val contentMarker = productionHolder.mark()

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean =
        pos.offsetInCurrentLine == -1

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    private var nodeType: IElementType = MarkdownElementTypes.SETEXT_1

    override fun getDefaultNodeType(): IElementType {
        return nodeType
    }

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

        val startSpaces = pos.charsToNonWhitespace()
            ?: return MarkerBlock.ProcessingResult(
                MarkerBlock.ClosingAction.DROP,
                MarkerBlock.ClosingAction.DROP,
                MarkerBlock.EventAction.PROPAGATE
            )

        val setextMarkerStart = pos.nextPosition(startSpaces)
        if (setextMarkerStart?.char == '-') {
            nodeType = MarkdownElementTypes.SETEXT_2
        }

        val setextMarkerStartOffset = setextMarkerStart?.offset ?: pos.offset
        val markerNodeType = if (nodeType == MarkdownElementTypes.SETEXT_2)
            MarkdownTokenTypes.SETEXT_2
        else
            MarkdownTokenTypes.SETEXT_1

        contentMarker.done(MarkdownTokenTypes.SETEXT_CONTENT)
        productionHolder.addProduction(
            listOf(
                SequentialParser.Node(
                    setextMarkerStartOffset..pos.nextLineOrEofOffset, markerNodeType
                )
            )
        )
        scheduleProcessingResult(pos.nextLineOrEofOffset, MarkerBlock.ProcessingResult.DEFAULT)
        return MarkerBlock.ProcessingResult.CANCEL
    }
}
