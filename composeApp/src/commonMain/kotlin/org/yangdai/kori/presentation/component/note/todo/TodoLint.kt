package org.yangdai.kori.presentation.component.note.todo

import org.yangdai.kori.presentation.component.note.Issue
import org.yangdai.kori.presentation.component.note.Lint

class TodoLint : Lint {
    override fun validate(text: String): List<Issue> {
        return emptyList()
    }
}