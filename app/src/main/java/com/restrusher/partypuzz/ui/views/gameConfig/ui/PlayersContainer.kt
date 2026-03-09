package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

private const val MAX_PLAYERS = 8

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PlayersContainer(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onAddPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(text = stringResource(id = R.string.players), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .height(IntrinsicSize.Max)
        ) {
            GamePlayersList.PlayersList.forEach { player ->
                val visibleState = remember(player.id) {
                    MutableTransitionState(false).apply { targetState = true }
                }
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 2 }
                ) {
                    PlayerDataCard(
                        player = player,
                        modifier = Modifier
                            .width(80.dp)
                            .fillMaxHeight()
                    )
                }
            }
            if (GamePlayersList.PlayersList.size < MAX_PLAYERS) {
                AddPlayerCard(
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier
                        .width(80.dp)
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
                PlayersContainer(animatedVisibilityScope = this, onAddPlayerClick = { })
            }
        }
    }
}
