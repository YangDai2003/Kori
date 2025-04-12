package org.yangdai.kori.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.AppViewModel
import org.yangdai.kori.presentation.component.NavigateUpButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    noteId: String? = null,
    viewModel: AppViewModel = koinViewModel<AppViewModel>(),
    navigateUp: () -> Unit
) {
    
    // 修复 LaunchedEffect，正确加载笔记数据
    LaunchedEffect(noteId) {
        viewModel.getNoteById(noteId)
    }

    val isEditMode = remember(noteId) { noteId != null }
    val screenTitle = if (isEditMode) "编辑笔记" else "新建笔记"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(screenTitle)
                },
                navigationIcon = {
                    NavigateUpButton(onClick = navigateUp)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val title = viewModel.titleState.text.toString()
                val content = viewModel.contentState.text.toString()
                
                // 如果标题和内容都为空，则不保存
                if (title.isBlank() && content.isBlank()) {
                    navigateUp()
                    return@FloatingActionButton
                }
                
                if (isEditMode) {
                    // 更新现有笔记
                    noteId?.let {
                        viewModel.updateNote(it, title, content)
                    }
                } else {
                    // 创建新笔记
                    viewModel.createNote(title, content)
                }
                navigateUp()
            }) {
                Icon(Icons.Default.Check, contentDescription = "保存")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
            BasicTextField(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                state = viewModel.titleState,
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                lineLimits = TextFieldLineLimits.SingleLine,
                decorator = { innerTextField ->
                    Box {
                        if (viewModel.titleState.text.isEmpty()) {
                            Text(
                                text = "标题",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            BasicTextField(
                modifier = Modifier.fillMaxWidth().weight(1f),
                state = viewModel.contentState,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorator = { innerTextField ->
                    Box {
                        if (viewModel.contentState.text.isEmpty()) {
                            Text(
                                text = "开始输入笔记内容...",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 16.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
