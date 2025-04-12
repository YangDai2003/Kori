package org.yangdai.kori

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.yangdai.kori.presentation.navigation.AppNavHost
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
@Preview
fun App(navHostController: NavHostController = rememberNavController()) {
    AppNavHost(navHostController = navHostController)
}
