package org.yangdai.kori.presentation.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.yangdai.kori.presentation.component.dialog.ProgressDialog
import org.yangdai.kori.presentation.screen.main.MainViewModel

@Composable
expect fun AppNavHostCore(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    navHostController: NavHostController
)

@Composable
fun AppNavHost(
    mainViewModel: MainViewModel,
    showPassScreen: Boolean,
    navHostController: NavHostController = rememberNavController()
) {
    val blur by animateDpAsState(targetValue = if (showPassScreen) 16.dp else 0.dp)
    val semanticsModifier =
        if (showPassScreen) Modifier.semantics(mergeDescendants = true) { hideFromAccessibility() }
        else Modifier
    Surface {
        AppNavHostCore(
            modifier = Modifier.fillMaxSize().blur(blur).then(semanticsModifier),
            mainViewModel = mainViewModel,
            navHostController = navHostController
        )
    }
    val dataActionState by mainViewModel.dataActionState.collectAsStateWithLifecycle()
    ProgressDialog(dataActionState) { mainViewModel.cancelDataAction() }
}