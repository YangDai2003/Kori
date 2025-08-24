package org.yangdai.kori.presentation.component.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.yangdai.kori.presentation.screen.settings.DataActionState

@Preview
@Composable
fun ProgressDialogPreview() {
    ProgressDialog(
        dataActionState = DataActionState(
            progress = 0.5f,
            infinite = false,
            message = "Loading..."
        ),
        onDismissRequest = {}
    )
}

@Preview
@Composable
fun ProgressDialogInfinitePreview() {
    ProgressDialog(
        dataActionState = DataActionState(
            progress = 0.5f,
            infinite = true
        ),
        onDismissRequest = {}
    )
}

@Preview
@Composable
fun ProgressDialogDonePreview() {
    ProgressDialog(
        dataActionState = DataActionState(
            progress = 1f,
            infinite = false,
            message = "Done!"
        ),
        onDismissRequest = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressDialog(
    dataActionState: DataActionState,
    onDismissRequest: () -> Unit
) {

    val progress = dataActionState.progress
    val infinite = dataActionState.infinite
    val message = dataActionState.message

    if (progress == Float.MIN_VALUE) return

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .background(AlertDialogDefaults.containerColor, AlertDialogDefaults.shape)
                    .clip(AlertDialogDefaults.shape),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .size(72.dp)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {

                    if (infinite && progress < 1f)
                        ContainedLoadingIndicator(Modifier.fillMaxSize())
                    else
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 6.dp,
                            progress = { progress }
                        )

                    AnimatedContent(targetState = progress) {
                        if (it == 1f)
                            Icon(
                                modifier = Modifier.size(56.dp),
                                imageVector = Icons.Rounded.Done,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        else
                            if (!infinite)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                    }
                }
                if (message.isNotEmpty()) Text(message)
                AnimatedVisibility(visible = progress < 1f) {
                    HorizontalDivider()
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        onClick = onDismissRequest
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}
