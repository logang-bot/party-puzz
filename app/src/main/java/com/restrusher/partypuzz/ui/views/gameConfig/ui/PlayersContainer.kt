package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appDataSource.GamePlayersList
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

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
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            items(GamePlayersList.PlayersList) { player ->
                PlayerDataCard(
                    player = player,
                    modifier = Modifier.width(80.dp))
            }
            item {
                AddPlayerCard(animatedVisibilityScope = animatedVisibilityScope, modifier = Modifier.clickable { onAddPlayerClick() })
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