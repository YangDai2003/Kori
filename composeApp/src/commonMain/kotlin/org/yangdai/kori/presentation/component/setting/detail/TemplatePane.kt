package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.date_format
import kori.composeapp.generated.resources.date_in_the_template_file_will_be_replaced_with_this_value
import kori.composeapp.generated.resources.explore_and_share_community_note_templates
import kori.composeapp.generated.resources.for_more_syntax_refer_to
import kori.composeapp.generated.resources.format_reference
import kori.composeapp.generated.resources.time_format
import kori.composeapp.generated.resources.time_in_the_template_file_will_be_replaced_with_this_value
import kori.composeapp.generated.resources.you_can_also_use_date_yyyy_mm_dd_to_override_the_format_once
import kori.composeapp.generated.resources.you_can_also_use_time_hh_mm_to_override_the_format_once
import kori.composeapp.generated.resources.your_current_syntax_looks_like_this
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.note.CustomTextField
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.util.Constants
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(FormatStringsInDatetimeFormats::class, ExperimentalTime::class)
@Composable
fun TemplatePane(mainViewModel: MainViewModel) {

    val uriHandler = LocalUriHandler.current
    val templatePaneState by mainViewModel.templatePaneState.collectAsStateWithLifecycle()

    var currentDateFormatter by remember { mutableStateOf("") }
    var currentTimeFormatter by remember { mutableStateOf("") }

    var currentDateFormatted by remember { mutableStateOf("") }
    var currentTimeFormatted by remember { mutableStateOf("") }

    var isDateInvalid by remember { mutableStateOf(false) }
    var isTimeInvalid by remember { mutableStateOf(false) }

    LaunchedEffect(templatePaneState) {
        currentDateFormatter = templatePaneState.dateFormatter
        currentTimeFormatter = templatePaneState.timeFormatter
    }

    LaunchedEffect(currentDateFormatter, currentTimeFormatter) {
        runCatching {
            val today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val dateFormatter = LocalDate.Format {
                byUnicodePattern(currentDateFormatter.ifBlank { "yyyy-MM-dd" })
            }
            currentDateFormatted = today.format(dateFormatter)
        }.onFailure {
            isDateInvalid = true
        }.onSuccess {
            isDateInvalid = false
        }

        runCatching {
            val now = Clock.System.now()
            val localTime = now.toLocalDateTime(TimeZone.currentSystemDefault()).time
            val timeFormatter = LocalTime.Format {
                byUnicodePattern(currentTimeFormatter.ifBlank { "HH:mm" })
            }
            currentTimeFormatted = localTime.format(timeFormatter)
        }.onFailure {
            isTimeInvalid = true
        }.onSuccess {
            isTimeInvalid = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.putPreferenceValue(
                Constants.Preferences.DATE_FORMATTER, currentDateFormatter
            )
            mainViewModel.putPreferenceValue(
                Constants.Preferences.TIME_FORMATTER, currentTimeFormatter
            )
        }
    }

    Column(
        Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(Res.string.date_format)) },
                supportingContent = {
                    val annotatedString = buildAnnotatedString {
                        append(stringResource(Res.string.date_in_the_template_file_will_be_replaced_with_this_value))
                        append(stringResource(Res.string.you_can_also_use_date_yyyy_mm_dd_to_override_the_format_once))
                        append(stringResource(Res.string.for_more_syntax_refer_to))
                        withLink(
                            LinkAnnotation.Url("https://developer.android.com/reference/java/time/format/DateTimeFormatter#patterns")
                        ) {
                            append(stringResource(Res.string.format_reference))
                        }
                        append(
                            stringResource(
                                Res.string.your_current_syntax_looks_like_this, currentDateFormatted
                            )
                        )
                    }
                    Text(text = annotatedString)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Row(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                CustomTextField(
                    value = currentDateFormatter,
                    onValueChange = { currentDateFormatter = it },
                    isError = isDateInvalid,
                    leadingIcon = Icons.Outlined.DateRange,
                    placeholderText = "yyyy-MM-dd"
                )
            }
        }


        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(Res.string.time_format)) },
                supportingContent = {
                    val annotatedString = buildAnnotatedString {
                        append(stringResource(Res.string.time_in_the_template_file_will_be_replaced_with_this_value))
                        append(stringResource(Res.string.you_can_also_use_time_hh_mm_to_override_the_format_once))
                        append(stringResource(Res.string.for_more_syntax_refer_to))
                        withLink(
                            LinkAnnotation.Url("https://developer.android.com/reference/java/time/format/DateTimeFormatter#patterns")
                        ) {
                            append(stringResource(Res.string.format_reference))
                        }
                        append(
                            stringResource(
                                Res.string.your_current_syntax_looks_like_this, currentTimeFormatted
                            )
                        )
                    }
                    Text(text = annotatedString)
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            Row(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                CustomTextField(
                    value = currentTimeFormatter,
                    onValueChange = { currentTimeFormatter = it },
                    isError = isTimeInvalid,
                    leadingIcon = Icons.Outlined.AccessTime,
                    placeholderText = "HH:mm"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DetailPaneItem(
            title = stringResource(Res.string.explore_and_share_community_note_templates),
            icon = Icons.AutoMirrored.Outlined.OpenInNew,
            onClick = {
                uriHandler.openUri("https://github.com/YangDai2003/OpenNote-Compose/discussions/categories/community-templates")
            }
        )

        Spacer(Modifier.height(8.dp))
    }
}