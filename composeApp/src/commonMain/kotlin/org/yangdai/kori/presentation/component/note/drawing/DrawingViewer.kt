package org.yangdai.kori.presentation.component.note.drawing

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun DrawingViewer(uuid: String, imageBitmap: ImageBitmap?, modifier: Modifier = Modifier)