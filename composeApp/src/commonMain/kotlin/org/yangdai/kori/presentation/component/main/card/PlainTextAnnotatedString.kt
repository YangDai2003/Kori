package org.yangdai.kori.presentation.component.main.card

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import org.yangdai.kori.presentation.component.note.plaintext.TextFormat
import org.yangdai.kori.presentation.theme.linkColor

fun buildPlainTextAnnotatedString(text: String) =
    buildAnnotatedString {
        append(text)
        listOf(
            TextFormat.AUTOLINK_EMAIL_ADDRESS,
            TextFormat.AUTOLINK_WEB_URL
        ).forEach { regex ->
            regex.findAll(text).forEach { matchResult ->
                addStyle(
                    SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                    matchResult.range.first,
                    matchResult.range.last + 1
                )
            }
        }
    }