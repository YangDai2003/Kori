package org.yangdai.kori.presentation.component.setting

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun SettingsListItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    colors: ListItemColors = ListItemDefaults.colors(containerColor = Color.Transparent),
    onClick: () -> Unit
) = ListItem(
    modifier = modifier.clickable { onClick() },
    headlineContent = {
        Text(
            text = title,
            maxLines = 1
        )
    },
    supportingContent = {
        Text(
            modifier = Modifier.basicMarquee(),
            text = description,
            maxLines = 1
        )
    },
    leadingContent = {
        Icon(
            imageVector = icon,
            contentDescription = null
        )
    },
    colors = colors
)