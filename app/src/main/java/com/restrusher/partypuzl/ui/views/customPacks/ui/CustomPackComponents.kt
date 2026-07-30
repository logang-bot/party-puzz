package com.restrusher.partypuzl.ui.views.customPacks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.AccentPink
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash

/**
 * The text-shaped pieces the four custom-pack screens share. Kept here rather than in `ui/common`
 * because nothing outside this feature uses them yet. The boxes and tiles live in
 * `CustomPackTiles.kt`.
 */

/**
 * The design's numbered step header — "STEP 01 · Name your pack" with an optional sub-line.
 * Used by the create-pack and entry screens, which are both step-driven forms.
 */
@Composable
internal fun NumberedStep(
    number: String,
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            text = stringResource(R.string.custom_pack_step, number, label).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary)
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Tertiary)
            )
        }
    }
}

/**
 * "5 minutes" for a sticky dare's duration. The same plural the content loader uses to build the
 * label the game ticker shows, so what the author previews is what the room reads.
 */
@Composable
internal fun stickyDurationLabel(seconds: Int): String {
    val minutes = (seconds / SECONDS_PER_MINUTE).coerceAtLeast(1)
    return pluralStringResource(R.plurals.sticky_dare_duration_minutes, minutes, minutes)
}

private const val SECONDS_PER_MINUTE = 60

/** Small pill used for a pack's category and spice, and an entry's type. */
@Composable
internal fun MetaChip(label: String, modifier: Modifier = Modifier, tone: Color? = null) {
    val color = tone ?: MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.sp,
        letterSpacing = 1.sp,
        color = color,
        modifier = modifier
            .clip(CircleShape)
            .background(color.wash(Wash.Hairline))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun NumberedStepSamples() {
    Column(modifier = Modifier.appBackground().padding(16.dp)) {
        NumberedStep(number = "01", label = "Name your pack")
        NumberedStep(
            number = "02",
            label = "Pick a category",
            subtitle = "This decides which game modes can deal your entries."
        )
    }
}

@Preview(name = "NumberedStep – Light", showBackground = true, widthDp = 360)
@Composable
private fun NumberedStepLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { NumberedStepSamples() }
}

@Preview(name = "NumberedStep – Dark", showBackground = true, widthDp = 360)
@Composable
private fun NumberedStepDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { NumberedStepSamples() }
}

@Composable
private fun MetaChipSamples() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.appBackground().padding(16.dp)
    ) {
        MetaChip(label = "Truth or dare", tone = AccentPink)
        MetaChip(label = "Medium")
        MetaChip(label = stickyDurationLabel(300))
    }
}

@Preview(name = "MetaChip – Light", showBackground = true, widthDp = 360)
@Composable
private fun MetaChipLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { MetaChipSamples() }
}

@Preview(name = "MetaChip – Dark", showBackground = true, widthDp = 360)
@Composable
private fun MetaChipDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { MetaChipSamples() }
}
