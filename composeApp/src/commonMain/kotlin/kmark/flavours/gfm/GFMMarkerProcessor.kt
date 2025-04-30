package kmark.flavours.gfm

import kmark.MarkdownTokenTypes
import kmark.flavours.commonmark.CommonMarkMarkerProcessor
import kmark.flavours.gfm.table.GitHubTableMarkerProvider
import kmark.parser.LookaheadText
import kmark.parser.MarkerProcessor
import kmark.parser.MarkerProcessorFactory
import kmark.parser.ProductionHolder
import kmark.parser.constraints.CommonMarkdownConstraints
import kmark.parser.constraints.MarkdownConstraints
import kmark.parser.constraints.getCharsEaten
import kmark.parser.markerblocks.MarkerBlockProvider
import kmark.parser.sequentialparsers.SequentialParser
import kotlin.math.min

class GFMMarkerProcessor(
    productionHolder: ProductionHolder,
    constraintsBase: CommonMarkdownConstraints
) : CommonMarkMarkerProcessor(productionHolder, constraintsBase) {

    private val markerBlockProviders = super.getMarkerBlockProviders()
        .plus(listOf(GitHubTableMarkerProvider()))

    override fun getMarkerBlockProviders(): List<MarkerBlockProvider<StateInfo>> {
        return markerBlockProviders
    }

    override fun populateConstraintsTokens(
        pos: LookaheadText.Position,
        constraints: MarkdownConstraints,
        productionHolder: ProductionHolder
    ) {
        if (constraints !is GFMConstraints || !constraints.hasCheckbox()) {
            super.populateConstraintsTokens(pos, constraints, productionHolder)
            return
        }

        val line = pos.currentLine
        var offset = pos.offsetInCurrentLine
        while (offset < line.length && line[offset] != '[') {
            offset++
        }
        if (offset == line.length) {
            super.populateConstraintsTokens(pos, constraints, productionHolder)
            return
        }

        val type = when (constraints.types.lastOrNull()) {
            '>' ->
                MarkdownTokenTypes.BLOCK_QUOTE

            '.', ')' ->
                MarkdownTokenTypes.LIST_NUMBER

            else ->
                MarkdownTokenTypes.LIST_BULLET
        }
        val middleOffset = pos.offset - pos.offsetInCurrentLine + offset
        val endOffset = min(
            pos.offset - pos.offsetInCurrentLine + constraints.getCharsEaten(pos.currentLine),
            pos.nextLineOrEofOffset
        )

        productionHolder.addProduction(
            listOf(
                SequentialParser.Node(pos.offset..middleOffset, type),
                SequentialParser.Node(middleOffset..endOffset, GFMTokenTypes.CHECK_BOX)
            )
        )
    }

    object Factory : MarkerProcessorFactory {
        override fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*> {
            return GFMMarkerProcessor(productionHolder, GFMConstraints.BASE)
        }
    }
}