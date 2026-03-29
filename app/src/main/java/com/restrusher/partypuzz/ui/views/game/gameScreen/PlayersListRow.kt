package com.restrusher.partypuzz.ui.views.game.gameScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.common.ClockHandAnimation
import com.restrusher.partypuzz.ui.views.game.common.PlayerPhoto

@Composable
internal fun PlayersListRow(
    players: List<Player>,
    selectedPlayer: Player?,
    dealPhase: GameDealPhase,
    activeStickyDares: List<ActiveStickyDare>,
    onPlayerTapped: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(dealPhase) {
        if (dealPhase == GameDealPhase.PLAYER_NAME_REVEAL) {
            val index = selectedPlayer?.let { players.indexOf(it) } ?: return@LaunchedEffect
            if (index >= 0) listState.animateScrollToItem(index)
        }
    }

    val isHighlightActive = dealPhase != GameDealPhase.IDLE && dealPhase != GameDealPhase.ANIMATING

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(players, key = { it.id }) { player ->
            val hasActiveDare = activeStickyDares.any {
                it.playerName == player.nickName && !it.isCompleted
            }
            PlayerAvatarCard(
                player = player,
                isHighlighted = isHighlightActive && player == selectedPlayer,
                hasActiveDare = hasActiveDare,
                onTapped = { onPlayerTapped(player) },
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .width(64.dp)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun PlayerAvatarCard(
    player: Player,
    isHighlighted: Boolean,
    hasActiveDare: Boolean,
    onTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isHighlighted) Color.White else Color.Transparent,
        animationSpec = tween(300),
        label = "highlight border"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = playerCardShape,
        modifier = modifier
            .border(2.dp, borderColor, playerCardShape)
            .clickable(interactionSource = interactionSource, indication = null) { onTapped() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PlayerPhoto(
                player = player,
                modifier = Modifier.fillMaxSize()
            )
            if (hasActiveDare) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    ClockHandAnimation(
                        color = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
