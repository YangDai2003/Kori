package org.yangdai.kori.presentation.component.setting.detail

import ai.koog.prompt.executor.clients.anthropic.AnthropicClientSettings
import ai.koog.prompt.executor.clients.dashscope.DashscopeClientSettings
import ai.koog.prompt.executor.clients.deepseek.DeepSeekClientSettings
import ai.koog.prompt.executor.clients.google.GoogleClientSettings
import ai.koog.prompt.executor.clients.mistralai.MistralAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.llm.LLMProvider
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import knet.ai.providers.Alibaba
import knet.ai.providers.Anthropic
import knet.ai.providers.DeepSeek
import knet.ai.providers.Google
import knet.ai.providers.LMStudio
import knet.ai.providers.Mistral
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
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
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
                modifier = Modifier.fillMaxWidth().imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pagerState =
                    rememberPagerState(AI.providers.values.indexOf(aiPaneState.llmProvider)) { AI.providers.size }
                PrimaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    edgePadding = 0.dp
                ) {
                    AI.providers.entries.forEachIndexed { i, item ->
                        Tab(
                            modifier = Modifier.clip(CircleShape),
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

                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.Top,
                    contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                    userScrollEnabled = false
                ) { page ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (page) {
                            AI.providers.keys.indexOf(LLMProvider.Google.id) -> {
                                GeminiSettings(mainViewModel)
                            }

                            AI.providers.keys.indexOf(LLMProvider.OpenAI.id) -> {
                                OpenAISettings(mainViewModel)
                            }

                            AI.providers.keys.indexOf(LLMProvider.Anthropic.id) -> {
                                AnthropicSettings(mainViewModel)
                            }

                            AI.providers.keys.indexOf(LLMProvider.Ollama.id) -> {
                                OllamaSettings(mainViewModel)
                            }

                            AI.providers.keys.indexOf(LLMProvider.DeepSeek.id) -> {
                                DeepSeekSettings(mainViewModel)
                            }

                            AI.providers.keys.indexOf(LMStudio.id) -> {
                                LMStudioSettings(mainViewModel)
                            }

                            AI.providers.keys.indexOf(LLMProvider.Alibaba.id) -> {
                                AlibabaSettings(mainViewModel)
                            }

                            AI.providers.keys.indexOf(LLMProvider.MistralAI.id) -> {
                                MistralSettings(mainViewModel)
                            }
                        }
                    }
                }

                if (AI.providers.values.indexOf(aiPaneState.llmProvider) != pagerState.currentPage) {
                    TextButton(
                        onClick = {
                            mainViewModel.putPreferenceValue(
                                Constants.Preferences.AI_PROVIDER,
                                AI.providers.keys.elementAt(pagerState.currentPage)
                            )
                        }
                    ) {
                        Text(stringResource(Res.string.set_as_default))
                    }
                }
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModelTextField(
    modelOptions: Set<String>,
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            label = { Text(stringResource(Res.string.model)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MenuDefaults.groupStandardContainerColor,
            shape = MenuDefaults.standaloneGroupShape,
        ) {
            val optionCount = modelOptions.size
            modelOptions.forEachIndexed { index, option ->
                DropdownMenuItem(
                    shapes = MenuDefaults.itemShape(index, optionCount),
                    text = { Text(option) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    selected = modelOptions.toList()[index] == value,
                    checkedLeadingIcon = { Icon(Icons.Filled.Check, null) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            label = { Text(stringResource(Res.string.model)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MenuDefaults.groupStandardContainerColor,
            shape = MenuDefaults.standaloneGroupShape,
        ) {
            val optionCount = modelOptions.size
            modelOptions.forEachIndexed { index, option ->
                DropdownMenuItem(
                    shapes = MenuDefaults.itemShape(index, optionCount),
                    text = { Text(option) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    selected = modelOptions.toList()[index] == value,
                    checkedLeadingIcon = { Icon(Icons.Filled.Check, null) },
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
private fun GeminiSettings(mainViewModel: MainViewModel) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.GEMINI_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.GEMINI_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.GEMINI_MODEL)) }

    KeyTextField(
        value = apiKey,
        onValueChange = {
            apiKey = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.GEMINI_API_KEY, it)
        }
    )
    UrlTextField(
        value = baseUrl,
        onValueChange = {
            baseUrl = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.GEMINI_BASE_URL, it)
        },
        defaultValue = GoogleClientSettings().baseUrl
    )
    ModelTextField(
        modelOptions = Google.googleModelMap.keys,
        value = model,
        onValueChange = {
            model = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.GEMINI_MODEL, it)
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
}

@Composable
private fun OpenAISettings(mainViewModel: MainViewModel) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.OPENAI_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.OPENAI_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.OPENAI_MODEL)) }

    KeyTextField(
        value = apiKey,
        onValueChange = {
            apiKey = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.OPENAI_API_KEY, it)
        }
    )
    UrlTextField(
        value = baseUrl,
        onValueChange = {
            baseUrl = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.OPENAI_BASE_URL, it)
        },
        defaultValue = OpenAIClientSettings().baseUrl
    )
    ModelTextField(
        modelOptions = OpenAI.openAIModelMap.keys,
        value = model,
        onValueChange = {
            model = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.OPENAI_MODEL, it)
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
}

@Composable
private fun AnthropicSettings(mainViewModel: MainViewModel) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.ANTHROPIC_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.ANTHROPIC_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.ANTHROPIC_MODEL)) }

    KeyTextField(
        value = apiKey,
        onValueChange = {
            apiKey = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.ANTHROPIC_API_KEY, it)
        }
    )
    UrlTextField(
        value = baseUrl,
        onValueChange = {
            baseUrl = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.ANTHROPIC_BASE_URL, it)
        },
        defaultValue = AnthropicClientSettings().baseUrl
    )
    ModelTextField(
        modelOptions = Anthropic.anthropicModelMap.keys,
        value = model,
        onValueChange = {
            model = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.ANTHROPIC_MODEL, it)
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
}

@Composable
private fun DeepSeekSettings(mainViewModel: MainViewModel) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.DEEPSEEK_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.DEEPSEEK_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.DEEPSEEK_MODEL)) }

    KeyTextField(
        value = apiKey,
        onValueChange = {
            apiKey = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.DEEPSEEK_API_KEY, it)
        }
    )
    UrlTextField(
        value = baseUrl,
        onValueChange = {
            baseUrl = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.DEEPSEEK_BASE_URL, it)
        },
        defaultValue = DeepSeekClientSettings().baseUrl
    )
    ModelTextField(
        modelOptions = DeepSeek.deepSeekModelMap.keys,
        value = model,
        onValueChange = {
            model = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.DEEPSEEK_MODEL, it)
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
}

@Composable
private fun OllamaSettings(mainViewModel: MainViewModel) {

    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.OLLAMA_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.OLLAMA_MODEL)) }

    UrlTextField(
        value = baseUrl,
        onValueChange = {
            baseUrl = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.OLLAMA_BASE_URL, it)
        },
        defaultValue = "http://localhost:11434"
    )
    ModelTextField(
        modelOptions = Ollama.ollamaModelMap.keys,
        value = model,
        onValueChange = {
            model = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.OLLAMA_MODEL, it)
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
}

@Composable
private fun LMStudioSettings(mainViewModel: MainViewModel) {

    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.LM_STUDIO_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.LM_STUDIO_MODEL)) }

    UrlTextField(
        value = baseUrl,
        onValueChange = {
            baseUrl = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.LM_STUDIO_BASE_URL, it)
        },
        defaultValue = "http://127.0.0.1:1234"
    )
    DynamicModelTextField(
        baseUrl = baseUrl,
        value = model,
        onValueChange = {
            model = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.LM_STUDIO_MODEL, it)
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
}

@Composable
private fun AlibabaSettings(mainViewModel: MainViewModel) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.ALIBABA_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.ALIBABA_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.ALIBABA_MODEL)) }

    KeyTextField(
        value = apiKey,
        onValueChange = {
            apiKey = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.ALIBABA_API_KEY, it)
        }
    )
    UrlTextField(
        value = baseUrl,
        onValueChange = {
            baseUrl = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.ALIBABA_BASE_URL, it)
        },
        defaultValue = DashscopeClientSettings().baseUrl
    )
    ModelTextField(
        modelOptions = Alibaba.alibabaModelMap.keys,
        value = model,
        onValueChange = {
            model = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.ALIBABA_MODEL, it)
        }
    )
    LinkText(
        text = "Alibaba Cloud",
        url = "https://qwen.ai/apiplatform"
    )
    TestConnectionColumn(
        llmProvider = LLMProvider.Alibaba,
        apiKey = apiKey,
        baseUrl = baseUrl,
        model = model
    )
}

@Composable
private fun MistralSettings(mainViewModel: MainViewModel) {

    var apiKey by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.MISTRAL_API_KEY)) }
    var baseUrl by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.MISTRAL_BASE_URL)) }
    var model by remember { mutableStateOf(mainViewModel.getStringValue(Constants.LLMConfig.MISTRAL_MODEL)) }

    KeyTextField(
        value = apiKey,
        onValueChange = {
            apiKey = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.MISTRAL_API_KEY, it)
        }
    )
    UrlTextField(
        value = baseUrl,
        onValueChange = {
            baseUrl = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.MISTRAL_BASE_URL, it)
        },
        defaultValue = MistralAIClientSettings().baseUrl
    )
    ModelTextField(
        modelOptions = Mistral.mistralModelMap.keys,
        value = model,
        onValueChange = {
            model = it
            mainViewModel.putPreferenceValue(Constants.LLMConfig.MISTRAL_MODEL, it)
        }
    )
    LinkText(
        text = "Mistral AI Documentation",
        url = "https://docs.mistral.ai/"
    )
    TestConnectionColumn(
        llmProvider = LLMProvider.MistralAI,
        apiKey = apiKey,
        baseUrl = baseUrl,
        model = model
    )
}