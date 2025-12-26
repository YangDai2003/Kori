package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EditorScaffold(
    modifier: Modifier,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) = Scaffold(
    modifier = modifier.imePadding(),
    topBar = topBar,
    bottomBar = bottomBar,
    contentWindowInsets = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
        .only(WindowInsetsSides.Horizontal)
) { innerPadding ->
    Column(Modifier.fillMaxSize().padding(innerPadding)) {
        content()
    }
}