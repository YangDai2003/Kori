package org.yangdai.kori.presentation.component.main

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.setWidgetPreviews
import kotlinx.coroutines.launch
import org.yangdai.kori.R
import org.yangdai.kori.InkActivity
import org.yangdai.kori.presentation.glance.MyAppWidgetReceiver

@Composable
actual fun WidgetListItem() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = remember { GlanceAppWidgetManager(context.applicationContext) }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            manager.setWidgetPreviews<MyAppWidgetReceiver>()
        }
    }
    ListItem(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(CardDefaults.shape)
            .clickable {
                scope.launch {
                    manager.requestPinGlanceAppWidget(MyAppWidgetReceiver::class.java)
                }
            },
        headlineContent = { Text(stringResource(R.string.home_widget)) },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                contentDescription = null
            )
        }
    )
}

@Composable
actual fun InkListItem() {
    val context = LocalContext.current
    ListItem(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(CardDefaults.shape)
            .clickable {
                context.startActivity(Intent(context, InkActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            },
        headlineContent = { Text("Ink Playground") },
        supportingContent = { Text("This is an experimental feature and may be changed, presented in a different form, or even removed entirely.") },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowRight,
                contentDescription = null
            )
        }
    )
}