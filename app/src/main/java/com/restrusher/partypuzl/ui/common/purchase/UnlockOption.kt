package com.restrusher.partypuzl.ui.common.purchase

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.AccentCoral
import com.restrusher.partypuzl.ui.theme.AccentYellow
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.Wash
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.theme.wash

/** One tappable way to unlock, as listed on the premium sheets. */
@Composable
internal fun UnlockOption(
    iconRes: Int,
    gradient: List<Color>,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    val borderColor = if (highlighted) MaterialTheme.appColors.badgePremium.ink(Ink.Muted)
    else MaterialTheme.colorScheme.onSurface.wash(Wash.Hairline)
    val containerColor = if (highlighted) MaterialTheme.appColors.badgePremium.wash(Wash.Soft)
    else MaterialTheme.colorScheme.onSurface.wash(Wash.Faint)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(gradient))
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.appColors.onAccentSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.ink(Ink.Standard)
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_keyboard_arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.ink(Ink.Tertiary),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun UnlockOptionSample(themeMode: ThemeMode) {
    PartyPuzlTheme(themeMode = themeMode) {
        Surface {
            UnlockOption(
                iconRes = R.drawable.ic_partypuzz,
                gradient = listOf(AccentYellow, AccentCoral),
                title = "Unlock everything",
                subtitle = "€2.99 one-time · premium packs + unlimited custom packs",
                enabled = true,
                onClick = {},
                highlighted = true,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "UnlockOption – Light", showBackground = true, widthDp = 360)
@Composable
private fun UnlockOptionPreview() = UnlockOptionSample(ThemeMode.LIGHT)

@Preview(name = "UnlockOption – Dark", showBackground = true, widthDp = 360)
@Composable
private fun UnlockOptionDarkPreview() = UnlockOptionSample(ThemeMode.DARK)
