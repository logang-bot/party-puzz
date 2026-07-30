package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.ReportPageTint
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryState
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryViewModel
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.ui.CustomPackCta
import com.restrusher.partypuzl.ui.views.customPacks.ui.NumberedStep

/**
 * One entry at a time. Step 01 picks the kind of game deal; the steps after it change to match,
 * and the card at the bottom shows what the room will end up reading.
 */
@Composable
fun CreateCustomEntryScreen(
    packId: String,
    entryId: String?,
    setAppBarTitle: (String) -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateCustomEntryViewModel = hiltViewModel()
) {
    val title = stringResource(
        if (entryId != null) R.string.custom_entry_edit_title else R.string.custom_entry_new_title
    )
    LaunchedEffect(title) { setAppBarTitle(title) }
    LaunchedEffect(packId, entryId) { viewModel.load(packId, entryId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // The page follows the entry type picked in step 01, so switching type re-tints the screen.
    ReportPageTint(uiState.type.accent)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 96.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            AddingToCard(packName = uiState.packName)

            NumberedStep(number = "01", label = stringResource(R.string.custom_entry_step_type))
            Spacer(modifier = Modifier.height(10.dp))
            EntryTypeCards(selected = uiState.type, onSelect = viewModel::onTypeChange)

            EntryForm(state = uiState, viewModel = viewModel)

            NumberedStep(
                number = if (uiState.type == CustomEntryType.TRIVIA ||
                    uiState.type == CustomEntryType.STICKY_DARE
                ) "04" else "03",
                label = stringResource(R.string.custom_entry_step_preview)
            )
            Spacer(modifier = Modifier.height(10.dp))
            EntryPreviewCard(state = uiState)
        }

        CustomPackCta(
            label = stringResource(R.string.custom_entry_save),
            iconRes = R.drawable.ic_check,
            onClick = { viewModel.onSave(onSaved) },
            enabled = uiState.canSave,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun EntryForm(state: CreateCustomEntryState, viewModel: CreateCustomEntryViewModel) {
    when (state.type) {
        CustomEntryType.TRUTH, CustomEntryType.DARE -> TruthOrDareForm(
            state = state,
            onTextChange = viewModel::onTextChange
        )

        CustomEntryType.STICKY_DARE -> StickyDareForm(
            state = state,
            onTextChange = viewModel::onTextChange,
            onDurationChange = viewModel::onDurationChange
        )

        CustomEntryType.TRIVIA -> TriviaForm(
            state = state,
            onTextChange = viewModel::onTextChange,
            onOptionAChange = viewModel::onOptionAChange,
            onOptionBChange = viewModel::onOptionBChange,
            onCorrectChange = viewModel::onCorrectOptionChange
        )
    }
}

/** Reminds the author which pack this is going into — the flow can be several screens deep. */
@Composable
private fun AddingToCard(packName: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.appColors.panelFill)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.wash(Wash.Fill),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.custom_entry_adding_to).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Tertiary)
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = packName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
