package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import org.yangdai.kori.presentation.component.SegmentText
import org.yangdai.kori.presentation.component.SegmentedControl
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

        Spacer(Modifier.height(16.dp))

        Column(
            Modifier.fillMaxWidth().padding(bottom = 8.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        imageVector = if (editorPaneState.isDefaultReadingView) Icons.AutoMirrored.Outlined.MenuBook
                        else Icons.Outlined.EditNote,
                        contentDescription = null
                    )
                },
                headlineContent = {
                    Text(
                        modifier = Modifier.basicMarquee(),
                        text = stringResource(Res.string.default_view),
                        maxLines = 1
                    )
                },
                supportingContent = {
                    Text(stringResource(Res.string.default_view_for_note))
                }
            )

            val viewOptions =
                listOf(
                    stringResource(Res.string.editing_view),
                    stringResource(Res.string.reading_view)
                )

            SegmentedControl(
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                segments = viewOptions,
                selectedSegmentIndex = if (editorPaneState.isDefaultReadingView) 1 else 0,
                onSegmentSelected = { index ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    settingsViewModel.putPreferenceValue(
                        Constants.Preferences.IS_DEFAULT_READING_VIEW,
                        index == 1
                    )
                },
                content = { SegmentText(it) }
            )
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

        Spacer(Modifier.height(8.dp))
    }
}