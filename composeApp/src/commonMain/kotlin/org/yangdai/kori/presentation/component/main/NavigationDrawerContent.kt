package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.all_notes
import kori.composeapp.generated.resources.app_name
import kori.composeapp.generated.resources.folders
import kori.composeapp.generated.resources.lock
import kori.composeapp.generated.resources.manage_folders
import kori.composeapp.generated.resources.settings
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.toolbox
import kori.composeapp.generated.resources.trash
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.data.local.dao.FolderDao.FolderWithNoteCount
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.data.local.entity.defaultFolderColor
import org.yangdai.kori.isDesktop
import org.yangdai.kori.presentation.component.TooltipIconButton
import org.yangdai.kori.presentation.navigation.Screen

/**
 * 抽屉状态数据类，用于统一管理抽屉中需要显示的所有数据
 */
@Immutable
data class DrawerState(
    val allNotesCount: Int = 0,
    val templatesCount: Int = 0,
    val trashNotesCount: Int = 0,
    val foldersWithNoteCounts: List<FolderWithNoteCount> = emptyList(),
    val selectedItem: DrawerItem = DrawerItem.AllNotes,
    val isAppProtected: Boolean = false
)

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    badge: String = "",
    isSelected: Boolean,
    onClick: () -> Unit
) = NavigationDrawerItem(
    modifier = Modifier
        .padding(bottom = 4.dp)
        .padding(NavigationDrawerItemDefaults.ItemPadding)
        .height(52.dp),
    icon = { Icon(imageVector = icon, contentDescription = null) },
    label = {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    },
    badge = {
        Text(
            text = badge,
            style = MaterialTheme.typography.labelMedium
        )
    },
    shape = MaterialTheme.shapes.medium,
    selected = isSelected,
    onClick = onClick
)

@Composable
private fun FolderDrawerItem(
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    label: String,
    badge: String = "",
    isStarred: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) = NavigationDrawerItem(
    modifier = Modifier
        .padding(bottom = 4.dp)
        .padding(NavigationDrawerItemDefaults.ItemPadding)
        .height(52.dp),
    icon = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSelected) Icons.Outlined.FolderOpen else Icons.Outlined.Folder,
                tint = iconTint,
                contentDescription = null
            )
            if (isStarred) {
                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(14.dp),
                    imageVector = Icons.Outlined.Star,
                    tint = MaterialTheme.colorScheme.tertiary,
                    contentDescription = null
                )
            }
        }
    },
    label = {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    },
    badge = {
        Text(
            text = badge,
            style = MaterialTheme.typography.labelMedium
        )
    },
    shape = MaterialTheme.shapes.medium,
    selected = isSelected,
    onClick = onClick
)


sealed class DrawerItem(val id: String) {
    data object AllNotes : DrawerItem("all_notes")
    data object Templates : DrawerItem("templates")
    data object Trash : DrawerItem("trash")
    data class Folder(val folder: FolderEntity) : DrawerItem(folder.id)
    data object Toolbox : DrawerItem("toolbox")

    // 将drawerItemSaver改为伴生对象
    companion object {
        val Saver: Saver<DrawerItem, Any> = listSaver(
            save = { drawerItem ->
                when (drawerItem) {
                    is AllNotes -> listOf("AllNotes")
                    is Templates -> listOf("Templates")
                    is Trash -> listOf("Trash")
                    is Folder -> listOf(
                        "Folder",
                        drawerItem.folder.id,
                        drawerItem.folder.name,
                        drawerItem.folder.colorValue,
                        drawerItem.folder.createdAt
                    )

                    is Toolbox -> listOf("Toolbox")
                }
            },
            restore = { state ->
                when (state[0]) {
                    "AllNotes" -> AllNotes
                    "Templates" -> Templates
                    "Trash" -> Trash
                    "Folder" -> Folder(
                        FolderEntity(
                            id = state[1] as String,
                            name = state[2] as String,
                            colorValue = (state[3] as Number).toLong(),
                            createdAt = state[4] as String
                        )
                    )

                    "Toolbox" -> Toolbox

                    else -> AllNotes // 默认值
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavigationDrawerContent(
    drawerState: DrawerState,
    onLockClick: () -> Unit,
    navigateToScreen: (Screen) -> Unit,
    onItemClick: (DrawerItem) -> Unit
) = Column(Modifier.verticalScroll(rememberScrollState())) {

    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentPlatformInfo.operatingSystem == OS.MACOS)
            Spacer(Modifier.weight(1f))
        else
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSynthesis = FontSynthesis.Weight,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )
        if (drawerState.isAppProtected)
            TooltipIconButton(
                hint = stringResource(Res.string.lock),
                actionText = if (currentPlatformInfo.isDesktop()) "⇧ + L" else "",
                icon = Icons.Outlined.Lock,
                shape = IconButtonDefaults.smallSquareShape,
                onClick = onLockClick
            )
        TooltipIconButton(
            hint = stringResource(Res.string.settings),
            icon = Icons.Outlined.Settings,
            shape = IconButtonDefaults.smallSquareShape,
            onClick = { navigateToScreen(Screen.Settings) }
        )
    }

    DrawerItem(
        icon = Icons.Outlined.Book,
        label = stringResource(Res.string.all_notes),
        badge = drawerState.allNotesCount.toString(),
        isSelected = drawerState.selectedItem == DrawerItem.AllNotes,
        onClick = { onItemClick(DrawerItem.AllNotes) }
    )

    DrawerItem(
        icon = Icons.AutoMirrored.Outlined.TextSnippet,
        label = stringResource(Res.string.templates),
        badge = drawerState.templatesCount.toString(),
        isSelected = drawerState.selectedItem == DrawerItem.Templates,
        onClick = { onItemClick(DrawerItem.Templates) }
    )

    DrawerItem(
        icon = Icons.Outlined.DeleteOutline,
        label = stringResource(Res.string.trash),
        badge = drawerState.trashNotesCount.toString(),
        isSelected = drawerState.selectedItem == DrawerItem.Trash,
        onClick = { onItemClick(DrawerItem.Trash) }
    )

    DrawerItem(
        icon = Icons.Outlined.BusinessCenter,
        label = stringResource(Res.string.toolbox),
        isSelected = drawerState.selectedItem == DrawerItem.Toolbox,
        onClick = { onItemClick(DrawerItem.Toolbox) }
    )

    val thickness = remember { DividerDefaults.Thickness }
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        Modifier.padding(bottom = 4.dp).padding(horizontal = 8.dp).fillMaxWidth().height(thickness)
    ) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(0f, thickness.toPx() / 2),
            end = Offset(size.width, thickness.toPx() / 2),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }

    var isFoldersExpended by remember(drawerState.foldersWithNoteCounts) {
        mutableStateOf(drawerState.foldersWithNoteCounts.isNotEmpty())
    }

    DrawerItem(
        icon = if (!isFoldersExpended) Icons.AutoMirrored.Outlined.KeyboardArrowRight else Icons.Outlined.KeyboardArrowDown,
        label = stringResource(Res.string.folders),
        badge = drawerState.foldersWithNoteCounts.size.toString(),
        isSelected = false,
        onClick = { isFoldersExpended = !isFoldersExpended }
    )

    AnimatedVisibility(isFoldersExpended) {
        Column {
            drawerState.foldersWithNoteCounts.forEach { folderWithNoteCount ->
                key(folderWithNoteCount.folder.id) {
                    FolderDrawerItem(
                        iconTint = if (folderWithNoteCount.folder.colorValue == defaultFolderColor) MaterialTheme.colorScheme.primary
                        else Color(folderWithNoteCount.folder.colorValue),
                        label = folderWithNoteCount.folder.name,
                        badge = folderWithNoteCount.noteCount.toString(),
                        isStarred = folderWithNoteCount.folder.isStarred,
                        isSelected = drawerState.selectedItem.id == folderWithNoteCount.folder.id,
                        onClick = { onItemClick(DrawerItem.Folder(folderWithNoteCount.folder)) }
                    )
                }
            }
        }
    }

    TextButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(NavigationDrawerItemDefaults.ItemPadding),
        onClick = { navigateToScreen(Screen.Folders) }
    ) {
        Text(text = stringResource(Res.string.manage_folders), textAlign = TextAlign.Center)
    }

    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
}
