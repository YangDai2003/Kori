package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import org.yangdai.kori.OS
import org.yangdai.kori.currentPlatformInfo
import org.yangdai.kori.isDesktop
import org.yangdai.kori.presentation.component.ConfettiEffect
import org.yangdai.kori.presentation.component.login.LogoText
import org.yangdai.kori.presentation.util.clickToShareText

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        val haptic = LocalHapticFeedback.current
        var clickedCount by rememberSaveable { mutableStateOf(0) }

        Button(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(240.dp),
            shapes = ButtonDefaults.shapes(
                shape = MaterialShapes.Cookie12Sided.toShape(),
                pressedShape = CircleShape
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                clickedCount++
                if (clickedCount >= 5) {
                    showConfetti = true
                    clickedCount = 0
                }
            }
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LogoText()
                Text(
                    modifier = Modifier.padding(bottom = 36.dp).align(Alignment.BottomCenter),
                    text = (if (!currentPlatformInfo.isDesktop()) currentPlatformInfo.operatingSystem.name + " " else "")
                            + currentPlatformInfo.version
                            + "\n" + currentPlatformInfo.deviceModel,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
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
                    imageVector = if (currentPlatformInfo.operatingSystem == OS.ANDROID) Icons.Outlined.Share
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