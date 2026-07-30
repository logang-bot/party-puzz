package com.restrusher.partypuzl.ui.views.customPacks.create.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.PACK_DESCRIPTION_MAX
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash

/** The typed inputs the create-pack and entry forms are built from. */

/**
 * Bordered text field matching `NameOptionsContainer` on the create-player screen: the box draws
 * the border, so the field's own indicators are switched off.
 */
@Composable
internal fun PackTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Int = 56,
    /** Off when the caller already draws a border around the field, as the trivia options do. */
    bordered: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (bordered) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.wash(Wash.Hairline),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else {
                    Modifier
                }
            )
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.ink(Ink.Muted)
                )
            },
            singleLine = singleLine,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp)
        )
    }
}

/** Right-aligned "n/max" counter shown under a length-capped field. */
@Composable
internal fun CharacterCounter(
    length: Int,
    modifier: Modifier = Modifier,
    max: Int = PACK_DESCRIPTION_MAX
) {
    Text(
        text = stringResource(R.string.custom_pack_counter, length, max),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.End,
        color = MaterialTheme.colorScheme.onBackground.ink(Ink.Tertiary),
        modifier = modifier.fillMaxWidth().padding(top = 6.dp)
    )
}

@Composable
private fun PackTextFieldSamples() {
    Column(modifier = Modifier.appBackground().padding(16.dp)) {
        PackTextField(value = "House Rules", onValueChange = {}, placeholder = "Name your pack")
        Spacer(modifier = Modifier.height(12.dp))
        PackTextField(value = "", onValueChange = {}, placeholder = "Name your pack")
        Spacer(modifier = Modifier.height(12.dp))
        PackTextField(
            value = "Inside jokes from the trip — nobody outside the group will get these.",
            onValueChange = {},
            placeholder = "Describe it",
            singleLine = false,
            minHeight = 96
        )
        CharacterCounter(length = 68)
    }
}

@Preview(name = "PackTextField – Light", showBackground = true, widthDp = 360)
@Composable
private fun PackTextFieldLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { PackTextFieldSamples() }
}

@Preview(name = "PackTextField – Dark", showBackground = true, widthDp = 360)
@Composable
private fun PackTextFieldDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { PackTextFieldSamples() }
}
