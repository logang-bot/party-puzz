package com.restrusher.partypuzz.ui.views.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.data.models.Player

@Composable
internal fun PlayersListRow(
    players: List<Player>,
    selectedPlayer: Player?,
    dealPhase: GameDealPhase,
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
            PlayerAvatarCard(
                player = player,
                isHighlighted = isHighlightActive && player == selectedPlayer,
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
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isHighlighted) Color.White else Color.Transparent,
        animationSpec = tween(300),
        label = "highlight border"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = playerCardShape,
        modifier = modifier.border(2.dp, borderColor, playerCardShape)
    ) {
        PlayerPhoto(
            player = player,
            modifier = Modifier.fillMaxSize()
        )
    }
}
