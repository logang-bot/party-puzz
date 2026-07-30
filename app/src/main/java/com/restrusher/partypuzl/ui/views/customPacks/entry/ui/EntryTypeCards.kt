package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash
import com.restrusher.partypuzl.ui.views.customPacks.model.EntryDeal
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.model.hintRes
import com.restrusher.partypuzl.ui.views.customPacks.model.iconRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.ui.AccentIconTile

/**
 * Step 01 of the entry form — one full-width card per kind of game deal.
 *
 * Truth and Dare share a card: they are one deal at the table, and which half it is is asked next,
 * in step 02. See [EntryDeal] for why they remain two types underneath.
 */
@Composable
internal fun EntryTypeCards(
    selected: EntryDeal,
    onSelect: (EntryDeal) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        EntryDeal.entries.forEach { deal ->
            EntryTypeCard(
                deal = deal,
                isSelected = deal == selected,
                onClick = { onSelect(deal) }
            )
        }
    }
}

@Composable
private fun EntryTypeCard(
    deal: EntryDeal,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = deal.accent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) accent.wash(Wash.Fill)
                else MaterialTheme.colorScheme.onBackground.wash(Wash.Faint)
            )
            .border(
                width = 1.5.dp,
                color = if (isSelected) accent.ink(Ink.Secondary)
                else MaterialTheme.colorScheme.onBackground.wash(Wash.Fill),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        AccentIconTile(
            accent = accent,
            iconRes = deal.iconRes,
            size = 38,
            filled = isSelected
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(deal.labelRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(deal.hintRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary)
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        SelectionDot(isSelected = isSelected, accent = accent)
    }
}

@Composable
private fun SelectionDot(isSelected: Boolean, accent: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (isSelected) accent else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (isSelected) Color.Transparent
                else MaterialTheme.colorScheme.onBackground.ink(Ink.Faint),
                shape = CircleShape
            )
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.appColors.onAccentSurface,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Preview(name = "EntryTypeCards – Light", showBackground = true, widthDp = 360)
@Composable
private fun EntryTypeCardsLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(modifier = Modifier.appBackground().padding(16.dp)) {
            EntryTypeCards(selected = EntryDeal.TRUTH_OR_DARE, onSelect = {})
        }
    }
}

@Preview(name = "EntryTypeCards – Dark", showBackground = true, widthDp = 360)
@Composable
private fun EntryTypeCardsDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(modifier = Modifier.appBackground().padding(16.dp)) {
            EntryTypeCards(selected = EntryDeal.TRUTH_OR_DARE, onSelect = {})
        }
    }
}
