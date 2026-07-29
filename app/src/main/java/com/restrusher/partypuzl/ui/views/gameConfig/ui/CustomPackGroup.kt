package com.restrusher.partypuzl.ui.views.gameConfig.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.views.gameConfig.PackUiModel
import com.restrusher.partypuzl.ui.views.gameConfig.text

/**
 * The **Custom** group on the setup screen — the user's own packs, toggled per session exactly like
 * the official ones, plus the way through to the manager.
 *
 * With no packs written yet it shows the design's dashed panel, which is now a live button into the
 * authoring flow rather than a "coming soon" placeholder.
 */
@Composable
internal fun CustomPackGroup(
    customPacks: List<PackUiModel>,
    onTogglePack: (String) -> Unit,
    onManagePacks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PackGroupLabel(
                text = stringResource(
                    R.string.pack_group_custom_count,
                    customPacks.count { it.isEnabled },
                    customPacks.size
                ),
                modifier = Modifier.weight(1f)
            )
            if (customPacks.isNotEmpty()) {
                ManageLink(onClick = onManagePacks)
            }
        }

        if (customPacks.isEmpty()) {
            CustomPacksEmptyState(onClick = onManagePacks)
        } else {
            PackGroup {
                customPacks.forEachIndexed { index, pack ->
                    PackRow(
                        name = pack.name.text(),
                        meta = stringResource(R.string.pack_prompts_count, pack.promptCount),
                        accent = pack.accent,
                        iconRes = pack.iconRes,
                        badgeLabel = stringResource(R.string.pack_group_custom),
                        badgeColor = MaterialTheme.appColors.badgeCustom,
                        isEnabled = pack.isEnabled,
                        isFirst = index == 0,
                        cornerIcon = R.drawable.ic_check,
                        cornerColor = pack.accent,
                        onClick = { onTogglePack(pack.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ManageLink(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.pack_manage).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = MaterialTheme.appColors.brandAccent
        )
        Icon(
            painter = painterResource(R.drawable.ic_keyboard_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.appColors.brandAccent,
            modifier = Modifier.size(13.dp)
        )
    }
}

/** Dashed prompt shown until the user has written a pack. Tapping it opens the manager. */
@Composable
private fun CustomPacksEmptyState(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 22.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.appColors.badgeCustom.copy(alpha = 0.14f))
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = null,
                tint = MaterialTheme.appColors.badgeCustom,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            text = stringResource(R.string.pack_write_first),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.appColors.brandAccent
        )
        Text(
            text = stringResource(R.string.pack_write_first_subtitle),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}
