package org.yangdai.kori.ink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.yangdai.kori.presentation.theme.KoriTheme

class InkActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        super.onCreate(savedInstanceState)
        setContent {
            KoriTheme {
                DrawingView {
                    finish()
                }
            }
        }
    }
}