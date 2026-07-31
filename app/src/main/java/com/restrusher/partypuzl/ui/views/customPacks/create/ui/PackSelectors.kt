package com.restrusher.partypuzl.ui.views.customPacks.create.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.PackTopic
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.common.accent
import com.restrusher.partypuzl.ui.common.iconRes
import com.restrusher.partypuzl.ui.common.labelRes
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes

/** The two pickers the create-pack form uses for a pack's topic and its spice. */

/**
 * Selectable pills for what the pack is about.
 *
 * A flow rather than a row: there are eight topics and they are words of very different lengths,
 * so they wrap onto as many lines as they need instead of being squeezed into equal columns.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TopicPills(
    selected: PackTopic,
    onSelect: (PackTopic) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        PackTopic.entries.forEach { topic ->
            val isOn = topic == selected
            val accent = MaterialTheme.colorScheme.primary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isOn) accent.wash(Wash.Hairline)
                        else MaterialTheme.colorScheme.onBackground.wash(Wash.Faint)
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isOn) accent
                        else MaterialTheme.colorScheme.onBackground.wash(Wash.Fill),
                        shape = CircleShape
                    )
                    .clickable { onSelect(topic) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (isOn) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = stringResource(topic.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = if (isOn) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
                )
            }
        }
    }
}

/** Segmented mild / medium / spicy control. The pick drives the pack's icon and accent. */
@Composable
internal fun SpiceSelector(
    selected: SpiceLevel,
    onSelect: (SpiceLevel) -> Unit,
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
        SpiceLevel.entries.forEach { level ->
            val isOn = level == selected
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (isOn) level.accent else Color.Transparent)
                    .clickable { onSelect(level) }
                    .padding(vertical = 10.dp)
            ) {
                Icon(
                    painter = painterResource(level.iconRes),
                    contentDescription = null,
                    tint = if (isOn) MaterialTheme.appColors.onAccentSurface
                    else MaterialTheme.colorScheme.onBackground.ink(Ink.Tertiary),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(level.labelRes).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOn) MaterialTheme.appColors.onAccentSurface
                    else MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
                )
            }
        }
    }
}

@Composable
private fun PackSelectorSamples() {
    Column(modifier = Modifier.appBackground().padding(16.dp)) {
        TopicPills(selected = PackTopic.FRIENDS_INSIDE_JOKES, onSelect = {})
        Spacer(modifier = Modifier.height(16.dp))
        SpiceSelector(selected = SpiceLevel.MEDIUM, onSelect = {})
        Spacer(modifier = Modifier.height(12.dp))
        SpiceSelector(selected = SpiceLevel.SPICY, onSelect = {})
    }
}

@Preview(name = "PackSelectors – Light", showBackground = true, widthDp = 360)
@Composable
private fun PackSelectorsLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { PackSelectorSamples() }
}

@Preview(name = "PackSelectors – Dark", showBackground = true, widthDp = 360)
@Composable
private fun PackSelectorsDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { PackSelectorSamples() }
}
