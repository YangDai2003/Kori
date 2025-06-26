package org.yangdai.kori.presentation.component.main

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable function that adapts the navigation drawer based on the screen size.
 *
 * @param isLargeScreen A boolean indicating whether the screen is large or not.
 * @param drawerState The state of the drawer.
 * @param gesturesEnabled A boolean indicating whether gestures are enabled or not.
 * @param drawerContent The content of the drawer.
 * @param content The main content.
 */
@Composable
fun AdaptiveNavigationDrawerLayout(
    isLargeScreen: Boolean,
    drawerState: DrawerState,
    gesturesEnabled: Boolean,
    drawerContent: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit
) = if (isLargeScreen) {
    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                drawerContent()
            }
        },
        content = content
    )
} else {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                drawerState = drawerState,
                modifier = Modifier.width(320.dp)
            ) {
                drawerContent()
            }
        },
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        content = content
    )
}