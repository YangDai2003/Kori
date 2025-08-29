package org.yangdai.kori.presentation.component.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PermDeviceInformation
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.app_info
import kori.composeapp.generated.resources.backup
import kori.composeapp.generated.resources.card
import kori.composeapp.generated.resources.card_size
import kori.composeapp.generated.resources.color_platte
import kori.composeapp.generated.resources.cowriter
import kori.composeapp.generated.resources.dark_mode
import kori.composeapp.generated.resources.data
import kori.composeapp.generated.resources.date_format
import kori.composeapp.generated.resources.default_view
import kori.composeapp.generated.resources.editor
import kori.composeapp.generated.resources.guide
import kori.composeapp.generated.resources.key
import kori.composeapp.generated.resources.language
import kori.composeapp.generated.resources.language_description
import kori.composeapp.generated.resources.line_numbers
import kori.composeapp.generated.resources.model
import kori.composeapp.generated.resources.password
import kori.composeapp.generated.resources.privacy_policy
import kori.composeapp.generated.resources.restore_from_backup
import kori.composeapp.generated.resources.security
import kori.composeapp.generated.resources.style
import kori.composeapp.generated.resources.templates
import kori.composeapp.generated.resources.text_overflow
import kori.composeapp.generated.resources.time_format
import org.jetbrains.compose.resources.stringResource
import org.yangdai.kori.presentation.util.clickToLanguageSetting
import org.yangdai.kori.presentation.util.shouldShowLanguageSetting

@Composable
fun SettingsListPane(
    selectedItem: Int? = null,
    navigateToDetail: (Int) -> Unit
) = Column(
    Modifier
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(top = 52.dp)
        .verticalScroll(rememberScrollState())
) {
    ListPaneSection(
        items = listOf(
            {
                ListPaneItem(
                    title = stringResource(Res.string.style),
                    description = stringResource(Res.string.dark_mode)
                            + "  •  " + stringResource(Res.string.color_platte),
                    icon = Icons.Outlined.Palette,
                    isSelected = selectedItem == 0,
                    onClick = { navigateToDetail(0) }
                )
            },
            {
                ListPaneItem(
                    title = stringResource(Res.string.editor),
                    description = stringResource(Res.string.default_view)
                            + "  •  " + stringResource(Res.string.line_numbers),
                    icon = Icons.Outlined.Edit,
                    isSelected = selectedItem == 1,
                    onClick = { navigateToDetail(1) }
                )
            },
            {
                ListPaneItem(
                    title = stringResource(Res.string.card),
                    description = stringResource(Res.string.text_overflow)
                            + "  •  " + stringResource(Res.string.card_size),
                    icon = Icons.Outlined.GridView,
                    isSelected = selectedItem == 2,
                    onClick = { navigateToDetail(2) }
                )
            }
        )
    )
    if (shouldShowLanguageSetting())
        ListPaneSection(
            items = listOf {
                ListPaneItem(
                    modifier = Modifier.clickToLanguageSetting(),
                    title = stringResource(Res.string.language),
                    description = stringResource(Res.string.language_description),
                    icon = Icons.Outlined.Language,
                    isSelected = false
                )
            }
        )
    ListPaneSection(
        items = listOf(
            {
                ListPaneItem(
                    title = stringResource(Res.string.templates),
                    description = stringResource(Res.string.time_format)
                            + "  •  " + stringResource(Res.string.date_format),
                    icon = Icons.AutoMirrored.Outlined.TextSnippet,
                    isSelected = selectedItem == 3,
                    onClick = { navigateToDetail(3) }
                )
            },
            {
                ListPaneItem(
                    title = stringResource(Res.string.data),
                    description = stringResource(Res.string.backup)
                            + "  •  " + stringResource(Res.string.restore_from_backup),
                    icon = Icons.Outlined.SdStorage,
                    isSelected = selectedItem == 4,
                    onClick = { navigateToDetail(4) }
                )
            },
            {
                ListPaneItem(
                    title = stringResource(Res.string.security),
                    description = stringResource(Res.string.password),
                    icon = Icons.Outlined.Security,
                    isSelected = selectedItem == 5,
                    onClick = { navigateToDetail(5) }
                )
            },
            {
                ListPaneItem(
                    title = stringResource(Res.string.cowriter),
                    description = stringResource(Res.string.model)
                            + "  •  " + "API ${stringResource(Res.string.key)}",
                    icon = Icons.Outlined.GeneratingTokens,
                    isSelected = selectedItem == 6,
                    onClick = { navigateToDetail(6) }
                )
            }
        )
    )
    ListPaneSection(
        items = listOf {
            ListPaneItem(
                title = stringResource(Res.string.app_info),
                description = stringResource(Res.string.guide)
                        + " • " + stringResource(Res.string.privacy_policy),
                icon = Icons.Outlined.PermDeviceInformation,
                isSelected = selectedItem == 7,
                onClick = { navigateToDetail(7) }
            )
        }
    )
}
