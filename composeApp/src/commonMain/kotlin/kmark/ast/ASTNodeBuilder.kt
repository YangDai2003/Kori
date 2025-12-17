package kmark.ast

import kmark.ExperimentalApi
import kmark.IElementType
import kmark.MarkdownElementTypes
import kmark.MarkdownTokenTypes
import kmark.ast.impl.ListCompositeNode
import kmark.ast.impl.ListItemCompositeNode
import kmark.parser.CancellationToken

open class ASTNodeBuilder @ExperimentalApi constructor(
    protected val text: CharSequence,
    protected val cancellationToken: CancellationToken
) {
    /**
     * For compatibility only.
     */
    @OptIn(ExperimentalApi::class)
    constructor(text: CharSequence) : this(text, CancellationToken.NonCancellable)

    @OptIn(ExperimentalApi::class)
    open fun createLeafNodes(type: IElementType, startOffset: Int, endOffset: Int): List<ASTNode> {
        if (type == MarkdownTokenTypes.WHITE_SPACE) {
            val result = ArrayList<ASTNode>()
            var lastEol = startOffset
            while (lastEol < endOffset) {
                cancellationToken.checkCancelled()

                val nextEol = indexOfSubSeq(text, lastEol, endOffset, '\n')
                if (nextEol == -1) {
                    break
                }

                if (nextEol > lastEol) {
                    result.add(LeafASTNode(MarkdownTokenTypes.WHITE_SPACE, lastEol, nextEol))
                }
                result.add(LeafASTNode(MarkdownTokenTypes.EOL, nextEol, nextEol + 1))
                lastEol = nextEol + 1
            }
            if (endOffset > lastEol) {
                result.add(LeafASTNode(MarkdownTokenTypes.WHITE_SPACE, lastEol, endOffset))
            }

            return result
        }
        return listOf(LeafASTNode(type, startOffset, endOffset))
    }

    @OptIn(ExperimentalApi::class)
    open fun createCompositeNode(type: IElementType, children: List<ASTNode>): CompositeASTNode {
        cancellationToken.checkCancelled()
        return when (type) {
            MarkdownElementTypes.UNORDERED_LIST,
            MarkdownElementTypes.ORDERED_LIST -> {
                ListCompositeNode(type, children)
            }

            MarkdownElementTypes.LIST_ITEM -> {
                ListItemCompositeNode(children)
            }

            else -> {
                CompositeASTNode(type, children)
            }
        }
    }

    companion object {
        fun indexOfSubSeq(s: CharSequence, from: Int, to: Int, c: Char): Int {
            for (i in from..<to) {
                if (s[i] == c) {
                    return i
                }
            }
            return -1
        }
    }
}
