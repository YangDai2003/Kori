package org.yangdai.kori.presentation.component.setting

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ListPaneItem(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    title: String,
    description: String,
    icon: ImageVector,
    colors: ListItemColors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
    onClick: (() -> Unit)? = null
) = ListItem(
    modifier = modifier.clip(MaterialTheme.shapes.extraSmall)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    headlineContent = { Text(text = title, maxLines = 1) },
    supportingContent = {
        Text(
            modifier = Modifier.basicMarquee(),
            text = description,
            maxLines = 1
        )
    },
    leadingContent = {
        Box(contentAlignment = Alignment.Center) {
            val circleSize by animateDpAsState(targetValue = if (isSelected) 40.dp else 0.dp)
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else LocalContentColor.current
            )
        }
    },
    colors = colors
)

@Composable
fun DetailPaneItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    trailingContent: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null
) = ListItem(
    modifier = modifier.clip(MaterialTheme.shapes.large)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    headlineContent = { Text(title) },
    supportingContent = description?.let { { Text(description) } },
    leadingContent = icon?.let {
        {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    },
    trailingContent = trailingContent,
    colors = colors
)

@Composable
fun DetailPaneItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: Painter? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    trailingContent: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null
) = ListItem(
    modifier = modifier.clip(MaterialTheme.shapes.large)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    headlineContent = { Text(title) },
    supportingContent = description?.let { { Text(description) } },
    leadingContent = icon?.let {
        {
            Icon(
                painter = icon,
                contentDescription = null
            )
        }
    },
    trailingContent = trailingContent,
    colors = colors
)