package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_name
import kori.composeapp.generated.resources.compose_multiplatform
import kori.composeapp.generated.resources.report_a_bug_or_request_a_feature
import kori.composeapp.generated.resources.share_this_app
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.Platform
import org.yangdai.kori.currentPlatform
import org.yangdai.kori.presentation.component.ConfettiEffect
import org.yangdai.kori.presentation.component.CurlyCornerShape
import org.yangdai.kori.presentation.util.clickToShareText

@Composable
fun AboutPane() {
    val uriHandler = LocalUriHandler.current
    var showConfetti by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var pressAMP by remember { mutableFloatStateOf(12f) }
        val animatedPress by animateFloatAsState(pressAMP)
        val haptic = LocalHapticFeedback.current

        Box(
            modifier = Modifier
                .size(240.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CurlyCornerShape(curlAmplitude = animatedPress.toDouble()),
                )
                .shadow(
                    elevation = 10.dp,
                    shape = CurlyCornerShape(curlAmplitude = animatedPress.toDouble()),
                    ambientColor = MaterialTheme.colorScheme.primaryContainer,
                    spotColor = MaterialTheme.colorScheme.primaryContainer,
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            pressAMP = 0f
                            tryAwaitRelease()
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            pressAMP = 12f
                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showConfetti = true
                        }
                    )
                }
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .requiredSize(200.dp)
                    .padding(top = 16.dp),
                painter = painterResource(Res.drawable.compose_multiplatform),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.app_name) + " for " + currentPlatform().name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSynthesis = FontSynthesis.Weight
                    ),
                    textAlign = TextAlign.Center
                )
//
//                Text(
//                    text = stringResource(Res.string.version) + " ",
//                    style = MaterialTheme.typography.bodySmall,
//                    textAlign = TextAlign.Center
//                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {
                    uriHandler.openUri("https://github.com/YangDai2003/Kori")
                },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            leadingContent = {
                Icon(imageVector = Icons.Outlined.BugReport, contentDescription = null)
            },
            headlineContent = {
                Text(text = stringResource(Res.string.report_a_bug_or_request_a_feature))
            })

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickToShareText(stringResource(Res.string.share_this_app)),
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            leadingContent = {
                Icon(
                    imageVector = if (currentPlatform() == Platform.Android) Icons.Outlined.Share
                    else Icons.Outlined.IosShare,
                    contentDescription = null
                )
            },
            headlineContent = {
                Text(text = stringResource(Res.string.share_this_app))
            })
    }

    if (showConfetti) {
        ConfettiEffect()
    }
}