package com.restrusher.partypuzl.ui.views.gameConfig.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash

/**
 * One pack row, shared by the Official / Premium / Custom groups.
 *
 * The three tiers use identical chrome and differ only in the badge and the trailing control —
 * an enabled row tints itself with the pack's accent, a locked premium row shows an Unlock pill
 * instead of the check. Matches the "row highlight" option from the design's pack variants.
 */
@Composable
internal fun PackRow(
    name: String,
    meta: String,
    accent: Color,
    iconRes: Int,
    badgeLabel: String,
    badgeColor: Color,
    isEnabled: Boolean,
    isFirst: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    cornerIcon: Int? = null,
    cornerColor: Color = accent
) {
    val rowBackground by animateColorAsState(
        targetValue = if (isEnabled) accent.wash(Wash.Soft) else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "pack row background"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (isLocked) 0.7f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "pack row alpha"
    )

    val toggleDescription = stringResource(
        if (isEnabled) R.string.pack_enabled_content_description
        else R.string.pack_disabled_content_description,
        name
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (!isFirst) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.appColors.panelFillRaised)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBackground)
                .clickable(onClick = onClick)
                .semantics {
                    contentDescription = toggleDescription
                    if (!isLocked) {
                        toggleableState =
                            if (isEnabled) ToggleableState.On else ToggleableState.Off
                    }
                }
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .alpha(contentAlpha)
        ) {
            PackIconTile(accent = accent, iconRes = iconRes, cornerIcon = cornerIcon, cornerColor = cornerColor)

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onBackground.ink(Ink.Prominent),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PackBadge(label = badgeLabel, color = badgeColor)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isLocked) UnlockPill() else PackCheck(isEnabled = isEnabled, accent = accent)
        }
    }
}
