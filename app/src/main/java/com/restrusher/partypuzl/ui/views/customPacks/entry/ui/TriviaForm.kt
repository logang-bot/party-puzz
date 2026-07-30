package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.TRIVIA_TEXT_MAX
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.AccentLime
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash
import com.restrusher.partypuzl.ui.views.customPacks.create.ui.CharacterCounter
import com.restrusher.partypuzl.ui.views.customPacks.create.ui.PackTextField
import com.restrusher.partypuzl.ui.views.customPacks.entry.CreateCustomEntryState
import com.restrusher.partypuzl.ui.views.customPacks.previewTriviaEntryState
import com.restrusher.partypuzl.ui.views.customPacks.ui.NumberedStep

/** Steps 02 and 03 for trivia — the question, then the two answers and which one is right. */
@Composable
internal fun TriviaForm(
    state: CreateCustomEntryState,
    onTextChange: (String) -> Unit,
    onOptionAChange: (String) -> Unit,
    onOptionBChange: (String) -> Unit,
    onCorrectChange: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        NumberedStep(number = "02", label = stringResource(R.string.custom_entry_step_trivia))
        Spacer(modifier = Modifier.height(10.dp))
        PackTextField(
            value = state.text,
            onValueChange = onTextChange,
            placeholder = stringResource(R.string.custom_entry_trivia_hint),
            singleLine = false,
            minHeight = 88
        )
        CharacterCounter(length = state.text.length, max = TRIVIA_TEXT_MAX)

        NumberedStep(
            number = "03",
            label = stringResource(R.string.custom_entry_step_options),
            subtitle = stringResource(R.string.custom_entry_step_options_sub)
        )
        Spacer(modifier = Modifier.height(10.dp))
        TriviaOptionField(
            value = state.optionA,
            onValueChange = onOptionAChange,
            placeholder = stringResource(R.string.custom_entry_option_a),
            isCorrect = state.correctOption == 'A',
            onMarkCorrect = { onCorrectChange('A') }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TriviaOptionField(
            value = state.optionB,
            onValueChange = onOptionBChange,
            placeholder = stringResource(R.string.custom_entry_option_b),
            isCorrect = state.correctOption == 'B',
            onMarkCorrect = { onCorrectChange('B') }
        )
    }
}

/** One answer, with the radio that marks it as the right one. */
@Composable
private fun TriviaOptionField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isCorrect: Boolean,
    onMarkCorrect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCorrect) AccentLime.wash(Wash.Fill) else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (isCorrect) AccentLime.ink(Ink.Secondary)
                else MaterialTheme.colorScheme.onBackground.wash(Wash.Hairline),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(start = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isCorrect) AccentLime else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (isCorrect) Color.Transparent
                    else MaterialTheme.colorScheme.onBackground.ink(Ink.Faint),
                    shape = CircleShape
                )
                .clickable(onClick = onMarkCorrect)
        ) {
            if (isCorrect) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = MaterialTheme.appColors.onAccentSurface,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
        PackTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            bordered = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TriviaFormSample() {
    Column(modifier = Modifier.appBackground().padding(16.dp)) {
        TriviaForm(
            state = previewTriviaEntryState,
            onTextChange = {},
            onOptionAChange = {},
            onOptionBChange = {},
            onCorrectChange = {}
        )
    }
}

@Preview(name = "TriviaForm – Light", showBackground = true, widthDp = 360)
@Composable
private fun TriviaFormLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { TriviaFormSample() }
}

@Preview(name = "TriviaForm – Dark", showBackground = true, widthDp = 360)
@Composable
private fun TriviaFormDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { TriviaFormSample() }
}
