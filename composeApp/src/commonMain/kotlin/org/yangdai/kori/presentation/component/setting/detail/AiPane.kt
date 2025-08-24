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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cowriter
import kori.composeapp.generated.resources.cowriter_description
import kori.composeapp.generated.resources.host
import kori.composeapp.generated.resources.key
import kori.composeapp.generated.resources.make_list
import kori.composeapp.generated.resources.make_table
import kori.composeapp.generated.resources.model
import kori.composeapp.generated.resources.reset
import kori.composeapp.generated.resources.rewrite
import kori.composeapp.generated.resources.summarize
import kori.composeapp.generated.resources.translate
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.screen.settings.AiProvider
import org.yangdai.kori.presentation.util.Constants

@Composable
fun AiPane(mainViewModel: MainViewModel) {

    val aiPaneState by mainViewModel.aiPaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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
                        mainViewModel.putPreferenceValue(
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
                    "rewrite" to stringResource(Res.string.rewrite),
                    "summarize" to stringResource(Res.string.summarize),
                    "translate" to stringResource(Res.string.translate),
                    "list" to stringResource(Res.string.make_list),
                    "table" to stringResource(Res.string.make_table),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    features.forEach { feature ->
                        FilterChip(
                            selected = aiPaneState.aiFeatures.contains(feature.first),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                if (aiPaneState.aiFeatures.contains(feature.first)) {
                                    mainViewModel.putPreferenceValue(
                                        Constants.Preferences.AI_FEATURES,
                                        aiPaneState.aiFeatures - feature.first
                                    )
                                } else {
                                    mainViewModel.putPreferenceValue(
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
                                mainViewModel.putPreferenceValue(
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
                            GeminiSettings(mainViewModel)
                        }

                        AiProvider.entries.indexOf(AiProvider.OpenAI) -> {
                            OpenAISettings(mainViewModel)
                        }

                        AiProvider.entries.indexOf(AiProvider.Ollama) -> {
                            OllamaSettings(mainViewModel)
                        }

                        AiProvider.entries.indexOf(AiProvider.LMStudio) -> {
                            LMStudioSettings(mainViewModel)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun KeyTextField(value: String, onValueChange: (String) -> Unit) {
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
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@Composable
private fun LinkText(text: String, url: String) = Text(
    text = buildAnnotatedString {
        append("* ")
        withLink(LinkAnnotation.Url(url)) {
            append(text)
        }
    },
    style = MaterialTheme.typography.labelSmall,
    modifier = Modifier.padding(top = 8.dp)
)

@Composable
private fun UrlTextField(
    value: String,
    onValueChange: (String) -> Unit,
    defaultValue: String
) = OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text("API " + stringResource(Res.string.host)) },
    placeholder = { Text(defaultValue, maxLines = 1) },
    trailingIcon = {
        if (value != defaultValue)
            TextButton(onClick = { onValueChange(defaultValue) }) {
                Text(stringResource(Res.string.reset))
            }
    },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
)

// Gemini 设置项
@Composable
private fun GeminiSettings(mainViewModel: MainViewModel) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.GEMINI_API_KEY)) }
    var apiHost by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.GEMINI_API_HOST)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.GEMINI_MODEL)) }

    Column(Modifier.padding(top = 16.dp)) {
        KeyTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                mainViewModel.putPreferenceValue(Constants.Preferences.GEMINI_API_KEY, it)
            }
        )
        UrlTextField(
            value = apiHost,
            onValueChange = {
                apiHost = it
                mainViewModel.putPreferenceValue(Constants.Preferences.GEMINI_API_HOST, it)
            },
            defaultValue = "https://generativelanguage.googleapis.com"
        )
        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.GEMINI_MODEL, it)
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
private fun OpenAISettings(mainViewModel: MainViewModel) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OPENAI_API_KEY)) }
    var apiHost by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OPENAI_API_HOST)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OPENAI_MODEL)) }

    Column(Modifier.padding(top = 16.dp)) {
        KeyTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OPENAI_API_KEY, it)
            }
        )
        UrlTextField(
            value = apiHost,
            onValueChange = {
                apiHost = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OPENAI_API_HOST, it)
            },
            defaultValue = "https://api.openai.com"
        )
        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OPENAI_MODEL, it)
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
private fun OllamaSettings(mainViewModel: MainViewModel) {

    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OLLAMA_API_HOST)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OLLAMA_MODEL)) }

    Column(Modifier.padding(top = 16.dp)) {
        UrlTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OLLAMA_API_HOST, it)
            },
            defaultValue = "http://localhost:11434"
        )
        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OLLAMA_MODEL, it)
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
private fun LMStudioSettings(mainViewModel: MainViewModel) {

    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.LMSTUDIO_API_HOST)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.LMSTUDIO_MODEL)) }

    Column(Modifier.padding(top = 16.dp)) {
        UrlTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                mainViewModel.putPreferenceValue(Constants.Preferences.LMSTUDIO_API_HOST, it)
            },
            defaultValue = "http://127.0.0.1:1234/v1"
        )
        OutlinedTextField(
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.LMSTUDIO_MODEL, it)
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
