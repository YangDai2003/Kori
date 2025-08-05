package org.yangdai.kori.presentation.component.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.yangdai.kori.LocalWindowState

@Composable
actual fun WidgetListItem() {
}

@Composable
actual fun InkListItem() {
    val windowState = LocalWindowState.current
    ListItem(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(CardDefaults.shape)
            .clickable {
                windowState.inkWindow.value = true
            },
        headlineContent = { Text("Ink Playground") },
        supportingContent = { Text("This is an experimental feature and may be changed, presented in a different form, or even removed entirely.") },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                contentDescription = null
            )
        }
    )
}