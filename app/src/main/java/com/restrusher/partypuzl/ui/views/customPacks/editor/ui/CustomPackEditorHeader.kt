package com.restrusher.partypuzl.ui.views.customPacks.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPackUiModel
import com.restrusher.partypuzl.ui.views.customPacks.list.messageRes
import com.restrusher.partypuzl.ui.common.accent
import com.restrusher.partypuzl.ui.common.iconRes
import com.restrusher.partypuzl.ui.common.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.previewPack
import com.restrusher.partypuzl.ui.views.customPacks.ui.AccentIconTile
import com.restrusher.partypuzl.ui.views.customPacks.ui.MetaChip

/** The pack's own details, sitting above the list of entries it holds. */
@Composable
internal fun PackHeader(pack: CustomPackUiModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            AccentIconTile(accent = pack.spice.accent, iconRes = pack.spice.iconRes, size = 56)
            Spacer(modifier = Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MetaChip(
                        label = stringResource(pack.topic.labelRes),
                        tone = pack.spice.accent
                    )
                    MetaChip(label = stringResource(pack.spice.labelRes))
                }
            }
        }
        if (pack.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = pack.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
            )
        }
        pack.warning?.let { warning ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(warning.messageRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.ink(Ink.Standard)
            )
        }
    }
}

/** "12 ENTRIES" with the shortcut that adds another. */
@Composable
internal fun EntriesLabel(count: Int, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.custom_pack_entries_count, count).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onAdd) {
            Text(
                text = stringResource(R.string.custom_pack_add).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.appColors.brandAccent
            )
        }
    }
}

@Composable
private fun PackHeaderSample() {
    Column(modifier = Modifier.appBackground().padding(16.dp)) {
        PackHeader(pack = previewPack)
        EntriesLabel(count = previewPack.entryCount, onAdd = {})
    }
}

@Preview(name = "PackHeader – Light", showBackground = true, widthDp = 360)
@Composable
private fun PackHeaderLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) { PackHeaderSample() }
}

@Preview(name = "PackHeader – Dark", showBackground = true, widthDp = 360)
@Composable
private fun PackHeaderDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) { PackHeaderSample() }
}
