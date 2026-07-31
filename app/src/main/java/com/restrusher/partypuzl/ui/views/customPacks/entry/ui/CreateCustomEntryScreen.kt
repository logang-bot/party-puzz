package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.ReportPageTint
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.ctaScrim
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryState
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryViewModel
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.model.EntryDeal
import com.restrusher.partypuzl.ui.views.customPacks.previewEntryState
import com.restrusher.partypuzl.ui.views.customPacks.previewTriviaEntryState
import com.restrusher.partypuzl.ui.views.customPacks.previewTruthOrDareEntryState
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

    // The page follows the deal picked in step 01, so switching card re-tints the screen. Swapping
    // truth for dare does not: they are one deal, and one tone.
    ReportPageTint(uiState.deal.accent)

    CreateCustomEntryContent(
        state = uiState,
        onDealChange = viewModel::onDealChange,
        onKindChange = viewModel::onKindChange,
        onTextChange = viewModel::onTextChange,
        onDurationChange = viewModel::onDurationChange,
        onOptionAChange = viewModel::onOptionAChange,
        onOptionBChange = viewModel::onOptionBChange,
        onCorrectChange = viewModel::onCorrectOptionChange,
        onSave = { viewModel.onSave(onSaved) },
        modifier = modifier
    )
}

@Composable
private fun CreateCustomEntryContent(
    state: CreateCustomEntryState,
    onDealChange: (EntryDeal) -> Unit,
    onKindChange: (CustomEntryType) -> Unit,
    onTextChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onOptionAChange: (String) -> Unit,
    onOptionBChange: (String) -> Unit,
    onCorrectChange: (Char) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 96.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            AddingToCard(packName = state.packName)

            NumberedStep(number = "01", label = stringResource(R.string.custom_entry_step_type))
            Spacer(modifier = Modifier.height(10.dp))
            EntryTypeCards(selected = state.deal, onSelect = onDealChange)

            EntryForm(
                state = state,
                onKindChange = onKindChange,
                onTextChange = onTextChange,
                onDurationChange = onDurationChange,
                onOptionAChange = onOptionAChange,
                onOptionBChange = onOptionBChange,
                onCorrectChange = onCorrectChange
            )

            // Every deal asks for two things between the picker and here, so the preview is
            // always step 04.
            NumberedStep(
                number = "04",
                label = stringResource(R.string.custom_entry_step_preview)
            )
            Spacer(modifier = Modifier.height(10.dp))
            EntryPreviewCard(state = state)
        }

        CustomPackCta(
            label = stringResource(R.string.custom_entry_save),
            iconRes = R.drawable.ic_check,
            onClick = onSave,
            enabled = state.canSave,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .ctaScrim()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 12.dp)
        )
    }
}

/** Steps 02 and 03 — each deal asks only for what it actually needs. */
@Composable
private fun EntryForm(
    state: CreateCustomEntryState,
    onKindChange: (CustomEntryType) -> Unit,
    onTextChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onOptionAChange: (String) -> Unit,
    onOptionBChange: (String) -> Unit,
    onCorrectChange: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state.type) {
        CustomEntryType.TRUTH, CustomEntryType.DARE -> TruthOrDareForm(
            state = state,
            onKindChange = onKindChange,
            onTextChange = onTextChange,
            modifier = modifier
        )

        CustomEntryType.STICKY_DARE -> StickyDareForm(
            state = state,
            onTextChange = onTextChange,
            onDurationChange = onDurationChange,
            modifier = modifier
        )

        CustomEntryType.TRIVIA -> TriviaForm(
            state = state,
            onTextChange = onTextChange,
            onOptionAChange = onOptionAChange,
            onOptionBChange = onOptionBChange,
            onCorrectChange = onCorrectChange,
            modifier = modifier
        )
    }
}

@Composable
private fun CreateCustomEntrySample(state: CreateCustomEntryState) {
    Box(modifier = Modifier.fillMaxSize().appBackground()) {
        CreateCustomEntryContent(
            state = state,
            onDealChange = {},
            onKindChange = {},
            onTextChange = {},
            onDurationChange = {},
            onOptionAChange = {},
            onOptionBChange = {},
            onCorrectChange = {},
            onSave = {}
        )
    }
}

@Preview(name = "CreateCustomEntry truth or dare – Light", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun CreateCustomEntryTruthOrDareLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        CreateCustomEntrySample(previewTruthOrDareEntryState)
    }
}

@Preview(name = "CreateCustomEntry truth or dare – Dark", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun CreateCustomEntryTruthOrDareDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        CreateCustomEntrySample(previewTruthOrDareEntryState)
    }
}

@Preview(name = "CreateCustomEntry sticky – Light", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun CreateCustomEntryStickyLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { CreateCustomEntrySample(previewEntryState) }
}

@Preview(name = "CreateCustomEntry sticky – Dark", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun CreateCustomEntryStickyDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { CreateCustomEntrySample(previewEntryState) }
}

@Preview(name = "CreateCustomEntry trivia – Light", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun CreateCustomEntryTriviaLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { CreateCustomEntrySample(previewTriviaEntryState) }
}

@Preview(name = "CreateCustomEntry trivia – Dark", showBackground = true, widthDp = 360, heightDp = 900)
@Composable
private fun CreateCustomEntryTriviaDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { CreateCustomEntrySample(previewTriviaEntryState) }
}
