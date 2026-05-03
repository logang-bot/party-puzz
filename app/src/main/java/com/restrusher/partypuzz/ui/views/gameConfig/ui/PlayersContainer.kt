package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

private const val MAX_PLAYERS = 8

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Composable
fun SharedTransitionScope.PlayersContainer(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAddPlayerClick: () -> Unit,
    onDeletePlayer: (Player) -> Unit,
    onEditPlayer: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    // Local display list drives the forEach — it keeps animating-out players visible
    // until their exit animation completes, regardless of GamePlayersList.PlayersList.
    val displayList = remember { mutableStateListOf<Player>().also { it.addAll(GamePlayersList.PlayersList) } }

    // Keep displayList in sync with external additions (e.g. after CreatePlayer returns).
    // We deliberately never remove here — removal is driven by the exit animation below.
    LaunchedEffect(Unit) {
        snapshotFlow { GamePlayersList.PlayersList.toList() }
            .distinctUntilChanged()
            .collect { latest ->
                val displayIds = displayList.map { it.id }.toSet()
                latest.forEach { player ->
                    if (player.id !in displayIds) displayList.add(player)
                }
            }
    }

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GameConfigSectionLabel(text = stringResource(id = R.string.players))
            Text(
                text = " · ${GamePlayersList.PlayersList.size} ${stringResource(R.string.added)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            displayList.forEach { player ->
                key(player.id) {
                    val visibleState = remember {
                        MutableTransitionState(false).apply { targetState = true }
                    }

                    AnimatedVisibility(
                        visibleState = visibleState,
                        enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 2 },
                        exit = shrinkHorizontally(tween(250)) + fadeOut(tween(250))
                    ) {
                        PlayerDataCard(
                            player = player,
                            onCardClick = { onEditPlayer(player) },
                            onDeleteClick = {
                                // Trigger animation and immediately propagate deletion so the
                                // button state and DB update without waiting for the animation.
                                visibleState.targetState = false
                                onDeletePlayer(player)
                            },
                            modifier = Modifier
                                .width(CARD_WIDTH)
                                .height(CARD_HEIGHT)
                                .sharedBounds(
                                    rememberSharedContentState(key = "player_card_${player.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = BoundsTransform { _, _ ->
                                        tween(durationMillis = 500, easing = FastOutSlowInEasing)
                                    }
                                )
                        )
                    }

                    // Outside AnimatedVisibility so it stays in composition through the entire
                    // exit animation. When the animation is done we remove from displayList here.
                    LaunchedEffect(visibleState.isIdle, visibleState.currentState) {
                        if (visibleState.isIdle && !visibleState.currentState) {
                            displayList.removeAll { it.id == player.id }
                        }
                    }
                }
            }

            if (GamePlayersList.PlayersList.size < MAX_PLAYERS) {
                AddPlayerCard(
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier
                        .width(CARD_WIDTH)
                        .height(CARD_HEIGHT)
                        .clickable { onAddPlayerClick() }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun PlayersContainerPreview() {
    PartyPuzzTheme {
        GamePlayersList.setBaseNumberOfPlayers(4)
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                PlayersContainer(
                    animatedVisibilityScope = this,
                    onAddPlayerClick = { },
                    onDeletePlayer = { },
                    onEditPlayer = { }
                )
            }
        }
    }
}
