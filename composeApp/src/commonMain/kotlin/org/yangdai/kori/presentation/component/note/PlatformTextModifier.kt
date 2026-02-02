package org.yangdai.kori.presentation.component.note

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun Modifier.dragAndDropText(onDrop: (String) -> Unit): Modifier