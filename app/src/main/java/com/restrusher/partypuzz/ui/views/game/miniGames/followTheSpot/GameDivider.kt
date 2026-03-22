package com.restrusher.partypuzz.ui.views.game.miniGames.followTheSpot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Gender
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
    isGameRunning: Boolean,
    onExitTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .then(
                if (!isGameRunning) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onExitTapped
                ) else Modifier
            )
    ) {
        HorizontalDivider(
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )

        if (!isGameRunning) {
            Text(
                text = stringResource(R.string.tap_to_exit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player 2 — rotated 180° since they sit at the top of the device
                Text(
                    text = player2Score.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(180f)
                )
                Spacer(Modifier.width(8.dp))
                if (player2 != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.rotate(180f)
                    ) {
                        Text(
                            text = player2.nickName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = timeRemaining.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.weight(1f))

                // Player 1 — normal orientation, sits at the bottom
                if (player1 != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        ) {
                            PlayerPhoto(player = player1, modifier = Modifier.fillMaxSize())
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = player1.nickName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

// ─── Previews ─────────────────────────────────────────────────────────────────

private val previewPlayer1 = Player(id = 1, nickName = "Alice", gender = Gender.Female)
private val previewPlayer2 = Player(id = 2, nickName = "Bob", gender = Gender.Male)

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
