package kmark.ast.visitors

import kmark.ast.ASTNode

interface Visitor {
    fun visitNode(node: ASTNode)
}