package org.yangdai.kori.presentation.component.setting.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kfile.JsonExporter
import kfile.JsonPicker
import kfile.PlatformFilesPicker
import kori.composeapp.generated.resources.Res
import kori.composeapp.generated.resources.backup
import kori.composeapp.generated.resources.backup_description
import kori.composeapp.generated.resources.import_files
import kori.composeapp.generated.resources.import_files_description
import kori.composeapp.generated.resources.reset_database
import kori.composeapp.generated.resources.reset_database_description
import kori.composeapp.generated.resources.reset_database_warning
import kori.composeapp.generated.resources.restore_description
import kori.composeapp.generated.resources.restore_from_backup
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.yangdai.kori.presentation.component.dialog.FoldersDialog
import org.yangdai.kori.presentation.component.dialog.ProgressDialog
import org.yangdai.kori.presentation.component.dialog.WarningDialog
import org.yangdai.kori.presentation.component.setting.DetailPaneItem
import org.yangdai.kori.presentation.screen.settings.DataViewModel

@Composable
fun DataPane(viewModel: DataViewModel = koinViewModel()) {

    val foldersWithNoteCounts by viewModel.foldersWithNoteCounts.collectAsStateWithLifecycle()
    val dataActionState by viewModel.dataActionState.collectAsStateWithLifecycle()

    var showWarningDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var selectFolderId by remember { mutableStateOf<String?>(null) }
    var showSaveJsonDialog by remember { mutableStateOf(false) }
    var showPickJsonDialog by remember { mutableStateOf(false) }
    var json by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(16.dp))

        DetailPaneItem(
            modifier = Modifier.padding(bottom = 8.dp),
            title = stringResource(Res.string.import_files),
            description = stringResource(Res.string.import_files_description),
            icon = Icons.Outlined.FileDownload,
            onClick = { showFolderDialog = true }
        )

        DetailPaneItem(
            modifier = Modifier.padding(bottom = 8.dp),
            title = stringResource(Res.string.backup),
            description = stringResource(Res.string.backup_description),
            icon = Icons.Outlined.Backup,
            onClick = {
                viewModel.createBackupJson {
                    json = it
                    if (json != null)
                        showSaveJsonDialog = true
                }
            }
        )

        DetailPaneItem(
            modifier = Modifier.padding(bottom = 8.dp),
            title = stringResource(Res.string.restore_from_backup),
            description = stringResource(Res.string.restore_description),
            icon = Icons.Outlined.Restore,
            onClick = { showPickJsonDialog = true }
        )

        DetailPaneItem(
            title = stringResource(Res.string.reset_database),
            description = stringResource(Res.string.reset_database_description),
            icon = Icons.Outlined.CleaningServices,
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                    alpha = 0.6f
                )
            ),
            onClick = { showWarningDialog = true }
        )

        Spacer(Modifier.height(8.dp))
    }

    if (showFolderDialog)
        FoldersDialog(
            oFolderId = null,
            foldersWithNoteCounts = foldersWithNoteCounts,
            onDismissRequest = { showFolderDialog = false },
            onSelect = { folderId ->
                selectFolderId = folderId
                showFolderDialog = false
                showImportDialog = true
            }
        )

    if (showImportDialog)
        PlatformFilesPicker {
            showImportDialog = false
            if (it.isNotEmpty())
                viewModel.importFiles(it, selectFolderId)
        }

    if (showSaveJsonDialog)
        JsonExporter(json!!) {
            showSaveJsonDialog = false
        }

    if (showPickJsonDialog)
        JsonPicker { json ->
            showPickJsonDialog = false
            if (json != null) {
                viewModel.restoreFromJson(json)
            }
        }

    if (showWarningDialog)
        WarningDialog(
            message = stringResource(Res.string.reset_database_warning),
            onDismissRequest = { showWarningDialog = false },
            onConfirm = { viewModel.resetDatabase() }
        )

    ProgressDialog(dataActionState) {
        viewModel.cancelDataAction()
    }
}