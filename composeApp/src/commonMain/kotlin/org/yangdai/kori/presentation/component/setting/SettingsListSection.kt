package org.yangdai.kori.presentation.component.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsListSection(
    vararg content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.6f)
    ) {
        Column {
            content.forEachIndexed { index, item ->
                item()
                if (index < content.size - 1) {
                    SettingsListSectionDivider()
                }
            }
        }
    }
}

@Composable
fun SettingsListSectionDivider() = HorizontalDivider(
    thickness = 4.dp,
    color = MaterialTheme.colorScheme.surfaceContainer
)