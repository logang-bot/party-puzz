package com.restrusher.partypuzl.ui.views.customPacks.entry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

/** Reminds the author which pack this is going into — the flow can be several screens deep. */
@Composable
internal fun AddingToCard(packName: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.appColors.panelFill)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.wash(Wash.Fill),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.custom_entry_adding_to).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Tertiary)
            )
            Spacer(modifier = Modifier.size(2.dp))
            Text(
                text = packName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddingToCardSample() {
    Box(modifier = Modifier.appBackground().padding(16.dp)) {
        AddingToCard(packName = "House Rules")
    }
}

@Preview(name = "AddingToCard – Light", showBackground = true, widthDp = 360)
@Composable
private fun AddingToCardLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { AddingToCardSample() }
}

@Preview(name = "AddingToCard – Dark", showBackground = true, widthDp = 360)
@Composable
private fun AddingToCardDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { AddingToCardSample() }
}
