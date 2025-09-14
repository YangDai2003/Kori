package org.yangdai.kori

import org.yangdai.kori.presentation.component.note.todo.TodoLint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TodoLintTest {

    private val linter = TodoLint()
    private val ids = TodoLint.Companion.RuleIds

    @Test
    fun `completion marker not lowercase - expect issue`() {
        val issues = linter.validate("X task")
        assertEquals(1, issues.size)
        assertEquals(ids.COMPLETION_MARKER_NOT_LOWERCASE, issues[0].ruleId)
        assertEquals(0, issues[0].startIndex)
        assertEquals(1, issues[0].endIndex)
    }

    @Test
    fun `invalid space after completion marker - too many spaces - expect issue`() {
        val issues = linter.validate("x  task") // Two spaces
        assertEquals(1, issues.size)
        assertEquals(ids.INVALID_SPACE_AFTER_COMPLETION_MARKER, issues[0].ruleId)
        assertEquals(1, issues[0].startIndex) // Index of the first offending space
        assertEquals(3, issues[0].endIndex)   // Index after the last offending space
    }

    @Test
    fun `invalid completion marker position - expect issue`() {
        val issues = linter.validate("task x done")
        assertEquals(1, issues.size)
        assertEquals(ids.INVALID_COMPLETION_MARKER_POSITION, issues[0].ruleId)
        assertEquals(4, issues[0].startIndex)
        assertEquals(7, issues[0].endIndex)   // " x "

        val issues2 = linter.validate("(A) x task done")
        assertEquals(1, issues2.size)
        assertEquals(ids.INVALID_COMPLETION_MARKER_POSITION, issues2[0].ruleId)
        assertEquals(3, issues2[0].startIndex)
        assertEquals(6, issues2[0].endIndex)
    }

    @Test
    fun `invalid priority format - lowercase - expect issue`() {
        val issues = linter.validate("(a) task")
        assertEquals(1, issues.size)
        assertEquals(ids.INVALID_PRIORITY_FORMAT_OR_PLACEMENT, issues[0].ruleId)
        assertEquals(0, issues[0].startIndex)
        assertEquals(4, issues[0].endIndex) // "(a) "
    }

    @Test
    fun `invalid priority placement - not at start or after x - expect issue`() {
        val issues = linter.validate("task (A) done")
        assertEquals(1, issues.size)
        assertEquals(ids.INVALID_PRIORITY_FORMAT_OR_PLACEMENT, issues[0].ruleId)
        assertEquals(5, issues[0].startIndex) // "task " is 5 chars
        assertEquals(9, issues[0].endIndex)   // "(A) "
    }

    @Test
    fun `valid completed task`() {
        val issues = linter.validate("x task")
        val issueIds = issues.map { it.ruleId }
        assertTrue(!issueIds.contains(ids.INVALID_PRIORITY_FORMAT_OR_PLACEMENT))
    }

    @Test
    fun `valid completed task with priority`() {
        val issues = linter.validate("x (A) 2023-01-01 buy milk")
        assertTrue(issues.isEmpty())
    }

    @Test
    fun `valid task with priority`() {
        val issues = linter.validate("(A) 2023-01-01 buy milk")
        assertTrue(issues.isEmpty())
    }
}
