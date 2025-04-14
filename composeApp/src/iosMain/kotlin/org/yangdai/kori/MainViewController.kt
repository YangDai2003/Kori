package org.yangdai.kori

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.window.ComposeUIViewController
import org.yangdai.kori.data.di.KoinInitializer

fun MainViewController() = ComposeUIViewController(
    configure = {
        KoinInitializer.init()
    }
) {
    MaterialTheme {
        Surface {
            App()
        }
    }
}