package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.guide
import kori.composeapp.generated.resources.privacy_policy
import kori.composeapp.generated.resources.report_a_bug_or_request_a_feature
import kori.composeapp.generated.resources.shareContent
import kori.composeapp.generated.resources.share_this_app
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.Platform
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.presentation.component.ConfettiEffect
import org.yangdai.kori.presentation.component.CurlyCornerShape
import org.yangdai.kori.presentation.component.login.LogoText
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
                .padding(top = 8.dp)
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
                },
            contentAlignment = Alignment.Center
        ) {
            LogoText()
            Text(
                modifier = Modifier
                    .padding(bottom = 36.dp)
                    .align(Alignment.BottomCenter),
                text = if (currentPlatformInfo.platform != Platform.Desktop) currentPlatformInfo.operatingSystem.name + " " else ""
                        + currentPlatformInfo.version
                        + "\n" + currentPlatformInfo.deviceModel,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {
                    uriHandler.openUri("https://github.com/YangDai2003/Kori/issues")
                },
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
                .clickable {
                    uriHandler.openUri("https://github.com/YangDai2003/Kori/blob/master/Guide.md")
                },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.TipsAndUpdates, contentDescription = null)
            },
            headlineContent = {
                Text(text = stringResource(Res.string.guide))
            })

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickable {
                    uriHandler.openUri("https://github.com/YangDai2003/Kori/blob/master/PRIVACY_POLICY.md")
                },
            leadingContent = {
                Icon(imageVector = Icons.Outlined.PrivacyTip, contentDescription = null)
            },
            headlineContent = {
                Text(text = stringResource(Res.string.privacy_policy))
            })

        ListItem(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clip(CircleShape)
                .clickToShareText(stringResource(Res.string.shareContent)),
            leadingContent = {
                Icon(
                    imageVector = if (currentPlatformInfo.platform == Platform.Android) Icons.Outlined.Share
                    else Icons.Outlined.IosShare,
                    contentDescription = null
                )
            },
            headlineContent = {
                Text(text = stringResource(Res.string.share_this_app))
            })

        Spacer(modifier = Modifier.navigationBarsPadding())
    }

    if (showConfetti) {
        ConfettiEffect()
    }
}