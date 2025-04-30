package kmark.ast.impl

import kmark.MarkdownElementTypes
import kmark.ast.ASTNode
import kmark.ast.CompositeASTNode

class ListItemCompositeNode(children: List<ASTNode>) :
    CompositeASTNode(MarkdownElementTypes.LIST_ITEM, children)