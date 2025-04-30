package kmark.ast

import kmark.IElementType
import kmark.ast.visitors.Visitor

interface ASTNode {
    val type: IElementType
    val startOffset: Int
    val endOffset: Int
    val parent: ASTNode?
    val children: List<ASTNode>
}

fun ASTNode.accept(visitor: Visitor) {
    visitor.visitNode(this)
}

fun ASTNode.acceptChildren(visitor: Visitor) {
    for (child in children) {
        child.accept(visitor)
    }
}   
