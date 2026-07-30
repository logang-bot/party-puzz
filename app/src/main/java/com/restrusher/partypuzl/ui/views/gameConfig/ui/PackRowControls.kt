package com.restrusher.partypuzl.ui.views.gameConfig.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash

/**
 * The small controls a [PackRow] is assembled from — icon tile, tier badge, selection check and
 * the locked-state Unlock pill.
 */

/** 36 dp rounded tile holding the pack icon, with an optional corner badge (check / lock). */
@Composable
internal fun PackIconTile(
    accent: Color,
    iconRes: Int,
    cornerIcon: Int?,
    cornerColor: Color
) {
    Box {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.wash(Wash.Fill))
                .border(1.dp, accent.ink(Ink.Faint), RoundedCornerShape(12.dp))
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        if (cornerIcon != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 0.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(cornerColor)
                    .border(2.dp, MaterialTheme.appColors.onAccentSurface, CircleShape)
            ) {
                Icon(
                    painter = painterResource(cornerIcon),
                    contentDescription = null,
                    tint = MaterialTheme.appColors.onAccentSurface,
                    modifier = Modifier.size(9.dp)
                )
            }
        }
    }
}

/** Tier chip — "Official" / "Premium" / "Custom". */
@Composable
internal fun PackBadge(label: String, color: Color) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.sp,
        letterSpacing = 1.sp,
        color = color,
        modifier = Modifier
            .clip(CircleShape)
            .background(color.wash(Wash.Hairline))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/** Trailing selection control — empty ring when off, filled accent circle with a check when on. */
@Composable
internal fun PackCheck(isEnabled: Boolean, accent: Color) {
    val background by animateColorAsState(
        targetValue = if (isEnabled) accent else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "pack check background"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isEnabled) Color.Transparent
        else MaterialTheme.colorScheme.onBackground.ink(Ink.Faint),
        animationSpec = tween(durationMillis = 150),
        label = "pack check border"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.5.dp, borderColor, CircleShape)
    ) {
        if (isEnabled) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = MaterialTheme.appColors.onAccentSurface,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/** Replaces the check on a locked premium row; tapping the row opens the unlock sheet. */
@Composable
internal fun UnlockPill() {
    val tone = MaterialTheme.appColors.badgePremium
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(tone.wash(Wash.Hairline))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_lock),
            contentDescription = null,
            tint = tone,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = stringResource(R.string.pack_unlock).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            color = tone
        )
    }
}
