package kmark.parser

interface MarkerProcessorFactory {
    fun createMarkerProcessor(productionHolder: ProductionHolder): MarkerProcessor<*>
}

