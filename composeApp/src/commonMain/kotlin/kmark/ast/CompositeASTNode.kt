package kmark.ast

import kmark.IElementType

open class CompositeASTNode(type: IElementType, final override val children: List<ASTNode>) :
    ASTNodeImpl(
        type,
        children.firstOrNull()?.startOffset ?: 0,
        children.lastOrNull()?.endOffset ?: 0
    ) {
    init {
        for (child in children) {
            if (child is ASTNodeImpl) {
                @Suppress("LeakingThis")
                child.parent = this
            }
        }
    }
}
