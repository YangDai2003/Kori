package org.yangdai.kori.presentation.component.main

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExpandedDockedSearchBar
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.TopSearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveSearchBar(
    isLargeScreen: Boolean,
    searchBarState: SearchBarState,
    inputField: @Composable () -> Unit,
    expandedContent: @Composable ColumnScope.() -> Unit
) {
    TopSearchBar(
        modifier = Modifier.padding(horizontal = 16.dp),
        state = searchBarState,
        inputField = inputField,
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        windowInsets = SearchBarDefaults.windowInsets.only(WindowInsetsSides.Top)
    )
    val shadowElevation by animateDpAsState(targetValue = if (searchBarState.currentValue == SearchBarValue.Expanded) 4.dp else 0.dp)
    if (isLargeScreen)
        ExpandedDockedSearchBar(
            state = searchBarState,
            inputField = inputField,
            colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
            shadowElevation = shadowElevation,
            content = expandedContent
        )
    else
        ExpandedFullScreenSearchBar(
            state = searchBarState,
            inputField = inputField,
            colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceBright),
            shadowElevation = shadowElevation,
            content = expandedContent
        )
}