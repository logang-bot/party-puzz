package com.restrusher.partypuzz.ui.views.game

import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.common.LockScreenOrientation

private val SPOT_DIAMETER = 56.dp

@Composable
fun FollowTheSpotScreen(
    onGameFinished: (player1Score: Int, player2Score: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FollowTheSpotViewModel = hiltViewModel()
) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SpotBoard(
            spotNormX = uiState.player2SpotNormX,
            spotNormY = uiState.player2SpotNormY,
            isActive = uiState.isGameRunning,
            onSpotTapped = viewModel::onPlayer2SpotTapped,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
        GameDivider(
            player1 = uiState.player1,
            player2 = uiState.player2,
            player1Score = uiState.player1Score,
            player2Score = uiState.player2Score,
            timeRemaining = uiState.timeRemaining,
            isGameRunning = uiState.isGameRunning,
            onExitTapped = { onGameFinished(uiState.player1Score, uiState.player2Score) }
        )
        SpotBoard(
            spotNormX = uiState.player1SpotNormX,
            spotNormY = uiState.player1SpotNormY,
            isActive = uiState.isGameRunning,
            onSpotTapped = viewModel::onPlayer1SpotTapped,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun SpotBoard(
    spotNormX: Float,
    spotNormY: Float,
    isActive: Boolean,
    onSpotTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    BoxWithConstraints(
        modifier = modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        val spotLeft = (maxWidth - SPOT_DIAMETER) * spotNormX
        val spotTop = (maxHeight - SPOT_DIAMETER) * spotNormY

        Box(
            modifier = Modifier
                .offset(x = spotLeft, y = spotTop)
                .size(SPOT_DIAMETER)
                .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                .then(
                    if (isActive) Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onSpotTapped
                    ) else Modifier
                )
        )
    }
}

@Composable
private fun GameDivider(
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
