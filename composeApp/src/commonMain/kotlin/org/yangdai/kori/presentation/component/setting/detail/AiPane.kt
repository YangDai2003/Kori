package org.yangdai.kori.presentation.component.setting.detail

import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.deepseek.DeepSeekClientSettings
import ai.koog.prompt.executor.clients.google.GoogleClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.llm.LLMProvider
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import knet.ai.AI
import knet.ai.GenerationResult
import knet.ai.providers.Anthropic
import knet.ai.providers.DeepSeek
import knet.ai.providers.Gemini
import knet.ai.providers.LMStudio
import knet.ai.providers.Ollama
import knet.ai.providers.OpenAI
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cowriter
import kori.composeapp.generated.resources.cowriter_description
import kori.composeapp.generated.resources.key
import kori.composeapp.generated.resources.model
import kori.composeapp.generated.resources.reset
import kori.composeapp.generated.resources.set_as_default
import kori.composeapp.generated.resources.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.screen.main.MainViewModel
import org.yangdai.kori.presentation.util.Constants

@Composable
fun AiPane(mainViewModel: MainViewModel) {

    val aiPaneState by mainViewModel.aiPaneState.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .imePadding()
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            .verticalScroll(rememberScrollState())
    ) {

        DetailPaneItem(
            title = stringResource(Res.string.cowriter),
            description = stringResource(Res.string.cowriter_description),
            icon = Icons.Outlined.GeneratingTokens,
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pagerState = rememberPagerState { AI.providers.size }
                PrimaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    divider = {},
                    edgePadding = 0.dp
                ) {
                    AI.providers.entries.forEachIndexed { i, item ->
                        Tab(
                            modifier = Modifier.clip(MaterialTheme.shapes.large),
                            selected = i == pagerState.currentPage,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(i)
                                }
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (aiPaneState.llmProvider == item.value)
                                        Icon(
                                            modifier = Modifier.padding(end = 4.dp).size(16.dp),
                                            imageVector = Icons.Default.Favorite,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            contentDescription = null
                                        )
                                    Text(item.value.display)
                                }
                            }
                        )
                    }
                }

                HorizontalDivider()

                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.Top,
                    userScrollEnabled = false
                ) { page ->
                    when (page) {
                        AI.providers.keys.indexOf(LLMProvider.Google.id) -> {
                            GeminiSettings(mainViewModel, aiPaneState.llmProvider)
                        }

                        AI.providers.keys.indexOf(LLMProvider.OpenAI.id) -> {
                            OpenAISettings(mainViewModel, aiPaneState.llmProvider)
                        }

                        AI.providers.keys.indexOf(LLMProvider.Anthropic.id) -> {
                            AnthropicSettings(mainViewModel, aiPaneState.llmProvider)
                        }

                        AI.providers.keys.indexOf(LLMProvider.Ollama.id) -> {
                            OllamaSettings(mainViewModel, aiPaneState.llmProvider)
                        }

                        AI.providers.keys.indexOf(LLMProvider.DeepSeek.id) -> {
                            DeepSeekSettings(mainViewModel, aiPaneState.llmProvider)
                        }

                        AI.providers.keys.indexOf(LMStudio.id) -> {
                            LMStudioSettings(mainViewModel, aiPaneState.llmProvider)
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
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
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
    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
)

@Composable
private fun UrlTextField(
    value: String,
    onValueChange: (String) -> Unit,
    defaultValue: String
) = OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text("Base URL") },
    placeholder = { Text(defaultValue, maxLines = 1) },
    trailingIcon = if (value != defaultValue) {
        {
            TextButton(onClick = { onValueChange(defaultValue) }) {
                Text(stringResource(Res.string.reset))
            }
        }
    } else {
        null
    },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
    singleLine = true,
    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelTextField(
    modelOptions: Set<String>,
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.model)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            modelOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DynamicModelTextField(
    baseUrl: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var modelOptions by remember { mutableStateOf(emptyList<String>()) }

    LaunchedEffect(expanded) {
        if (!expanded) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            modelOptions = AI.getAvailableModels(baseUrl)
        }
    }

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.model)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            modelOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TestConnectionColumn(
    llmProvider: LLMProvider,
    model: String,
    apiKey: String = "",
    baseUrl: String = ""
) {
    val scope = rememberCoroutineScope()
    var isTesting by remember { mutableStateOf(false) }
    var generateResult by remember { mutableStateOf<GenerationResult?>(null) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledTonalButton(
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isTesting || generateResult == null) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    when (generateResult) {
                        is GenerationResult.Error -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                }
            ),
            onClick = {
                scope.launch(Dispatchers.IO) {
                    isTesting = true
                    generateResult = AI.testConnection(
                        lLMProvider = llmProvider,
                        model = model,
                        apiKey = apiKey,
                        baseUrl = baseUrl
                    )
                    isTesting = false
                }
            }
        ) {
            AnimatedContent(isTesting) {
                if (it) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(Res.string.test))
                }
            }
        }

        AnimatedVisibility(generateResult != null) {
            generateResult?.let {
                Text(
                    text = when (it) {
                        is GenerationResult.Error -> "Failure\n" + it.errorMessage
                        is GenerationResult.Success -> "Success\n" + it.text
                    }
                )
            }
        }
    }
}

@Composable
private fun GeminiSettings(mainViewModel: MainViewModel, defaultProvider: LLMProvider) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.GEMINI_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.GEMINI_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.GEMINI_MODEL)) }

    Column(Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        KeyTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                mainViewModel.putPreferenceValue(Constants.Preferences.GEMINI_API_KEY, it)
            }
        )
        UrlTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                mainViewModel.putPreferenceValue(Constants.Preferences.GEMINI_BASE_URL, it)
            },
            defaultValue = GoogleClientSettings().baseUrl
        )
        ModelTextField(
            modelOptions = Gemini.modelOptions.keys,
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.GEMINI_MODEL, it)
            }
        )
        LinkText(
            text = "Google AI Studio",
            url = "https://aistudio.google.com/prompts/new_chat"
        )
        TestConnectionColumn(
            llmProvider = LLMProvider.Google,
            apiKey = apiKey,
            baseUrl = baseUrl,
            model = model
        )
        if (defaultProvider != LLMProvider.Google) {
            TextButton(
                onClick = {
                    mainViewModel.putPreferenceValue(
                        Constants.Preferences.AI_PROVIDER,
                        LLMProvider.Google.id
                    )
                }
            ) {
                Text(stringResource(Res.string.set_as_default))
            }
        }
    }
}

@Composable
private fun OpenAISettings(mainViewModel: MainViewModel, defaultProvider: LLMProvider) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OPENAI_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OPENAI_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OPENAI_MODEL)) }

    Column(Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        KeyTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OPENAI_API_KEY, it)
            }
        )
        UrlTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OPENAI_BASE_URL, it)
            },
            defaultValue = OpenAIClientSettings().baseUrl
        )
        ModelTextField(
            modelOptions = OpenAI.modelOptions.keys,
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OPENAI_MODEL, it)
            }
        )
        LinkText(
            text = "OpenAI Platform",
            url = "https://platform.openai.com/docs/overview"
        )
        TestConnectionColumn(
            llmProvider = LLMProvider.OpenAI,
            apiKey = apiKey,
            baseUrl = baseUrl,
            model = model
        )
        if (defaultProvider != LLMProvider.OpenAI) {
            TextButton(
                onClick = {
                    mainViewModel.putPreferenceValue(
                        Constants.Preferences.AI_PROVIDER,
                        LLMProvider.OpenAI.id
                    )
                }
            ) {
                Text(stringResource(Res.string.set_as_default))
            }
        }
    }
}

@Composable
private fun AnthropicSettings(mainViewModel: MainViewModel, defaultProvider: LLMProvider) {
    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.ANTHROPIC_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.ANTHROPIC_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.ANTHROPIC_MODEL)) }

    Column(Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        KeyTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                mainViewModel.putPreferenceValue(Constants.Preferences.ANTHROPIC_API_KEY, it)
            }
        )
        UrlTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                mainViewModel.putPreferenceValue(Constants.Preferences.ANTHROPIC_BASE_URL, it)
            },
            defaultValue = AnthropicClientSettings().baseUrl
        )
        ModelTextField(
            modelOptions = Anthropic.modelOptions.keys,
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.ANTHROPIC_MODEL, it)
            }
        )
        LinkText(
            text = "Anthropic Console",
            url = "https://console.anthropic.com"
        )
        TestConnectionColumn(
            llmProvider = LLMProvider.Anthropic,
            apiKey = apiKey,
            baseUrl = baseUrl,
            model = model
        )
        if (defaultProvider != LLMProvider.Anthropic) {
            TextButton(
                onClick = {
                    mainViewModel.putPreferenceValue(
                        Constants.Preferences.AI_PROVIDER,
                        LLMProvider.Anthropic.id
                    )
                }
            ) {
                Text(stringResource(Res.string.set_as_default))
            }
        }
    }
}

@Composable
private fun DeepSeekSettings(mainViewModel: MainViewModel, defaultProvider: LLMProvider) {
    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.DEEPSEEK_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.DEEPSEEK_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.DEEPSEEK_MODEL)) }

    Column(Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        KeyTextField(
            value = apiKey,
            onValueChange = {
                apiKey = it
                mainViewModel.putPreferenceValue(Constants.Preferences.DEEPSEEK_API_KEY, it)
            }
        )
        UrlTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                mainViewModel.putPreferenceValue(Constants.Preferences.DEEPSEEK_BASE_URL, it)
            },
            defaultValue = DeepSeekClientSettings().baseUrl
        )
        ModelTextField(
            modelOptions = DeepSeek.modelOptions.keys,
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.DEEPSEEK_MODEL, it)
            }
        )
        LinkText(
            text = "DeepSeek Platform",
            url = "https://platform.deepseek.com"
        )
        TestConnectionColumn(
            llmProvider = LLMProvider.DeepSeek,
            apiKey = apiKey,
            baseUrl = baseUrl,
            model = model
        )
        if (defaultProvider != LLMProvider.DeepSeek) {
            TextButton(
                onClick = {
                    mainViewModel.putPreferenceValue(
                        Constants.Preferences.AI_PROVIDER,
                        LLMProvider.DeepSeek.id
                    )
                }
            ) {
                Text(stringResource(Res.string.set_as_default))
            }
        }
    }
}

@Composable
private fun OllamaSettings(mainViewModel: MainViewModel, defaultProvider: LLMProvider) {

    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OLLAMA_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.OLLAMA_MODEL)) }

    Column(Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        UrlTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OLLAMA_BASE_URL, it)
            },
            defaultValue = "http://localhost:11434"
        )
        ModelTextField(
            modelOptions = Ollama.modelOptions.keys,
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.OLLAMA_MODEL, it)
            }
        )
        LinkText(
            text = "Ollama API",
            url = "https://github.com/ollama/ollama/blob/main/docs/api.md"
        )
        TestConnectionColumn(
            llmProvider = LLMProvider.Ollama,
            baseUrl = baseUrl,
            model = model
        )
        if (defaultProvider != LLMProvider.Ollama) {
            TextButton(
                onClick = {
                    mainViewModel.putPreferenceValue(
                        Constants.Preferences.AI_PROVIDER,
                        LLMProvider.Ollama.id
                    )
                }
            ) {
                Text(stringResource(Res.string.set_as_default))
            }
        }
    }
}

@Composable
private fun LMStudioSettings(mainViewModel: MainViewModel, defaultProvider: LLMProvider) {

    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.LM_STUDIO_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.Preferences.LM_STUDIO_MODEL)) }

    Column(Modifier.padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        UrlTextField(
            value = baseUrl,
            onValueChange = {
                baseUrl = it
                mainViewModel.putPreferenceValue(Constants.Preferences.LM_STUDIO_BASE_URL, it)
            },
            defaultValue = "http://127.0.0.1:1234"
        )
        DynamicModelTextField(
            baseUrl = baseUrl,
            value = model,
            onValueChange = {
                model = it
                mainViewModel.putPreferenceValue(Constants.Preferences.LM_STUDIO_MODEL, it)
            }
        )
        LinkText(
            text = "LM Studio Docs",
            url = "https://lmstudio.ai/docs/app/api/endpoints/openai"
        )
        TestConnectionColumn(
            llmProvider = LMStudio,
            baseUrl = baseUrl,
            model = model
        )
        if (defaultProvider != LMStudio) {
            TextButton(
                onClick = {
                    mainViewModel.putPreferenceValue(
                        Constants.Preferences.AI_PROVIDER,
                        LMStudio.id
                    )
                }
            ) {
                Text(stringResource(Res.string.set_as_default))
            }
        }
    }
}