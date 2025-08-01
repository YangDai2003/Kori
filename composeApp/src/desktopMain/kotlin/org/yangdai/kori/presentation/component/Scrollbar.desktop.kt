package org.yangdai.kori.presentation.component

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun VerticalScrollbar(modifier: Modifier, state: LazyGridState) =
    VerticalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(state),
        style = LocalScrollbarStyle.current
            .copy(
                unhoverColor = MaterialTheme.colorScheme.outlineVariant,
                hoverColor = MaterialTheme.colorScheme.outline
            )
    )

@Composable
actual fun HorizontalScrollbar(modifier: Modifier, state: LazyListState) =
    HorizontalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(state),
        style = LocalScrollbarStyle.current
            .copy(
                unhoverColor = MaterialTheme.colorScheme.outlineVariant,
                hoverColor = MaterialTheme.colorScheme.outline
            )
    )

@Composable
actual fun VerticalScrollbar(modifier: Modifier, state: ScrollState) =
    VerticalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(state),
        style = LocalScrollbarStyle.current
            .copy(
                unhoverColor = MaterialTheme.colorScheme.outlineVariant,
                hoverColor = MaterialTheme.colorScheme.outline
            )
    )