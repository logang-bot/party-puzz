package com.restrusher.partypuzl.ui.views.customPacks.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.ReportPageTint
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.customPacks.editor.CustomPackEditorViewModel
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPackUiModel
import com.restrusher.partypuzl.ui.views.customPacks.list.PackWarning
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.model.iconRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.ui.AccentIconTile
import com.restrusher.partypuzl.ui.views.customPacks.ui.CustomPackCta
import com.restrusher.partypuzl.ui.views.customPacks.ui.DashedEmptyState
import com.restrusher.partypuzl.ui.views.customPacks.ui.MetaChip
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

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            uiState.pack?.let { pack ->
                item { PackHeader(pack = pack) }
                item { EntriesLabel(count = uiState.entries.size, onAdd = onAddEntry) }
            }

            if (uiState.entries.isEmpty()) {
                item {
                    DashedEmptyState(
                        title = stringResource(R.string.custom_pack_entries_empty_title),
                        subtitle = stringResource(R.string.custom_pack_entries_empty_subtitle)
                    )
                }
            }

            itemsIndexed(uiState.entries, key = { _, entry -> entry.id }) { index, entry ->
                CustomEntryRow(
                    entry = entry,
                    position = index + 1,
                    durationLabel = entry.durationSeconds
                        ?.takeIf { entry.type == CustomEntryType.STICKY_DARE }
                        ?.let { stickyDurationLabel(it) },
                    onEdit = { onEditEntry(entry.id) },
                    onDelete = { viewModel.onDeleteRequested(entry) }
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

    if (uiState.deleteTarget != null) {
        AlertDialog(
            onDismissRequest = viewModel::onDeleteDismissed,
            title = { Text(stringResource(R.string.custom_entry_delete_title)) },
            text = { Text(stringResource(R.string.custom_entry_delete_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::onDeleteConfirmed) {
                    Text(
                        text = stringResource(R.string.custom_pack_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteDismissed) {
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
private fun PackHeader(pack: CustomPackUiModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            AccentIconTile(accent = pack.spice.accent, iconRes = pack.spice.iconRes, size = 56)
            Spacer(modifier = Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetaChip(
                        label = stringResource(pack.category.labelRes),
                        tone = pack.spice.accent
                    )
                    MetaChip(label = stringResource(pack.spice.labelRes))
                }
            }
        }
        if (pack.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = pack.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
            )
        }
        pack.warning?.let { warning ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(warning.messageRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
            )
        }
    }
}

@Composable
private fun EntriesLabel(count: Int, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.custom_pack_entries_count, count).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onAdd) {
            Text(
                text = stringResource(R.string.custom_pack_add).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.appColors.brandAccent
            )
        }
    }
}

private val PackWarning.messageRes: Int
    get() = when (this) {
        PackWarning.EMPTY -> R.string.custom_pack_warning_empty
        PackWarning.TRUTHS_ONLY -> R.string.custom_pack_warning_truths_only
        PackWarning.DARES_ONLY -> R.string.custom_pack_warning_dares_only
    }
