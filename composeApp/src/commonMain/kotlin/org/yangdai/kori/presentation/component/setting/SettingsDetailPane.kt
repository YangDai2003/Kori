package org.yangdai.kori.presentation.component.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.yangdai.kori.presentation.component.setting.detail.AboutPane
import org.yangdai.kori.presentation.component.setting.detail.AiPane
import org.yangdai.kori.presentation.component.setting.detail.CardPane
import org.yangdai.kori.presentation.component.setting.detail.DataPane
import org.yangdai.kori.presentation.component.setting.detail.EditorPane
import org.yangdai.kori.presentation.component.setting.detail.SecurityPane
import org.yangdai.kori.presentation.component.setting.detail.StylePane
import org.yangdai.kori.presentation.component.setting.detail.TemplatePane
import org.yangdai.kori.presentation.screen.main.MainViewModel

@Composable
fun SettingsDetailPane(
    selectedItem: Int?,
    isExpanded: Boolean,
    viewModel: MainViewModel
) = Box(
    modifier = Modifier.fillMaxSize()
        .padding(top = 52.dp, end = if (isExpanded) 24.dp else 0.dp),
    contentAlignment = Alignment.TopCenter
) {
    Box(Modifier.widthIn(max = 600.dp).fillMaxSize()) {
        when (selectedItem) {
            0 -> StylePane(viewModel)
            1 -> EditorPane(viewModel)
            2 -> CardPane(viewModel)
            3 -> TemplatePane(viewModel)
            4 -> DataPane(viewModel)
            5 -> SecurityPane(viewModel)
            6 -> AiPane(viewModel)
            7 -> AboutPane()
            else -> {}
        }
    }
}
