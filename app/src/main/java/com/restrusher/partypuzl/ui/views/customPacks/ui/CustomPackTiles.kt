package com.restrusher.partypuzl.ui.views.customPacks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.AccentViolet
import com.restrusher.partypuzl.ui.theme.BrandTeal
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash

/**
 * The boxed pieces the custom-pack screens share — the icon tile, the empty-state box, and the
 * row actions under a pack or entry. The text-shaped pieces live in `CustomPackComponents.kt`.
 */

/** Rounded tile holding a pack or entry icon, tinted by its accent. */
@Composable
internal fun AccentIconTile(
    accent: Color,
    iconRes: Int,
    modifier: Modifier = Modifier,
    size: Int = 44,
    filled: Boolean = false
) {
    val corner = (size * 0.28f).dp
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(corner))
            .background(if (filled) accent else accent.wash(Wash.Fill))
            .border(1.dp, accent.ink(Ink.Faint), RoundedCornerShape(corner))
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (filled) MaterialTheme.appColors.onAccentSurface else accent,
            modifier = Modifier.size((size * 0.45f).dp)
        )
    }
}

/** The design's dashed "nothing here yet" box, shared by the pack list and the pack editor. */
@Composable
internal fun DashedEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onBackground.wash(Wash.Hairline),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary),
            textAlign = TextAlign.Center
        )
        content()
    }
}

/** Text button used for the Edit / Delete pair under a pack or entry row. */
@Composable
internal fun RowActionButton(
    label: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    val color = tint ?: MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = color
        )
    }
}

@Composable
private fun AccentIconTileSamples() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.appBackground().padding(16.dp)
    ) {
        AccentIconTile(accent = BrandTeal, iconRes = R.drawable.ic_lightbulb)
        AccentIconTile(accent = AccentViolet, iconRes = R.drawable.ic_hourglass, filled = true)
        AccentIconTile(accent = AccentViolet, iconRes = R.drawable.ic_whatshot, size = 56)
    }
}

@Preview(name = "AccentIconTile – Light", showBackground = true, widthDp = 360)
@Composable
private fun AccentIconTileLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { AccentIconTileSamples() }
}

@Preview(name = "AccentIconTile – Dark", showBackground = true, widthDp = 360)
@Composable
private fun AccentIconTileDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { AccentIconTileSamples() }
}

@Composable
private fun DashedEmptyStateSample() {
    Box(modifier = Modifier.appBackground().padding(16.dp)) {
        DashedEmptyState(
            title = "No packs yet",
            subtitle = "Write your own truths, dares and trivia for the group."
        )
    }
}

@Preview(name = "DashedEmptyState – Light", showBackground = true, widthDp = 360)
@Composable
private fun DashedEmptyStateLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { DashedEmptyStateSample() }
}

@Preview(name = "DashedEmptyState – Dark", showBackground = true, widthDp = 360)
@Composable
private fun DashedEmptyStateDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { DashedEmptyStateSample() }
}

@Composable
private fun RowActionButtonSamples() {
    Column(modifier = Modifier.appBackground().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RowActionButton(label = "Edit", iconRes = R.drawable.ic_outline_edit, onClick = {})
            RowActionButton(label = "Delete", iconRes = R.drawable.ic_delete, onClick = {})
        }
        Spacer(modifier = Modifier.height(8.dp))
        RowActionButton(
            label = "Delete",
            iconRes = R.drawable.ic_delete,
            onClick = {},
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Preview(name = "RowActionButton – Light", showBackground = true, widthDp = 360)
@Composable
private fun RowActionButtonLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { RowActionButtonSamples() }
}

@Preview(name = "RowActionButton – Dark", showBackground = true, widthDp = 360)
@Composable
private fun RowActionButtonDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { RowActionButtonSamples() }
}
