package com.restrusher.partypuzl.ui.views.customPacks.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R

/**
 * The pieces the four custom-pack screens share. Kept here rather than in `ui/common` because
 * nothing outside this feature uses them yet.
 */

/**
 * The design's numbered step header — "STEP 01 · Name your pack" with an optional sub-line.
 * Used by the create-pack and entry screens, which are both step-driven forms.
 */
@Composable
internal fun NumberedStep(
    number: String,
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            text = stringResource(R.string.custom_pack_step, number, label).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
            )
        }
    }
}

/** Rounded tile holding a pack or entry icon, tinted by its accent. */
@Composable
internal fun AccentIconTile(
    accent: Color,
    iconRes: Int,
    modifier: Modifier = Modifier,
    size: Int = 44,
    filled: Boolean = false
) {
    val corner = (size * 0.28f).dp
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(corner))
            .background(if (filled) accent else accent.copy(alpha = 0.13f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(corner))
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (filled) MaterialTheme.colorScheme.background else accent,
            modifier = Modifier.size((size * 0.45f).dp)
        )
    }
}

/**
 * "5 minutes" for a sticky dare's duration. The same plural the content loader uses to build the
 * label the game ticker shows, so what the author previews is what the room reads.
 */
@Composable
internal fun stickyDurationLabel(seconds: Int): String {
    val minutes = (seconds / SECONDS_PER_MINUTE).coerceAtLeast(1)
    return pluralStringResource(R.plurals.sticky_dare_duration_minutes, minutes, minutes)
}

private const val SECONDS_PER_MINUTE = 60

/** Small pill used for a pack's category and spice, and an entry's type. */
@Composable
internal fun MetaChip(label: String, modifier: Modifier = Modifier, tone: Color? = null) {
    val color = tone ?: MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.sp,
        letterSpacing = 1.sp,
        color = color,
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** The design's dashed "nothing here yet" box, shared by the pack list and the pack editor. */
@Composable
internal fun DashedEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
        content()
    }
}

/** Text button used for the Edit / Delete pair under a pack or entry row. */
@Composable
internal fun RowActionButton(
    label: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    val color = tint ?: MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp,
            color = color
        )
    }
}
