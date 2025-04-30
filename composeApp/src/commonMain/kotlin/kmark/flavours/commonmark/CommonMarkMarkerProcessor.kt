package kmark.flavours.commonmark

import kmark.MarkdownTokenTypes
import kmark.parser.markerblocks.providers.AtxHeaderProvider
import kmark.parser.markerblocks.providers.BlockQuoteProvider
import kmark.parser.markerblocks.providers.CodeBlockProvider
import kmark.parser.markerblocks.providers.CodeFenceProvider
import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.MarkerProcessorFactory
import kmark.parser.ProductionHolder
import kmark.parser.constraints.CommonMarkdownConstraints
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.getCharsEaten
import kmark.parser.markerblocks.MarkerBlock
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.markerblocks.providers.HorizontalRuleProvider
import kmark.parser.markerblocks.providers.HtmlBlockProvider
import kmark.parser.markerblocks.providers.LinkReferenceDefinitionProvider
import kmark.parser.markerblocks.providers.ListMarkerProvider
import kmark.parser.markerblocks.providers.SetextHeaderProvider
import kmark.parser.sequentialparsers.SequentialParser
import kotlin.math.min

open class CommonMarkMarkerProcessor(
    productionHolder: ProductionHolder,
    constraintsBase: MarkdownConstraints
) : MarkerProcessor<MarkerProcessor.StateInfo>(productionHolder, constraintsBase) {
    override var stateInfo: StateInfo = StateInfo(
        startConstraints,
        startConstraints,
        markersStack
    )

    private val markerBlockProviders = listOf(
        CodeBlockProvider(),
        HorizontalRuleProvider(),
        CodeFenceProvider(),
        SetextHeaderProvider(),
        BlockQuoteProvider(),
        ListMarkerProvider(),
        AtxHeaderProvider(),
        HtmlBlockProvider(),
        LinkReferenceDefinitionProvider()
    )

    override fun getMarkerBlockProviders(): List<MarkerBlockProvider<StateInfo>> {
        return markerBlockProviders
    }

    override fun updateStateInfo(pos: LookaheadText.Position) {
        if (pos.offsetInCurrentLine == -1) {
            stateInfo = StateInfo(
                startConstraints,
                topBlockConstraints.applyToNextLine(pos),
                markersStack
            )
        } else if (MarkerBlockProvider.isStartOfLineWithConstraints(
                pos,
                stateInfo.nextConstraints
            )
        ) {
            stateInfo = StateInfo(
                stateInfo.nextConstraints,
                stateInfo.nextConstraints.addModifierIfNeeded(pos) ?: stateInfo.nextConstraints,
                markersStack
            )
        }
    }

    override fun populateConstraintsTokens(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints,
        productionHolder: ProductionHolder
    ) {
        if (constraints.indent == 0) {
            return
        }

        val startOffset = pos.offset
        val endOffset = min(
            pos.offset - pos.offsetInCurrentLine + constraints.getCharsEaten(pos.currentLine),
            pos.nextLineOrEofOffset
        )

        val type = when (constraints.types.lastOrNull()) {
            '>' ->
                MarkdownTokenTypes.BLOCK_QUOTE

            '.', ')' ->
                MarkdownTokenTypes.LIST_NUMBER

            else ->
                MarkdownTokenTypes.LIST_BULLET
        }
        productionHolder.addProduction(listOf(SequentialParser.Node(startOffset..endOffset, type)))
    }

    override fun createNewMarkerBlocks(
        pos: LookaheadText.Position,
        productionHolder: ProductionHolder
    ): List<MarkerBlock> {
        if (pos.offsetInCurrentLine == -1) {
            return NO_BLOCKS
        }

        return super.createNewMarkerBlocks(pos, productionHolder)
    }

    object Factory : MarkerProcessorFactory {
        override fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*> {
            return CommonMarkMarkerProcessor(productionHolder, CommonMarkdownConstraints.BASE)
        }
    }
}
