package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.ENTRY_TEXT_MAX
import com.restrusher.partypuzl.data.models.StickyDurationPresets
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.customPacks.create.ui.CharacterCounter
import com.restrusher.partypuzl.ui.views.customPacks.create.ui.PackTextField
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryState
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.previewEntryState
import com.restrusher.partypuzl.ui.views.customPacks.ui.NumberedStep
import com.restrusher.partypuzl.ui.views.customPacks.ui.stickyDurationLabel

/** Steps 02 and 03 for a sticky dare — the prompt, then how long the player is stuck with it. */
@Composable
internal fun StickyDareForm(
    state: CreateCustomEntryState,
    onTextChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        NumberedStep(
            number = "02",
            label = stringResource(R.string.custom_entry_step_sticky),
            subtitle = stringResource(R.string.custom_entry_step_sticky_sub)
        )
        Spacer(modifier = Modifier.height(10.dp))
        PackTextField(
            value = state.text,
            onValueChange = onTextChange,
            placeholder = stringResource(R.string.custom_entry_sticky_hint),
            singleLine = false,
            minHeight = 108
        )
        CharacterCounter(length = state.text.length, max = ENTRY_TEXT_MAX)

        NumberedStep(
            number = "03",
            label = stringResource(R.string.custom_entry_step_duration),
            subtitle = stringResource(R.string.custom_entry_step_duration_sub)
        )
        Spacer(modifier = Modifier.height(10.dp))
        DurationSelector(selected = state.durationSeconds, onSelect = onDurationChange)
    }
}

@Composable
private fun DurationSelector(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = CustomEntryType.STICKY_DARE.accent
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.appColors.panelFill)
            .padding(4.dp)
    ) {
        StickyDurationPresets.forEach { seconds ->
            val isOn = seconds == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isOn) accent else Color.Transparent)
                    .clickable { onSelect(seconds) }
                    .padding(vertical = 11.dp)
            ) {
                Text(
                    text = stickyDurationLabel(seconds),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = if (isOn) MaterialTheme.appColors.onAccentSurface
                    else MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
                )
            }
        }
    }
}

@Composable
private fun StickyDareFormSample() {
    Column(modifier = Modifier.appBackground().padding(16.dp)) {
        StickyDareForm(state = previewEntryState, onTextChange = {}, onDurationChange = {})
    }
}

@Preview(name = "StickyDareForm – Light", showBackground = true, widthDp = 360)
@Composable
private fun StickyDareFormLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { StickyDareFormSample() }
}

@Preview(name = "StickyDareForm – Dark", showBackground = true, widthDp = 360)
@Composable
private fun StickyDareFormDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { StickyDareFormSample() }
}
