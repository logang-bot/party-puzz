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
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appCard
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash
import com.restrusher.partypuzl.ui.views.customPacks.list.CustomPackUiModel
import com.restrusher.partypuzl.ui.views.customPacks.list.PackWarning
import com.restrusher.partypuzl.ui.views.customPacks.model.accent
import com.restrusher.partypuzl.ui.views.customPacks.model.iconRes
import com.restrusher.partypuzl.ui.views.customPacks.model.labelRes
import com.restrusher.partypuzl.ui.views.customPacks.ui.AccentIconTile
import com.restrusher.partypuzl.ui.views.customPacks.ui.MetaChip
import com.restrusher.partypuzl.ui.views.customPacks.ui.RowActionButton

/**
 * One pack in the manager: what it is, how much is in it, whether it will play, and the three
 * things you can do to it — switch it off, edit it, delete it.
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
                    MetaChip(label = stringResource(pack.category.labelRes), tone = accent)
                    MetaChip(label = stringResource(pack.spice.labelRes))
                    Text(
                        text = stringResource(R.string.custom_pack_entries_count, pack.entryCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.ink(Ink.Secondary)
                    )
                }
            }
            Switch(
                checked = pack.isEnabled,
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
    val message = when (warning) {
        PackWarning.EMPTY -> R.string.custom_pack_warning_empty
        PackWarning.TRUTHS_ONLY -> R.string.custom_pack_warning_truths_only
        PackWarning.DARES_ONLY -> R.string.custom_pack_warning_dares_only
    }
    Text(
        text = stringResource(message),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.ink(Ink.Standard),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.appColors.panelFill)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

private val previewPack = CustomPackUiModel(
    id = "custom_preview",
    name = "House Rules",
    description = "Inside jokes from the trip — nobody outside the group will get these.",
    category = PackCategory.TRUTH_OR_DARE,
    spice = SpiceLevel.MEDIUM,
    entryCount = 12,
    isEnabled = true,
    warning = PackWarning.TRUTHS_ONLY
)

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
