package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme

private const val REEL_HOLD_MS = 200

/** "Surprise me" — spins through the four deals and lands on the one the game already picked. */
@Composable
internal fun SurpriseShuffleContent(
    dealTypes: List<GameDealType>,
    targetDealType: GameDealType?,
    modifier: Modifier = Modifier
) {
    val pulse = rememberInfiniteTransition(label = "shuffle pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "shuffle pulse scale"
    )

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
                .scale(pulseScale)
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_random),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.shuffling_the_deal).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 3.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        SlotReel(
            itemCount = dealTypes.size,
            targetIndex = dealTypes.indexOf(targetDealType).coerceAtLeast(0),
            durationMillis = (SURPRISE_SHUFFLE_DURATION_MS - REEL_HOLD_MS).toInt(),
            tone = MaterialTheme.colorScheme.primary,
            maskColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.widthIn(max = 300.dp)
        ) { index ->
            DealReelRow(dealType = dealTypes[index])
        }
    }
}

@Composable
private fun DealReelRow(dealType: GameDealType, modifier: Modifier = Modifier) {
    val accent = dealType.accent

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(accent.iconRes),
            contentDescription = null,
            tint = accent.tone,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = stringResource(accent.labelRes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(name = "SurpriseShuffle – Light", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun SurpriseShuffleLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().fillMaxSize()) {
            SurpriseShuffleContent(
                dealTypes = GameDealType.entries,
                targetDealType = GameDealType.STICKY_DARE
            )
        }
    }
}

@Preview(name = "SurpriseShuffle – Dark", showBackground = true, widthDp = 360, heightDp = 560)
@Composable
private fun SurpriseShuffleDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().fillMaxSize()) {
            SurpriseShuffleContent(
                dealTypes = GameDealType.entries,
                targetDealType = GameDealType.MINI_GAME
            )
        }
    }
}
