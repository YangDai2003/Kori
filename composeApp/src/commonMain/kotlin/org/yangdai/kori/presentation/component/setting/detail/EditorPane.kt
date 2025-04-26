package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.default_view
import kori.composeapp.generated.resources.default_view_for_note
import kori.composeapp.generated.resources.editing_view
import kori.composeapp.generated.resources.line_numbers
import kori.composeapp.generated.resources.lint
import kori.composeapp.generated.resources.lint_description
import kori.composeapp.generated.resources.reading_view
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.util.Constants

@Composable
fun EditorPane(settingsViewModel: SettingsViewModel) {

    val editorPaneState by settingsViewModel.editorPaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Column(
            Modifier.fillMaxWidth().padding(bottom = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.large
                )
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        modifier = Modifier.basicMarquee(),
                        text = stringResource(Res.string.default_view),
                        maxLines = 1
                    )
                },
                supportingContent = {
                    Text(stringResource(Res.string.default_view_for_note))
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            val viewOptions =
                listOf(
                    stringResource(Res.string.editing_view),
                    stringResource(Res.string.reading_view)
                )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                viewOptions.forEachIndexed { index, option ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = viewOptions.size
                        ),
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            settingsViewModel.putPreferenceValue(
                                Constants.Preferences.IS_DEFAULT_READING_VIEW,
                                index == 1
                            )
                        },
                        selected = editorPaneState.isDefaultReadingView == (index == 1)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (index == 0) Icons.Outlined.EditNote else Icons.AutoMirrored.Outlined.MenuBook,
                                contentDescription = option
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option, maxLines = 1, modifier = Modifier.basicMarquee())
                        }
                    }
                }
            }
        }

        DetailPaneItem(
            modifier = Modifier.padding(bottom = 8.dp),
            title = stringResource(Res.string.line_numbers),
            icon = Icons.Outlined.FormatListNumbered,
            trailingContent = {
                Switch(
                    checked = editorPaneState.showLineNumber,
                    onCheckedChange = { checked ->
                        if (checked)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        else
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        settingsViewModel.putPreferenceValue(
                            Constants.Preferences.SHOW_LINE_NUMBER,
                            checked
                        )
                    }
                )
            }
        )

        DetailPaneItem(
            title = stringResource(Res.string.lint),
            description = stringResource(Res.string.lint_description),
            icon = Icons.Outlined.Spellcheck,
            trailingContent = {
                Switch(
                    checked = editorPaneState.isMarkdownLintEnabled,
                    onCheckedChange = { checked ->
                        if (checked)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        else
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        settingsViewModel.putPreferenceValue(
                            Constants.Preferences.IS_MARKDOWN_LINT_ENABLED,
                            checked
                        )
                    }
                )
            }
        )
    }
}