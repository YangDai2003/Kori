package org.yangdai.kori.presentation.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun LazyGridScrollbar(
    modifier: Modifier, state: LazyGridState
) {
}

@Composable
actual fun HorizontalLazyListScrollbar(
    modifier: Modifier,
    state: LazyListState
) {
}

@Composable
actual fun VerticalColumnScrollbar(
    modifier: Modifier,
    state: ScrollState
) {
}