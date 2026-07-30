package com.restrusher.partypuzl.ui.views.game.miniGames.tapWar

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.game.common.PlayerPhoto

@Composable
internal fun TugOfWarBar(
    player1: Player?,
    player2: Player?,
    barPosition: Float,
    isGameRunning: Boolean,
    timeRemaining: Int,
    totalDuration: Int,
    onExitTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerAnimatable = remember {
        Animatable(if (totalDuration > 0) timeRemaining / totalDuration.toFloat() else 0f)
    }
    LaunchedEffect(timeRemaining, isGameRunning) {
        if (!isGameRunning || totalDuration <= 0) {
            timerAnimatable.snapTo(0f)
        } else {
            timerAnimatable.snapTo(timeRemaining / totalDuration.toFloat())
            timerAnimatable.animateTo(
                targetValue = maxOf(0f, (timeRemaining - 1) / totalDuration.toFloat()),
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
    }

    val animatedPosition by animateFloatAsState(
        targetValue = barPosition,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "barPosition"
    )
    val runningAlpha by animateFloatAsState(
        targetValue = if (isGameRunning) 1f else 0f,
        animationSpec = tween(300),
        label = "runningAlpha"
    )
    val tapToExitAlpha by animateFloatAsState(
        targetValue = if (!isGameRunning) 1f else 0f,
        animationSpec = tween(600),
        label = "tapToExitAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Transparent)
            .then(
                if (!isGameRunning) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onExitTapped
                ) else Modifier
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(timerAnimatable.value)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .alpha(runningAlpha),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerAvatar(player = player1, isFlipped = false)
            IndicatorTrack(animatedPosition = animatedPosition, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            PlayerAvatar(player = player2, isFlipped = true)
        }

        Text(
            text = stringResource(R.string.tap_to_exit),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Center).alpha(tapToExitAlpha)
        )
    }
}

@Composable
private fun PlayerAvatar(player: Player?, isFlipped: Boolean) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .then(if (isFlipped) Modifier.rotate(180f) else Modifier)
    ) {
        if (player != null) PlayerPhoto(player = player, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun IndicatorTrack(animatedPosition: Float, modifier: Modifier = Modifier) {
    val leftWeight = animatedPosition.coerceAtLeast(0.001f)
    val rightWeight = (1f - animatedPosition).coerceAtLeast(0.001f)

    Box(modifier = modifier.height(24.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.outline.ink(Ink.Muted))
        )
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(leftWeight))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.weight(rightWeight))
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewPlayer1 = Player(id = 1, nickName = "Alice", gender = Gender.Female, interestedIn = InterestedIn.Man)
private val previewPlayer2 = Player(id = 2, nickName = "Bob", gender = Gender.Male, interestedIn = InterestedIn.Woman)

@Preview(name = "TugOfWarBar – running center (Light)", showBackground = true, widthDp = 360, heightDp = 80)
@Preview(name = "TugOfWarBar – running center (Dark)", showBackground = true, widthDp = 360, heightDp = 80, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TugOfWarBarCenteredPreview() {
    PartyPuzlTheme {
        TugOfWarBar(player1 = previewPlayer1, player2 = previewPlayer2, barPosition = 0.5f, isGameRunning = true, timeRemaining = 7, totalDuration = TapWarViewModel.GAME_DURATION_SECONDS, onExitTapped = {})
    }
}

@Preview(name = "TugOfWarBar – player1 winning (Light)", showBackground = true, widthDp = 360, heightDp = 80)
@Preview(name = "TugOfWarBar – player1 winning (Dark)", showBackground = true, widthDp = 360, heightDp = 80, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TugOfWarBarP1WinningPreview() {
    PartyPuzlTheme {
        TugOfWarBar(player1 = previewPlayer1, player2 = previewPlayer2, barPosition = 0.8f, isGameRunning = true, timeRemaining = 4, totalDuration = TapWarViewModel.GAME_DURATION_SECONDS, onExitTapped = {})
    }
}

@Preview(name = "TugOfWarBar – tap to exit (Light)", showBackground = true, widthDp = 360, heightDp = 80)
@Preview(name = "TugOfWarBar – tap to exit (Dark)", showBackground = true, widthDp = 360, heightDp = 80, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TugOfWarBarTapToExitPreview() {
    PartyPuzlTheme {
        TugOfWarBar(player1 = previewPlayer1, player2 = previewPlayer2, barPosition = 1f, isGameRunning = false, timeRemaining = 0, totalDuration = TapWarViewModel.GAME_DURATION_SECONDS, onExitTapped = {})
    }
}
