package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.views.game.gameScreen.EventCategory
import com.restrusher.partypuzl.ui.views.game.gameScreen.OUTCOME_SPIN_DURATION_MS
import com.restrusher.partypuzl.ui.views.game.gameScreen.SlotReel

private const val REEL_HOLD_MS = 250

/** The roll that precedes a reward or punishment landing. */
@Composable
internal fun OutcomeSpinContent(
    mode: OutcomeMode,
    category: EventCategory,
    targetIndex: Int,
    modifier: Modifier = Modifier
) {
    val theme = outcomeTheme(mode, category)
    val labels = stringArrayResource(outcomeReelLabelsRes(mode))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(theme.gradient))
        ) {
            Icon(
                painter = painterResource(theme.iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(theme.rollingRes).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 3.sp,
            color = theme.tone,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        SlotReel(
            itemCount = labels.size,
            targetIndex = targetIndex.coerceIn(0, (labels.size - 1).coerceAtLeast(0)),
            durationMillis = (OUTCOME_SPIN_DURATION_MS - REEL_HOLD_MS).toInt(),
            tone = theme.tone,
            maskColor = MaterialTheme.colorScheme.surface,
            itemHeight = 70.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) { index ->
            Text(
                text = labels[index],
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "OutcomeSpin – bar punishment – Dark", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeSpinBarPunishmentDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.background(Color(0xFF0B1F24)).fillMaxSize()) {
            OutcomeSpinContent(
                mode = OutcomeMode.BAR,
                category = EventCategory.PUNISHMENT,
                targetIndex = 3
            )
        }
    }
}

@Preview(name = "OutcomeSpin – couples reward – Light", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun OutcomeSpinCouplesRewardLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.background(Color(0xFFFFF5E6)).fillMaxSize()) {
            OutcomeSpinContent(
                mode = OutcomeMode.COUPLES,
                category = EventCategory.REWARD,
                targetIndex = 0
            )
        }
    }
}
