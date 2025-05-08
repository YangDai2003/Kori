package org.yangdai.kori.presentation.component.note

import androidx.compose.foundation.ContextMenuDataProvider
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.foundation.text.TextContextMenu
import androidx.compose.runtime.Composable

//@OptIn(ExperimentalFoundationApi::class)
//val CustomTextMenu = object : TextContextMenu {
//    @Composable
//    override fun Area(
//        textManager: TextContextMenu.TextManager,
//        state: ContextMenuState,
//        content: @Composable (() -> Unit)
//    ) {
//        val textMenu = LocalTextContextMenu.current
//        // Reuses original TextContextMenu and adds a new item
//        ContextMenuDataProvider({
//            val shortText = textManager.selectedText
//            if (shortText.isNotEmpty()) {
//                listOf(
//                    ContextMenuItem("Search $shortText") {
//
//                    }
//                )
//            } else {
//                emptyList()
//            }
//        }) {
//            textMenu.Area(textManager, state, content = content)
//        }
//    }
//}
