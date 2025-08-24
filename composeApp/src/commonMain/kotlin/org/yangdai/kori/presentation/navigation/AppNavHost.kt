package org.yangdai.kori.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.yangdai.kori.presentation.screen.main.MainViewModel

@Composable
expect fun AppNavHost(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    navHostController: NavHostController = rememberNavController()
)
