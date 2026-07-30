package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.common.ClockHandAnimation
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.appColors
import com.restrusher.partypuzl.ui.theme.ink
import com.restrusher.partypuzl.ui.views.game.common.PlayerPhoto

private val avatarSize = 46.dp

@Composable
internal fun PlayersListRow(
    players: List<Player>,
    selectedPlayer: Player?,
    activeStickyDares: List<ActiveStickyDare>,
    onPlayerTapped: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedPlayer) {
        val index = players.indexOf(selectedPlayer ?: return@LaunchedEffect)
        if (index >= 0) listState.animateScrollToItem(index)
    }

    LazyRow(
        state = listState,
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        items(players, key = { it.id }) { player ->
            val hasActiveDare = activeStickyDares.any {
                it.playerName == player.nickName && !it.isCompleted
            }
            PlayerAvatarCard(
                player = player,
                isHighlighted = player == selectedPlayer,
                hasActiveDare = hasActiveDare,
                onTapped = { onPlayerTapped(player) }
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
    val ringColor by animateColorAsState(
        targetValue = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(300),
        label = "highlight ring"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .border(2.dp, ringColor, playerCardShape)
            .padding(3.dp)
            .size(avatarSize)
            .clip(playerCardShape)
            .clickable(interactionSource = interactionSource, indication = null) { onTapped() }
    ) {
        PlayerPhoto(player = player, modifier = Modifier.fillMaxSize())
        if (hasActiveDare) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.appColors.chipScrim.ink(Ink.Tertiary))
            ) {
                ClockHandAnimation(
                    color = MaterialTheme.appColors.onAccentSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private val previewPlayers = listOf(
    Player(1, "Alice", Gender.Female, InterestedIn.Man),
    Player(2, "Bruno", Gender.Male, InterestedIn.Woman),
    Player(3, "Cleo", Gender.Female, InterestedIn.Both)
)

@Preview(name = "PlayersListRow – Light", showBackground = true, widthDp = 360, heightDp = 90)
@Composable
private fun PlayersListRowLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        PlayersListRow(
            players = previewPlayers,
            selectedPlayer = previewPlayers[1],
            activeStickyDares = emptyList(),
            onPlayerTapped = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "PlayersListRow – Dark", showBackground = true, widthDp = 360, heightDp = 90)
@Composable
private fun PlayersListRowDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground()) {
            PlayersListRow(
                players = previewPlayers,
                selectedPlayer = previewPlayers.first(),
                activeStickyDares = listOf(
                    ActiveStickyDare(
                        id = "d1",
                        playerName = "Cleo",
                        presentContinuousText = "speaking in rhyme",
                        durationLabel = "2 min",
                        totalSeconds = 120,
                        remainingSeconds = 74
                    )
                ),
                onPlayerTapped = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
