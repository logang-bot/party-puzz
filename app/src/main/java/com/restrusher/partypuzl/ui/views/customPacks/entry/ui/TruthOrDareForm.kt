package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.ENTRY_TEXT_MAX
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.AccentPink
import com.restrusher.partypuzl.ui.theme.BrandTeal
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.customPacks.create.ui.CharacterCounter
import com.restrusher.partypuzl.ui.views.customPacks.create.ui.PackTextField
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryState
import com.restrusher.partypuzl.ui.views.customPacks.model.iconRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.ui.NumberedStep

/**
 * Steps 02 and 03 for a truth or a dare — which half it is, then the prompt itself.
 *
 * Step 01 offers the two as one card, so the half is asked here. It is a real choice, not a
 * cosmetic one: it decides which pool the entry joins in the deck.
 */
@Composable
internal fun TruthOrDareForm(
    state: CreateCustomEntryState,
    onKindChange: (CustomEntryType) -> Unit,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTruth = state.type == CustomEntryType.TRUTH
    Column(modifier = modifier.fillMaxWidth()) {
        NumberedStep(
            number = "02",
            label = stringResource(R.string.custom_entry_step_kind),
            subtitle = stringResource(R.string.custom_entry_step_kind_sub)
        )
        Spacer(modifier = Modifier.height(10.dp))
        KindSelector(selected = state.type, onSelect = onKindChange)

        NumberedStep(
            number = "03",
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

/** The two halves the tag can read. Same chrome as the sticky dare's duration selector. */
private val TruthOrDareKinds = listOf(CustomEntryType.TRUTH, CustomEntryType.DARE)

/**
 * The one place truths and dares are contrasted rather than grouped, so it is also the one place
 * they keep separate colours — everywhere else they share [CustomEntryType.accent].
 */
private val CustomEntryType.kindAccent: Color
    get() = if (this == CustomEntryType.DARE) AccentPink else BrandTeal

@Composable
private fun KindSelector(
    selected: CustomEntryType,
    onSelect: (CustomEntryType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.appColors.panelFill)
            .padding(4.dp)
    ) {
        TruthOrDareKinds.forEach { kind ->
            val isOn = kind == selected
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isOn) kind.kindAccent else Color.Transparent)
                    .clickable { onSelect(kind) }
                    .padding(vertical = 11.dp)
            ) {
                val contentColor = if (isOn) MaterialTheme.appColors.onAccentSurface
                else MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
                Icon(
                    painter = painterResource(kind.iconRes),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = stringResource(kind.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = contentColor
                )
            }
        }
    }
}

private val previewTruthState = CreateCustomEntryState(
    type = CustomEntryType.TRUTH,
    text = "What is the worst thing you have ever said to get out of a round?"
)

@Composable
private fun TruthOrDareFormSample() {
    Column(modifier = Modifier.appBackground().padding(16.dp)) {
        TruthOrDareForm(state = previewTruthState, onKindChange = {}, onTextChange = {})
        TruthOrDareForm(
            state = previewTruthState.copy(type = CustomEntryType.DARE, text = ""),
            onKindChange = {},
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
