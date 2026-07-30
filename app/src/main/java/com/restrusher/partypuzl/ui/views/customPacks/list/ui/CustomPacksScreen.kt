package com.restrusher.partypuzl.ui.views.customPacks.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPackUiModel
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPacksState
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPacksViewModel
import com.restrusher.partypuzl.ui.views.customPacks.previewPacksState
import com.restrusher.partypuzl.ui.views.customPacks.ui.CustomPackCta
import com.restrusher.partypuzl.ui.views.customPacks.ui.DashedEmptyState

/**
 * The custom-pack manager — every pack the user has written, with the switch that decides whether
 * it is offered when setting up a game, and the way in to editing or deleting it. Reached from
 * Settings.
 */
@Composable
fun CustomPacksScreen(
    setAppBarTitle: (String) -> Unit,
    onCreatePack: () -> Unit,
    onOpenPack: (String) -> Unit,
    onEditPack: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomPacksViewModel = hiltViewModel()
) {
    val title = stringResource(R.string.custom_packs_title)
    LaunchedEffect(Unit) { setAppBarTitle(title) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CustomPacksContent(
        state = uiState,
        onCreatePack = onCreatePack,
        onOpenPack = onOpenPack,
        onEditPack = onEditPack,
        onToggle = viewModel::onToggle,
        onDeleteRequested = viewModel::onDeleteRequested,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onDeleteDismissed = viewModel::onDeleteDismissed,
        modifier = modifier
    )
}

@Composable
private fun CustomPacksContent(
    state: CustomPacksState,
    onCreatePack: () -> Unit,
    onOpenPack: (String) -> Unit,
    onEditPack: (String) -> Unit,
    onToggle: (String) -> Unit,
    onDeleteRequested: (CustomPackUiModel) -> Unit,
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
            item { PacksHeader(packCount = state.packs.size, entryCount = state.totalEntries) }

            if (state.packs.isEmpty() && !state.isLoading) {
                item {
                    DashedEmptyState(
                        title = stringResource(R.string.custom_packs_empty_title),
                        subtitle = stringResource(R.string.custom_packs_empty_subtitle)
                    )
                }
            }

            items(state.packs, key = { it.id }) { pack ->
                CustomPackCard(
                    pack = pack,
                    onOpen = { onOpenPack(pack.id) },
                    onToggle = { onToggle(pack.id) },
                    onEdit = { onEditPack(pack.id) },
                    onDelete = { onDeleteRequested(pack) }
                )
            }
        }

        CustomPackCta(
            label = stringResource(R.string.custom_packs_create_cta),
            iconRes = R.drawable.ic_plus,
            onClick = onCreatePack,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }

    state.deleteTarget?.let { target ->
        DeletePackDialog(
            pack = target,
            onConfirm = onDeleteConfirmed,
            onDismiss = onDeleteDismissed
        )
    }
}

@Composable
private fun PacksHeader(packCount: Int, entryCount: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp)) {
        Text(
            text = stringResource(R.string.custom_packs_summary, packCount, entryCount).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.custom_packs_headline),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.custom_packs_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
        )
    }
}

@Composable
private fun DeletePackDialog(
    pack: CustomPackUiModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_pack_delete_title)) },
        text = { Text(stringResource(R.string.custom_pack_delete_message, pack.name)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.custom_pack_delete_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.custom_pack_cancel),
                    color = MaterialTheme.appColors.brandAccent
                )
            }
        }
    )
}

@Composable
private fun CustomPacksSample(state: CustomPacksState) {
    Box(modifier = Modifier.fillMaxSize().appBackground()) {
        CustomPacksContent(
            state = state,
            onCreatePack = {},
            onOpenPack = {},
            onEditPack = {},
            onToggle = {},
            onDeleteRequested = {},
            onDeleteConfirmed = {},
            onDeleteDismissed = {}
        )
    }
}

@Preview(name = "CustomPacks – Light", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun CustomPacksLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { CustomPacksSample(previewPacksState) }
}

@Preview(name = "CustomPacks – Dark", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun CustomPacksDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { CustomPacksSample(previewPacksState) }
}

@Preview(name = "CustomPacks empty – Light", showBackground = true, widthDp = 360, heightDp = 480)
@Composable
private fun CustomPacksEmptyLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        CustomPacksSample(CustomPacksState(isLoading = false))
    }
}

@Preview(name = "CustomPacks empty – Dark", showBackground = true, widthDp = 360, heightDp = 480)
@Composable
private fun CustomPacksEmptyDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        CustomPacksSample(CustomPacksState(isLoading = false))
    }
}
