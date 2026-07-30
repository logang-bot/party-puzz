package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import android.content.res.Configuration
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
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.model.hintRes
import com.restrusher.partypuzl.ui.views.customPacks.model.iconRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.ui.AccentIconTile

/**
 * Step 01 of the entry form — one full-width card per kind of game deal.
 *
 * Truth and Dare are separate cards rather than a sub-toggle because they are separate pools in
 * the deck: the reveal screen draws from one or the other, and a pack holding only one half is
 * worth flagging.
 */
@Composable
internal fun EntryTypeCards(
    selected: CustomEntryType,
    onSelect: (CustomEntryType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        CustomEntryType.entries.forEach { type ->
            EntryTypeCard(
                type = type,
                isSelected = type == selected,
                onClick = { onSelect(type) }
            )
        }
    }
}

@Composable
private fun EntryTypeCard(
    type: CustomEntryType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = type.accent
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
            iconRes = type.iconRes,
            size = 38,
            filled = isSelected
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(type.labelRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(type.hintRes),
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

@Preview(showBackground = true)
@Composable
private fun EntryTypeCardsPreview() {
    PartyPuzlTheme {
        Column(modifier = Modifier.appBackground().padding(16.dp)) {
            EntryTypeCards(selected = CustomEntryType.DARE, onSelect = {})
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EntryTypeCardsDarkPreview() {
    PartyPuzlTheme {
        Column(modifier = Modifier.appBackground().padding(16.dp)) {
            EntryTypeCards(selected = CustomEntryType.DARE, onSelect = {})
        }
    }
}
