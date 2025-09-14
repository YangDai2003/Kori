package org.yangdai.kori

import org.yangdai.kori.presentation.component.note.markdown.MarkdownLint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkdownLintTest {

    private val linter = MarkdownLint()
    private val ids = MarkdownLint.Companion.RuleIds

    private fun assertIssue(
        text: String,
        ruleId: String,
        expectedStartIndex: Int? = null,
        expectedEndIndex: Int? = null
    ) {
        val issues = linter.validate(text)
        val issue = issues.find { it.ruleId == ruleId }
        assertTrue(issue != null, "Expected rule $ruleId to be triggered for text: '$text'")
        issue.let {
            expectedStartIndex?.let {
                assertEquals(
                    it,
                    issue.startIndex,
                    "Issue startIndex mismatch for $ruleId on text '$text'"
                )
            }
            expectedEndIndex?.let {
                assertEquals(
                    it,
                    issue.endIndex,
                    "Issue endIndex mismatch for $ruleId on text '$text'"
                )
            }
        }
    }

    @Test
    fun testHeadingInvalidFormat_MD001() {
        // Example: Missing space after #
        assertIssue("#Invalid Format", ids.HEADING_INVALID_FORMAT, 0, 15)
        assertIssue("####### Too many hashes", ids.HEADING_INVALID_FORMAT, 0, 23)
    }

    @Test
    fun testHeadingInvalidSpacing_MD002() {
        assertIssue("#  Too many spaces", ids.HEADING_INVALID_SPACING, 2, 3)
    }

    @Test
    fun testHeadingTrailingPunctuation_MD003() {
        assertIssue("# Heading.", ids.HEADING_TRAILING_PUNCTUATION, 9, 10)
        assertIssue("## Heading,", ids.HEADING_TRAILING_PUNCTUATION, 10, 11)
        assertIssue("### Heading;", ids.HEADING_TRAILING_PUNCTUATION, 11, 12)
        assertIssue("#### Heading:", ids.HEADING_TRAILING_PUNCTUATION, 12, 13)
    }

    @Test
    fun testTrailingSpaces_MD004() {
        assertIssue("Line with three spaces   ", ids.TRAILING_SPACES, 24, 25)
        assertIssue("Line with four spaces    ", ids.TRAILING_SPACES, 23, 25)
    }

    @Test
    fun testLinkChineseParentheses_MD005() {
        assertIssue("[link]（url）", ids.LINK_CHINESE_PARENTHESES, 6, 7) // Opening paren
        // assertIssue("[link]（url）", ids.LINK_CHINESE_PARENTHESES, 11, 12) // Closing paren
    }

    @Test
    fun testImageChineseExclamation_MD006() {
        assertIssue("！[alt](url)", ids.IMAGE_CHINESE_EXCLAMATION, 0, 1)
    }

    @Test
    fun testInlineCodeSurroundingSpaces_MD007() {
        assertIssue("` code `", ids.INLINE_CODE_SURROUNDING_SPACES, 1, 2) // Leading space
        // assertIssue("` code `", ids.INLINE_CODE_SURROUNDING_SPACES, 6, 7) // Trailing space - linter might report one or two issues
        assertIssue("`code `", ids.INLINE_CODE_SURROUNDING_SPACES, 5, 6)
        assertIssue("` code`", ids.INLINE_CODE_SURROUNDING_SPACES, 1, 2)
        assertIssue("`  `", ids.INLINE_CODE_SURROUNDING_SPACES, 1, 3) // both
    }

    @Test
    fun testLinkTextSurroundingSpaces_MD008() {
        assertIssue("[ text ](url)", ids.LINK_TEXT_SURROUNDING_SPACES, 1, 2) // Leading space
        // assertIssue("[ text ](url)", ids.LINK_TEXT_SURROUNDING_SPACES, 6, 7) // Trailing space
        assertIssue("![ alt ](url)", ids.LINK_TEXT_SURROUNDING_SPACES, 2, 3) // Image leading
        // assertIssue("![ alt ](url)", ids.LINK_TEXT_SURROUNDING_SPACES, 7,8) // Image trailing
    }
}
