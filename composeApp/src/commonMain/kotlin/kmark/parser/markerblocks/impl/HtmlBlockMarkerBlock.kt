package kmark.parser.markerblocks.impl

import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.MarkdownTokenTypes
import kmark.parser.LookaheadText
import kmark.parser.ProductionHolder
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.extendsPrev
import kmark.parser.constraints.getCharsEaten
import kmark.parser.markerblocks.MarkdownParserUtil
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockImpl
import kmark.parser.sequentialparsers.SequentialParser
import kotlin.text.Regex

class HtmlBlockMarkerBlock(
    myConstraints: MarkdownConstraints,
    private val productionHolder: ProductionHolder,
    private val endCheckingRegex: Regex?,
    startPosition: LookaheadText.Position
) : MarkerBlockImpl(myConstraints, productionHolder.mark()) {
    init {
        productionHolder.addProduction(
            listOf(
                SequentialParser.Node(
                    startPosition.offset..startPosition.nextLineOrEofOffset,
                    MarkdownTokenTypes.HTML_BLOCK_CONTENT
                )
            )
        )
    }

    override fun allowsSubBlocks(): Boolean = false

    override fun isInterestingOffset(pos: LookaheadText.Position): Boolean = true

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


        val prevLine = pos.prevLine ?: return MarkerBlock.ProcessingResult.DEFAULT
        if (!constraints.applyToNextLine(pos).extendsPrev(constraints)) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        if (endCheckingRegex == null && MarkdownParserUtil.calcNumberOfConsequentEols(
                pos,
                constraints
            ) >= 2
        ) {
            return MarkerBlock.ProcessingResult.DEFAULT
        } else if (endCheckingRegex != null && endCheckingRegex.find(prevLine) != null) {
            return MarkerBlock.ProcessingResult.DEFAULT
        }

        if (pos.currentLine.isNotEmpty()) {
            productionHolder.addProduction(
                listOf(
                    SequentialParser.Node(
                        pos.offset + 1 + constraints.getCharsEaten(pos.currentLine)..pos.nextLineOrEofOffset,
                        MarkdownTokenTypes.HTML_BLOCK_CONTENT
                    )
                )
            )
        }


        return MarkerBlock.ProcessingResult.CANCEL
    }

    override fun calcNextInterestingOffset(pos: LookaheadText.Position): Int {
        return pos.nextLineOrEofOffset
    }

    override fun getDefaultNodeType(): IElementType {
        return MarkdownElementTypes.HTML_BLOCK
    }
}