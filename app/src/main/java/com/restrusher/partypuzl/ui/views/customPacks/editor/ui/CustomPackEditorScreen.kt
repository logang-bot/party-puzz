package com.restrusher.partypuzl.ui.views.customPacks.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.ReportPageTint
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.views.customPacks.editor.CustomPackEditorState
import com.restrusher.partypuzl.ui.views.customPacks.editor.CustomPackEditorViewModel
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.previewEditorState
import com.restrusher.partypuzl.ui.views.customPacks.previewPack
import com.restrusher.partypuzl.ui.views.customPacks.ui.CustomPackCta
import com.restrusher.partypuzl.ui.views.customPacks.ui.DashedEmptyState
import com.restrusher.partypuzl.ui.views.customPacks.ui.stickyDurationLabel

/** A pack's contents. Entries are added one at a time, each type asking only for what it needs. */
@Composable
fun CustomPackEditorScreen(
    packId: String,
    setAppBarTitle: (String) -> Unit,
    onAddEntry: () -> Unit,
    onEditEntry: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomPackEditorViewModel = hiltViewModel()
) {
    val title = stringResource(R.string.custom_pack_editor_title)
    LaunchedEffect(Unit) { setAppBarTitle(title) }
    LaunchedEffect(packId) { viewModel.load(packId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // The page washes in the pack's own spice accent, the same colour its icon tile uses.
    ReportPageTint(uiState.pack?.spice?.accent)

    CustomPackEditorContent(
        state = uiState,
        onAddEntry = onAddEntry,
        onEditEntry = onEditEntry,
        onDeleteRequested = viewModel::onDeleteRequested,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onDeleteDismissed = viewModel::onDeleteDismissed,
        modifier = modifier
    )
}

@Composable
private fun CustomPackEditorContent(
    state: CustomPackEditorState,
    onAddEntry: () -> Unit,
    onEditEntry: (String) -> Unit,
    onDeleteRequested: (CustomEntryEntity) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDeleteDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            state.pack?.let { pack ->
                item { PackHeader(pack = pack) }
                item { EntriesLabel(count = state.entries.size, onAdd = onAddEntry) }
            }

            if (state.entries.isEmpty()) {
                item {
                    DashedEmptyState(
                        title = stringResource(R.string.custom_pack_entries_empty_title),
                        subtitle = stringResource(R.string.custom_pack_entries_empty_subtitle)
                    )
                }
            }

            itemsIndexed(state.entries, key = { _, entry -> entry.id }) { index, entry ->
                CustomEntryRow(
                    entry = entry,
                    position = index + 1,
                    durationLabel = entry.durationSeconds
                        ?.takeIf { entry.type == CustomEntryType.STICKY_DARE }
                        ?.let { stickyDurationLabel(it) },
                    onEdit = { onEditEntry(entry.id) },
                    onDelete = { onDeleteRequested(entry) }
                )
            }
        }

        CustomPackCta(
            label = stringResource(R.string.custom_pack_add_entry),
            iconRes = R.drawable.ic_plus,
            onClick = onAddEntry,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }

    if (state.deleteTarget != null) {
        AlertDialog(
            onDismissRequest = onDeleteDismissed,
            title = { Text(stringResource(R.string.custom_entry_delete_title)) },
            text = { Text(stringResource(R.string.custom_entry_delete_message)) },
            confirmButton = {
                TextButton(onClick = onDeleteConfirmed) {
                    Text(
                        text = stringResource(R.string.custom_pack_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDeleteDismissed) {
                    Text(
                        text = stringResource(R.string.custom_pack_cancel),
                        color = MaterialTheme.appColors.brandAccent
                    )
                }
            }
        )
    }
}

@Composable
private fun CustomPackEditorSample(state: CustomPackEditorState) {
    Box(modifier = Modifier.fillMaxSize().appBackground()) {
        CustomPackEditorContent(
            state = state,
            onAddEntry = {},
            onEditEntry = {},
            onDeleteRequested = {},
            onDeleteConfirmed = {},
            onDeleteDismissed = {}
        )
    }
}

@Preview(name = "CustomPackEditor – Light", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CustomPackEditorLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { CustomPackEditorSample(previewEditorState) }
}

@Preview(name = "CustomPackEditor – Dark", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CustomPackEditorDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { CustomPackEditorSample(previewEditorState) }
}

@Preview(name = "CustomPackEditor empty – Light", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun CustomPackEditorEmptyLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        CustomPackEditorSample(CustomPackEditorState(pack = previewPack.copy(entryCount = 0)))
    }
}

@Preview(name = "CustomPackEditor empty – Dark", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun CustomPackEditorEmptyDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        CustomPackEditorSample(CustomPackEditorState(pack = previewPack.copy(entryCount = 0)))
    }
}
