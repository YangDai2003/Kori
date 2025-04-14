package org.yangdai.kori.presentation.component

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun LazyGridScrollbar(
    modifier: Modifier,
    state: LazyGridState
) {
    VerticalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(state),
        style = LocalScrollbarStyle.current
            .copy(
                unhoverColor = MaterialTheme.colorScheme.outlineVariant,
                hoverColor = MaterialTheme.colorScheme.outline
            )
    )
}