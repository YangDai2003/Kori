package org.yangdai.kori.presentation.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VerticalScrollbar(
    modifier: Modifier,
    state: LazyGridState,
)

@Composable
expect fun HorizontalScrollbar(
    modifier: Modifier,
    state: LazyListState,
)

@Composable
expect fun VerticalScrollbar(
    modifier: Modifier,
    state: ScrollState,
)