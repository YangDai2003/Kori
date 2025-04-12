package org.yangdai.kori.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.presentation.viewModel.AppViewModel
import org.yangdai.kori.presentation.component.main.AdaptiveNavigationScreen
import org.yangdai.kori.presentation.component.main.DrawerContent
import org.yangdai.kori.presentation.component.main.DrawerItem
import org.yangdai.kori.presentation.component.main.DrawerState
import org.yangdai.kori.presentation.component.main.MainScreenContent
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.rememberIsScreenSizeLarge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: AppViewModel = koinViewModel<AppViewModel>(),
    navigateToScreen: (Screen) -> Unit
) {
    val scope = rememberCoroutineScope()
    val isLargeScreen = rememberIsScreenSizeLarge()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // 使用自定义的 Saver
    var currentDrawerItem by rememberSaveable(stateSaver = DrawerItem.Saver) {
        mutableStateOf<DrawerItem>(DrawerItem.AllNotes)
    }

    // 获取文件夹列表及其包含的笔记数量
    val foldersWithNoteCounts by viewModel.foldersWithNoteCounts.collectAsState()
    val allNotesCount by viewModel.activeNotesCount.collectAsState()
    val templatesCount by viewModel.templateNotesCount.collectAsState()
    val trashNotesCount by viewModel.trashNotesCount.collectAsState()

    AdaptiveNavigationScreen(
        isLargeScreen = isLargeScreen,
        gesturesEnabled = true,
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                drawerState = DrawerState(
                    allNotesCount = allNotesCount,
                    templatesCount = templatesCount,
                    trashNotesCount = trashNotesCount,
                    foldersWithNoteCounts = foldersWithNoteCounts,
                    selectedItem = currentDrawerItem
                ),
                navigateToScreen = navigateToScreen,
                onItemClick = { item ->
                    currentDrawerItem = item
                    if (!isLargeScreen) {
                        // 如果不是大屏幕，点击后关闭抽屉
                        scope.launch {
                            drawerState.close()
                        }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("我的笔记")
                    },
                    navigationIcon = {
                        if (!isLargeScreen) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open drawer"
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // 创建新笔记时不传递ID
                        navigateToScreen(Screen.Note())
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "创建笔记")
                }
            }
        ) { innerPadding ->
            MainScreenContent(
                viewModel = viewModel,
                innerPadding = innerPadding,
                currentDrawerItem = currentDrawerItem,
                navigateToScreen = navigateToScreen
            )
        }
    }
}
