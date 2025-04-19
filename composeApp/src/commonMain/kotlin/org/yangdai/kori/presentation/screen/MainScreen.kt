package org.yangdai.kori.presentation.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.open_navigation_drawer
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.component.main.AdaptiveNavigationDrawerScreen
import org.yangdai.kori.presentation.component.main.DrawerItem
import org.yangdai.kori.presentation.component.main.DrawerState
import org.yangdai.kori.presentation.component.main.MainScreenContent
import org.yangdai.kori.presentation.component.main.NavigationDrawerContent
import org.yangdai.kori.presentation.navigation.Screen
import org.yangdai.kori.presentation.util.rememberIsScreenSizeLarge
import org.yangdai.kori.presentation.viewModel.AppViewModel

@Composable
fun MainScreen(
    viewModel: AppViewModel = koinViewModel(),
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
    val foldersWithNoteCounts by viewModel.foldersWithNoteCounts.collectAsStateWithLifecycle()
    val allNotesCount by viewModel.activeNotesCount.collectAsStateWithLifecycle()
    val templatesCount by viewModel.templateNotesCount.collectAsStateWithLifecycle()
    val trashNotesCount by viewModel.trashNotesCount.collectAsStateWithLifecycle()

    AdaptiveNavigationDrawerScreen(
        isLargeScreen = isLargeScreen,
        gesturesEnabled = true,
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
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
        MainScreenContent(
            viewModel = viewModel,
            currentDrawerItem = currentDrawerItem,
            navigationIcon = {
                if (!isLargeScreen) {
                    TooltipIconButton(
                        tipText = stringResource(Res.string.open_navigation_drawer),
                        icon = Icons.Default.Menu,
                        onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
            },
            navigateToScreen = navigateToScreen
        )
    }
}
