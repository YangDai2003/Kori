package kmark.ast.visitors

import kmark.ast.ASTNode
import kmark.ast.CompositeASTNode

open class RecursiveVisitor : Visitor {
    override fun visitNode(node: ASTNode) {
        if (node is CompositeASTNode) {
            for (child in node.children) {
                visitNode(child)
            }
        }
    }
}

