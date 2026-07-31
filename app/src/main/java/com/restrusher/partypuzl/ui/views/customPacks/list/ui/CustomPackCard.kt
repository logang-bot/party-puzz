package com.restrusher.partypuzl.ui.views.customPacks.list.ui

import android.content.res.Configuration
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appCard
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPackUiModel
import com.restrusher.partypuzl.ui.views.customPacks.list.PackWarning
import com.restrusher.partypuzl.ui.views.customPacks.list.messageRes
import com.restrusher.partypuzl.ui.views.customPacks.previewPack
import com.restrusher.partypuzl.ui.common.accent
import com.restrusher.partypuzl.ui.common.iconRes
import com.restrusher.partypuzl.ui.common.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.ui.AccentIconTile
import com.restrusher.partypuzl.ui.views.customPacks.ui.MetaChip
import com.restrusher.partypuzl.ui.views.customPacks.ui.RowActionButton

/**
 * One pack in the manager: what it is, how much is in it, whether it will play, and the three
 * things you can do to it — withdraw it, edit it, delete it.
 *
 * The switch is availability, not this session's pick: off means the pack stops being offered on
 * the setup screen at all. Choosing which of the offered packs plays tonight happens there.
 */
@Composable
internal fun CustomPackCard(
    pack: CustomPackUiModel,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = pack.spice.accent
    Column(
        modifier = modifier
            .fillMaxWidth()
            .appCard(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            AccentIconTile(accent = accent, iconRes = pack.spice.iconRes)
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (pack.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pack.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.ink(Ink.Standard),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetaChip(label = stringResource(pack.topic.labelRes), tone = accent)
                    MetaChip(label = stringResource(pack.spice.labelRes))
                    Text(
                        text = stringResource(R.string.custom_pack_entries_count, pack.entryCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.ink(Ink.Secondary)
                    )
                }
            }
            Switch(
                checked = pack.isAvailable,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = accent,
                    checkedThumbColor = MaterialTheme.appColors.onAccentSurface
                )
            )
        }

        if (pack.warning != null) {
            Spacer(modifier = Modifier.height(10.dp))
            PackWarningRow(warning = pack.warning)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            RowActionButton(
                label = stringResource(R.string.custom_pack_edit),
                iconRes = R.drawable.ic_outline_edit,
                onClick = onEdit
            )
            RowActionButton(
                label = stringResource(R.string.custom_pack_delete),
                iconRes = R.drawable.ic_delete,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun PackWarningRow(warning: PackWarning, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(warning.messageRes),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.ink(Ink.Standard),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.appColors.panelFill)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun CustomPackCardPreview() {
    PartyPuzlTheme {
        Column(modifier = Modifier.appBackground().padding(16.dp)) {
            CustomPackCard(previewPack, {}, {}, {}, {})
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CustomPackCardDarkPreview() {
    PartyPuzlTheme {
        Column(modifier = Modifier.appBackground().padding(16.dp)) {
            CustomPackCard(previewPack, {}, {}, {}, {})
        }
    }
}
