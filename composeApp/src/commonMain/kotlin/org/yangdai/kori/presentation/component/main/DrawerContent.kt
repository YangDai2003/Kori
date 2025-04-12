package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.yangdai.kori.data.local.dao.FolderDao.FolderWithNoteCount
import org.yangdai.kori.data.local.entity.FolderEntity
import org.yangdai.kori.presentation.navigation.Screen

/**
 * 抽屉状态数据类，用于统一管理抽屉中需要显示的所有数据
 */
data class DrawerState(
    val allNotesCount: Int = 0,
    val templatesCount: Int = 0,
    val trashNotesCount: Int = 0,
    val foldersWithNoteCounts: List<FolderWithNoteCount> = emptyList(),
    val selectedItem: DrawerItem = DrawerItem.AllNotes
)

@Composable
private fun DrawerItem(
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    label: String,
    badge: String = "",
    isSelected: Boolean,
    onClick: () -> Unit
) = NavigationDrawerItem(
    modifier = Modifier.padding(bottom = 2.dp).padding(NavigationDrawerItemDefaults.ItemPadding),
    icon = {
        Icon(
            modifier = Modifier.padding(horizontal = 4.dp),
            imageVector = icon,
            tint = iconTint,
            contentDescription = "Leading Icon"
        )
    },
    label = {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
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

                    else -> AllNotes // 默认值
                }
            }
        )
    }
}

@Composable
fun DrawerContent(
    drawerState: DrawerState,
    navigateToScreen: (Screen) -> Unit,
    onItemClick: (DrawerItem) -> Unit
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kori",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { navigateToScreen(Screen.Settings) }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Open Settings"
                )
            }
        }

        DrawerItem(
            icon = Icons.Outlined.Book,
            label = "All Notes",
            badge = drawerState.allNotesCount.toString(),
            isSelected = drawerState.selectedItem == DrawerItem.AllNotes,
            onClick = { onItemClick(DrawerItem.AllNotes) }
        )

        DrawerItem(
            icon = Icons.AutoMirrored.Outlined.TextSnippet,
            label = "Templates",
            badge = drawerState.templatesCount.toString(),
            isSelected = drawerState.selectedItem == DrawerItem.Templates,
            onClick = { onItemClick(DrawerItem.Templates) }
        )

        DrawerItem(
            icon = Icons.Outlined.DeleteOutline,
            label = "Trash",
            badge = drawerState.trashNotesCount.toString(),
            isSelected = drawerState.selectedItem == DrawerItem.Trash,
            onClick = { onItemClick(DrawerItem.Trash) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        var isFoldersExpended by remember(drawerState.foldersWithNoteCounts) {
            mutableStateOf(drawerState.foldersWithNoteCounts.isNotEmpty())
        }

        DrawerItem(
            icon = if (!isFoldersExpended) Icons.AutoMirrored.Outlined.KeyboardArrowRight else Icons.Outlined.KeyboardArrowDown,
            label = "Folders",
            badge = drawerState.foldersWithNoteCounts.size.toString(),
            isSelected = false,
            onClick = { isFoldersExpended = !isFoldersExpended }
        )

        AnimatedVisibility(visible = isFoldersExpended) {
            Column {
                drawerState.foldersWithNoteCounts.forEach { folderWithNoteCount ->
                    key(folderWithNoteCount.folder.id) {
                        DrawerItem(
                            icon = Icons.Outlined.Folder,
                            label = folderWithNoteCount.folder.name,
                            badge = folderWithNoteCount.noteCount.toString(),
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
            Text(text = "管理文件夹", textAlign = TextAlign.Center)
        }
    }
}
