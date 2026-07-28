package com.restrusher.partypuzl.ui.views.gameConfig.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.restrusher.partypuzl.ui.views.gameConfig.PackUiModel

/**
 * "Choose your packs" — the setup screen's replacement for the old deal-category toggles.
 *
 * Three groups, one row design: Official (free), Premium (needs unlocking) and Custom (no
 * authoring flow yet, so it shows the design's empty state).
 */
@Composable
internal fun QuestionPacksSection(
    officialPacks: List<PackUiModel>,
    premiumPacks: List<PackUiModel>,
    onTogglePack: (String) -> Unit,
    onUnlockPack: (PackUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        GameConfigSectionLabel(stringResource(R.string.choose_your_packs))
        Text(
            text = stringResource(R.string.choose_your_packs_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        if (officialPacks.isNotEmpty()) {
            PackGroupLabel(stringResource(R.string.pack_group_official))
            PackGroup {
                officialPacks.forEachIndexed { index, pack ->
                    PackRow(
                        name = stringResource(pack.nameRes),
                        meta = stringResource(R.string.pack_prompts_count, pack.promptCount),
                        accent = pack.accent,
                        iconRes = pack.iconRes,
                        badgeLabel = stringResource(R.string.pack_group_official),
                        badgeColor = OfficialBadgeColor,
                        isEnabled = pack.isEnabled,
                        isFirst = index == 0,
                        cornerIcon = R.drawable.ic_check,
                        cornerColor = pack.accent,
                        onClick = { onTogglePack(pack.id) }
                    )
                }
            }
        }

        if (premiumPacks.isNotEmpty()) {
            PackGroupLabel(stringResource(R.string.pack_group_premium))
            PackGroup {
                premiumPacks.forEachIndexed { index, pack ->
                    PackRow(
                        name = stringResource(pack.nameRes),
                        meta = premiumMeta(pack),
                        accent = pack.accent,
                        iconRes = pack.iconRes,
                        badgeLabel = stringResource(R.string.pack_group_premium),
                        badgeColor = PremiumBadgeColor,
                        isEnabled = pack.isEnabled,
                        isLocked = !pack.isUnlocked,
                        isFirst = index == 0,
                        cornerIcon = if (pack.isUnlocked) null else R.drawable.ic_lock,
                        cornerColor = PremiumBadgeColor,
                        onClick = {
                            if (pack.isUnlocked) onTogglePack(pack.id) else onUnlockPack(pack)
                        }
                    )
                }
            }
        }

        PackGroupLabel(stringResource(R.string.pack_group_custom_count, 0, 0))
        CustomPacksEmptyState()
    }
}

/** Meta line for a premium row — flags a session unlock so it doesn't look permanent. */
@Composable
private fun premiumMeta(pack: PackUiModel): String {
    val prompts = stringResource(R.string.pack_prompts_count, pack.promptCount)
    return if (pack.isSessionUnlocked) {
        "$prompts · ${stringResource(R.string.pack_unlocked_session)}"
    } else {
        prompts
    }
}

@Composable
private fun PackGroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        letterSpacing = 1.6.sp,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
        modifier = modifier.padding(top = 18.dp, bottom = 2.dp)
    )
}

/** Rounded container the rows share, so dividers read as one grouped card. */
@Composable
private fun PackGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}

/**
 * Custom packs have no authoring flow yet, so the group shows the design's dashed prompt with
 * a "coming soon" note rather than a dead button.
 */
@Composable
private fun CustomPacksEmptyState(modifier: Modifier = Modifier) {
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
            .padding(horizontal = 16.dp, vertical = 22.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
            modifier = Modifier.size(22.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.pack_write_first),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(CustomBadgeColor.copy(alpha = 0.14f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.pack_coming_soon).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = CustomBadgeColor
                )
            }
        }
        Text(
            text = stringResource(R.string.pack_write_first_subtitle),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}
