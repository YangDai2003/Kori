package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.util.Constants

@Composable
fun AiPane(settingsViewModel: SettingsViewModel) {

    val aiPaneState by settingsViewModel.aiPaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))

        DetailPaneItem(
            title = "启用写作助手",
            description = "接入第三方生成式人工智能云服务",
            icon = Icons.Outlined.GeneratingTokens,
            trailingContent = {
                Switch(
                    checked = aiPaneState.isAiEnabled,
                    onCheckedChange = { checked ->
                        if (checked)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        else
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                        settingsViewModel.putPreferenceValue(
                            Constants.Preferences.IS_AI_ENABLED,
                            checked
                        )
                    }
                )
            }
        )

        AnimatedVisibility(visible = aiPaneState.isAiEnabled) {
            // TODO
            Column {
                val features = listOf("纠错", "润色", "摘要", "翻译", "制作列表", "制作表格")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    features.forEachIndexed { index, feature ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            },
                            label = { Text(feature) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Done,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
                val tabItems = listOf("Gemini", "OpenAI", "Ollama", "LM Studio")
                TabRow(
                    selectedTabIndex = 0,
                ) {
                    tabItems.forEachIndexed { index, title ->
                        Tab(
                            selected = index == 0,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.navigationBarsPadding())

    }
}