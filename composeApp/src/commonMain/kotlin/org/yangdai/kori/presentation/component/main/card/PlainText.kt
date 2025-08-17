package org.yangdai.kori.presentation.component.main.card

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import org.yangdai.kori.presentation.component.note.plaintext.Patterns
import org.yangdai.kori.presentation.screen.settings.CardSize
import org.yangdai.kori.presentation.theme.linkColor

@Composable
fun PlainText(text: String, noteItemProperties: NoteItemProperties) =
    Text(
        text = buildAnnotatedString {
            append(text)
            listOf(
                Patterns.AUTOLINK_EMAIL_ADDRESS,
                Patterns.AUTOLINK_WEB_URL
            ).forEach { regex ->
                regex.findAll(text).forEach { matchResult ->
                    addStyle(
                        SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                        matchResult.range.first,
                        matchResult.range.last + 1
                    )
                }
            }
        },
        style = MaterialTheme.typography.bodyMedium,
        maxLines = if (noteItemProperties.cardSize == CardSize.DEFAULT) 5 else 2,
        overflow = if (noteItemProperties.clipOverflow) TextOverflow.Clip else TextOverflow.Ellipsis
    )