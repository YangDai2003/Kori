package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cowriter
import kori.composeapp.generated.resources.cowriter_description
import kori.composeapp.generated.resources.host
import kori.composeapp.generated.resources.key
import kori.composeapp.generated.resources.model
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.screen.settings.AiProvider
import org.yangdai.kori.presentation.screen.settings.SettingsViewModel
import org.yangdai.kori.presentation.theme.linkColor
import org.yangdai.kori.presentation.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiPane(settingsViewModel: SettingsViewModel) {

    val aiPaneState by settingsViewModel.aiPaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))

        DetailPaneItem(
            title = stringResource(Res.string.cowriter),
            description = stringResource(Res.string.cowriter_description),
            icon = Icons.Outlined.GeneratingTokens,
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
            Column {
                val features = listOf(
                    "correction" to "纠错",
                    "refining" to "润色",
                    "summary" to "摘要",
                    "translation" to "翻译",
                    "list" to "制作列表",
                    "table" to "制作表格"
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    features.forEach { feature ->
                        FilterChip(
                            selected = aiPaneState.aiFeatures.contains(feature.first),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                if (aiPaneState.aiFeatures.contains(feature.first)) {
                                    settingsViewModel.putPreferenceValue(
                                        Constants.Preferences.AI_FEATURES,
                                        aiPaneState.aiFeatures - feature.first
                                    )
                                } else {
                                    settingsViewModel.putPreferenceValue(
                                        Constants.Preferences.AI_FEATURES,
                                        aiPaneState.aiFeatures + feature.first
                                    )
                                }
                            },
                            label = { Text(feature.second) },
                            leadingIcon = {
                                if (aiPaneState.aiFeatures.contains(feature.first))
                                    Icon(
                                        modifier = Modifier.size(16.dp),
                                        imageVector = Icons.Outlined.Done,
                                        contentDescription = null
                                    )
                                else null
                            }
                        )
                    }
                }
                PrimaryScrollableTabRow(
                    selectedTabIndex = AiProvider.entries.indexOf(aiPaneState.aiProvider),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    edgePadding = 0.dp
                ) {
                    AiProvider.entries.forEach { item ->
                        Tab(
                            selected = item == aiPaneState.aiProvider,
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                settingsViewModel.putPreferenceValue(
                                    Constants.Preferences.AI_PROVIDER,
                                    item.provider
                                )
                            },
                            text = { Text(item.provider) }
                        )
                    }
                }

                val pagerState =
                    rememberPagerState(initialPage = AiProvider.entries.indexOf(aiPaneState.aiProvider)) { AiProvider.entries.size }
                LaunchedEffect(aiPaneState.aiProvider) {
                    val index = AiProvider.entries.indexOf(aiPaneState.aiProvider)
                    if (index != pagerState.currentPage) {
                        pagerState.animateScrollToPage(index)
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.Top,
                    userScrollEnabled = false
                ) { page ->
                    when (page) {
                        AiProvider.entries.indexOf(AiProvider.Gemini) -> {
                            GeminiSettings(settingsViewModel)
                        }

                        AiProvider.entries.indexOf(AiProvider.OpenAI) -> {
                            OpenAISettings(settingsViewModel)
                        }

                        AiProvider.entries.indexOf(AiProvider.Ollama) -> {
                            OllamaSettings(settingsViewModel)
                        }

                        AiProvider.entries.indexOf(AiProvider.LMStudio) -> {
                            LMStudioSettings(settingsViewModel)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun KeyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var showKey by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("API " + stringResource(Res.string.key)) },
        singleLine = true,
        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconToggleButton(
                checked = showKey,
                onCheckedChange = { showKey = it },
                colors = IconButtonDefaults.iconToggleButtonColors().copy(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    checkedContentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = if (showKey) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LinkText(text: String, url: String) {
    val annotatedString = buildAnnotatedString {
        append("* ")
        withLink(
            LinkAnnotation.Url(
                url,
                TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(text)
        }
    }
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(top = 8.dp)
    )
}

// Gemini 设置项
@Composable
private fun GeminiSettings(settingsViewModel: SettingsViewModel) {
    val geminiState by settingsViewModel.geminiState.collectAsStateWithLifecycle()
    var apiKey by remember { mutableStateOf(geminiState.apiKey) }
    var apiHost by remember { mutableStateOf(geminiState.apiHost) }
    var model by remember { mutableStateOf(geminiState.model) }
    Column(Modifier.padding(top = 16.dp)) {
        KeyOutlinedTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.GEMINI_API_KEY, it)
            }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = apiHost,
            onValueChange = {
                apiHost = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.GEMINI_API_HOST, it)
            },
            label = { Text("API " + stringResource(Res.string.host)) },
            placeholder = { Text("https://generativelanguage.googleapis.com", maxLines = 1) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.GEMINI_MODEL, it)
            },
            label = { Text(stringResource(Res.string.model)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LinkText(
            text = "Google AI Studio",
            url = "https://aistudio.google.com/prompts/new_chat"
        )
    }
}

// OpenAI 设置项
@Composable
private fun OpenAISettings(settingsViewModel: SettingsViewModel) {
    val openAiState by settingsViewModel.openAiState.collectAsStateWithLifecycle()
    var apiKey by remember { mutableStateOf(openAiState.apiKey) }
    var apiHost by remember { mutableStateOf(openAiState.apiHost) }
    var model by remember { mutableStateOf(openAiState.model) }
    Column(Modifier.padding(top = 16.dp)) {
        KeyOutlinedTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.OPENAI_API_KEY, it)
            }
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = apiHost,
            onValueChange = {
                apiHost = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.OPENAI_API_HOST, it)
            },
            label = { Text("API " + stringResource(Res.string.host)) },
            placeholder = { Text("https://api.openai.com", maxLines = 1) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.OPENAI_MODEL, it)
            },
            label = { Text(stringResource(Res.string.model)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LinkText(
            text = "OpenAI Platform",
            url = "https://platform.openai.com/docs/overview"
        )
    }
}

// Ollama 设置项
@Composable
private fun OllamaSettings(settingsViewModel: SettingsViewModel) {
    val ollamaState by settingsViewModel.ollamaState.collectAsStateWithLifecycle()
    var baseUrl by remember { mutableStateOf(ollamaState.apiHost) }
    var model by remember { mutableStateOf(ollamaState.model) }
    Column(Modifier.padding(top = 16.dp)) {
        OutlinedTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.OLLAMA_API_HOST, it)
            },
            label = { Text("API " + stringResource(Res.string.host)) },
            placeholder = { Text("http://localhost:11434", maxLines = 1) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.OLLAMA_MODEL, it)
            },
            label = { Text(stringResource(Res.string.model)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LinkText(
            text = "Ollama API",
            url = "https://github.com/ollama/ollama/blob/main/docs/api.md"
        )
    }
}

// LM Studio 设置项
@Composable
private fun LMStudioSettings(settingsViewModel: SettingsViewModel) {
    val lmStudioState by settingsViewModel.lmStudioState.collectAsStateWithLifecycle()
    var baseUrl by remember { mutableStateOf(lmStudioState.apiHost) }
    var model by remember { mutableStateOf(lmStudioState.model) }
    Column(Modifier.padding(top = 16.dp)) {
        OutlinedTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.LMSTUDIO_API_HOST, it)
            },
            label = { Text("API " + stringResource(Res.string.host)) },
            placeholder = { Text("http://127.0.0.1:1234/v1", maxLines = 1) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                settingsViewModel.putPreferenceValue(Constants.Preferences.LMSTUDIO_MODEL, it)
            },
            label = { Text(stringResource(Res.string.model)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        LinkText(
            text = "LM Studio API",
            url = "https://lmstudio.ai/docs/app/api"
        )
    }
}
