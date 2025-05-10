package org.yangdai.kori.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun TextOptionButton(
    modifier: Modifier = Modifier,
    buttonText: String
) = Box(
    modifier = Modifier
        .padding(top = 8.dp)
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .then(modifier),
    contentAlignment = Alignment.Center
) {
    Text(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .padding(vertical = 4.dp),
        text = buttonText,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun TextOptionButton(
    buttonText: String,
    onButtonClick: () -> Unit
) = TextButton(
    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    shape = MaterialTheme.shapes.medium,
    colors = ButtonDefaults.textButtonColors().copy(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    onClick = onButtonClick
) {
    Text(
        modifier = Modifier.padding(vertical = 4.dp),
        text = buttonText,
        style = MaterialTheme.typography.titleMedium
    )
}