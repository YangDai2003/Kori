package kmark.parser

import kmark.MarkdownElementType
import kmark.MarkdownTokenTypes
import kmark.ast.ASTNode
import kmark.ast.ASTNodeBuilder

class TopLevelBuilder(nodeBuilder: ASTNodeBuilder) : TreeBuilder(nodeBuilder) {

    override fun flushEverythingBeforeEvent(
        event: MyEvent,
        currentNodeChildren: MutableList<MyASTNodeWrapper>?
    ) {
    }

    override fun createASTNodeOnClosingEvent(
        event: MyEvent,
        currentNodeChildren: List<MyASTNodeWrapper>,
        isTopmostNode: Boolean
    ): MyASTNodeWrapper {
        val newNode: ASTNode

        val type = event.info.type
        val startOffset = event.info.range.first
        val endOffset = event.info.range.last

        if (type is MarkdownElementType && type.isToken) {
            val nodes = nodeBuilder.createLeafNodes(type, startOffset, endOffset)
            return MyASTNodeWrapper(nodes.first(), startOffset, endOffset)
        }

        val childrenWithWhitespaces = ArrayList<ASTNode>(currentNodeChildren.size)

//        if (currentNodeChildren.isNotEmpty()) {
        addRawTokens(
            childrenWithWhitespaces,
            startOffset,
            currentNodeChildren.firstOrNull()?.startTokenIndex ?: endOffset
        )

        for (i in 1..<currentNodeChildren.size) {
            val prev = currentNodeChildren[i - 1]
            val next = currentNodeChildren[i]

            childrenWithWhitespaces.add(prev.astNode)

            addRawTokens(childrenWithWhitespaces, prev.endTokenIndex, next.startTokenIndex)
        }
        if (!currentNodeChildren.isEmpty()) {
            childrenWithWhitespaces.add(currentNodeChildren.last().astNode)
            addRawTokens(
                childrenWithWhitespaces,
                currentNodeChildren.last().endTokenIndex,
                endOffset
            )
        }
//        }

        newNode = nodeBuilder.createCompositeNode(type, childrenWithWhitespaces)
        return MyASTNodeWrapper(newNode, startOffset, endOffset)
    }

    private fun addRawTokens(childrenWithWhitespaces: MutableList<ASTNode>, from: Int, to: Int) {
        // Let's for now assume that it's just whitespace
        if (from != to) {
            childrenWithWhitespaces.addAll(
                nodeBuilder.createLeafNodes(
                    MarkdownTokenTypes.WHITE_SPACE,
                    from,
                    to
                )
            )
        }
    }

}
