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
import com.restrusher.partypuzl.ui.theme.appCard
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.views.gameConfig.PackUiModel
import com.restrusher.partypuzl.ui.views.gameConfig.text

/**
 * "Choose your packs" — the setup screen's replacement for the old deal-category toggles.
 *
 * Three groups, one row design: Official (free), Premium (needs unlocking) and Custom (written by
 * the user). Group chrome lives in `PackGroupContainers`, the custom group in `CustomPackGroup`.
 */
@Composable
internal fun QuestionPacksSection(
    officialPacks: List<PackUiModel>,
    premiumPacks: List<PackUiModel>,
    customPacks: List<PackUiModel>,
    onTogglePack: (String) -> Unit,
    onUnlockPack: (PackUiModel) -> Unit,
    onManagePacks: () -> Unit,
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
                        name = pack.name.text(),
                        meta = stringResource(R.string.pack_prompts_count, pack.promptCount),
                        accent = pack.accent,
                        iconRes = pack.iconRes,
                        badgeLabel = stringResource(R.string.pack_group_official),
                        badgeColor = MaterialTheme.appColors.badgeOfficial,
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
                        name = pack.name.text(),
                        meta = premiumMeta(pack),
                        accent = pack.accent,
                        iconRes = pack.iconRes,
                        badgeLabel = stringResource(R.string.pack_group_premium),
                        badgeColor = MaterialTheme.appColors.badgePremium,
                        isEnabled = pack.isEnabled,
                        isLocked = !pack.isUnlocked,
                        isFirst = index == 0,
                        cornerIcon = if (pack.isUnlocked) null else R.drawable.ic_lock,
                        cornerColor = MaterialTheme.appColors.badgePremium,
                        onClick = {
                            if (pack.isUnlocked) onTogglePack(pack.id) else onUnlockPack(pack)
                        }
                    )
                }
            }
        }

        CustomPackGroup(
            customPacks = customPacks,
            onTogglePack = onTogglePack,
            onManagePacks = onManagePacks
        )
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
