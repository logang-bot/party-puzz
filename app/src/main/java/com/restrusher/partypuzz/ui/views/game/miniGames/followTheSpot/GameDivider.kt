package com.restrusher.partypuzz.ui.views.game.miniGames.followTheSpot

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import com.restrusher.partypuzz.ui.views.game.common.PlayerPhoto

@Composable
internal fun GameDivider(
    player1: Player?,
    player2: Player?,
    player1Score: Int,
    player2Score: Int,
    timeRemaining: Int,
    totalDuration: Int = 15,
    isGameRunning: Boolean,
    onExitTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerFraction = if (totalDuration > 0) maxOf(0f, (timeRemaining - 1) / totalDuration.toFloat()) else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = timerFraction,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "timerProgress"
    )
    val tapToExitAlpha by animateFloatAsState(
        targetValue = if (!isGameRunning) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "tapToExitAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f))
            .then(
                if (!isGameRunning) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onExitTapped
                ) else Modifier
            )
    ) {
        // Timer progress background — shrinks from right to left
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedFraction)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        // "Tap to exit" text — fades in when game ends
        Text(
            text = stringResource(R.string.tap_to_exit),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(tapToExitAlpha)
        )

        if (isGameRunning) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player 2 — rotated 180° since they sit at the top of the device
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .rotate(180f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (player2 != null) {
                        Text(
                            text = player2.nickName.substringBefore(" "),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        ) {
                            PlayerPhoto(player = player2, modifier = Modifier.fillMaxSize())
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = player2Score.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = timeRemaining.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Player 1 — normal orientation, sits at the bottom
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (player1 != null) {
                        Text(
                            text = player1.nickName.substringBefore(" "),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        ) {
                            PlayerPhoto(player = player1, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = player1Score.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewPlayer1 = Player(id = 1, nickName = "Alice", gender = Gender.Female, interestedIn = InterestedIn.Man)
private val previewPlayer2 = Player(id = 2, nickName = "Bob", gender = Gender.Male, interestedIn = InterestedIn.Woman)

@Preview(name = "GameDivider – running", showBackground = true, widthDp = 360, heightDp = 80)
@Composable
private fun GameDividerRunningPreview() {
    PartyPuzzTheme {
        GameDivider(
            player1 = previewPlayer1,
            player2 = previewPlayer2,
            player1Score = 3,
            player2Score = 5,
            timeRemaining = 8,
            isGameRunning = true,
            onExitTapped = {}
        )
    }
}

@Preview(name = "GameDivider – tap to exit", showBackground = true, widthDp = 360, heightDp = 80)
@Composable
private fun GameDividerTapToExitPreview() {
    PartyPuzzTheme {
        GameDivider(
            player1 = previewPlayer1,
            player2 = previewPlayer2,
            player1Score = 7,
            player2Score = 4,
            timeRemaining = 0,
            isGameRunning = false,
            onExitTapped = {}
        )
    }
}
