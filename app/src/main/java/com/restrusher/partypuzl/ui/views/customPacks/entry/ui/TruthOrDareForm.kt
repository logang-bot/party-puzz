package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.ENTRY_TEXT_MAX
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.views.customPacks.create.ui.CharacterCounter
import com.restrusher.partypuzl.ui.views.customPacks.create.ui.PackTextField
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryState
import com.restrusher.partypuzl.ui.views.customPacks.ui.NumberedStep

/** Step 02 for a truth or a dare — one prompt, and nothing else to ask for. */
@Composable
internal fun TruthOrDareForm(
    state: CreateCustomEntryState,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTruth = state.type == CustomEntryType.TRUTH
    Column(modifier = modifier.fillMaxWidth()) {
        NumberedStep(
            number = "02",
            label = stringResource(
                if (isTruth) R.string.custom_entry_step_truth else R.string.custom_entry_step_dare
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        PackTextField(
            value = state.text,
            onValueChange = onTextChange,
            placeholder = stringResource(
                if (isTruth) R.string.custom_entry_truth_hint else R.string.custom_entry_dare_hint
            ),
            singleLine = false,
            minHeight = 108
        )
        CharacterCounter(length = state.text.length, max = ENTRY_TEXT_MAX)
    }
}

private val previewTruthState = CreateCustomEntryState(
    type = CustomEntryType.TRUTH,
    text = "What is the worst thing you have ever said to get out of a round?"
)

@Composable
private fun TruthOrDareFormSample() {
    Column(modifier = Modifier.appBackground().padding(16.dp)) {
        TruthOrDareForm(state = previewTruthState, onTextChange = {})
        TruthOrDareForm(
            state = previewTruthState.copy(type = CustomEntryType.DARE, text = ""),
            onTextChange = {}
        )
    }
}

@Preview(name = "TruthOrDareForm – Light", showBackground = true, widthDp = 360)
@Composable
private fun TruthOrDareFormLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { TruthOrDareFormSample() }
}

@Preview(name = "TruthOrDareForm – Dark", showBackground = true, widthDp = 360)
@Composable
private fun TruthOrDareFormDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { TruthOrDareFormSample() }
}
