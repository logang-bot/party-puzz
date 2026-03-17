package com.restrusher.partypuzz.ui.views.game

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val SPOT_DIAMETER = 56.dp

@Composable
fun FollowTheSpotScreen(
    modifier: Modifier = Modifier,
    viewModel: FollowTheSpotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
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
            player1Score = uiState.player1Score,
            player2Score = uiState.player2Score,
            timeRemaining = uiState.timeRemaining
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
    player1Score: Int,
    player2Score: Int,
    timeRemaining: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        HorizontalDivider(
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = player2Score.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(180f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = timeRemaining.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = player1Score.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
