package org.yangdai.kori.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
expect fun AppNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController()
)
