package org.yangdai.kori

import androidx.compose.material3.Surface
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import org.yangdai.kori.data.di.KoinInitializer
import org.yangdai.kori.presentation.theme.KoriTheme

@OptIn(ExperimentalComposeApi::class, ExperimentalComposeUiApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        KoinInitializer.init()
        enableBackGesture = true
        parallelRendering = true
    }
) {
    KoriTheme {
        Surface {
            App()
        }
    }
}