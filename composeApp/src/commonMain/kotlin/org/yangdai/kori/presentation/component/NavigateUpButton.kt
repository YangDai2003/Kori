package org.yangdai.kori.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.back
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.Platform
import org.yangdai.kori.currentPlatform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigateUpButton(onClick: () -> Unit) {
    val platform = remember { currentPlatform() }
    if (platform is Platform.JVM) return
    TooltipIconButton(
        tipText = stringResource(Res.string.back),
        icon = Icons.AutoMirrored.Filled.NavigateBefore,
        onClick = onClick
    )
}