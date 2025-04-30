package kmark.html

import kmark.ast.ASTNode

interface GeneratingProvider {
    fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode)
}