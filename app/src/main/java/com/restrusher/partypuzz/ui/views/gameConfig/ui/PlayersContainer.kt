package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.restrusher.partypuzz.data.appDataSource.GamePlayersList
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@Composable
fun PlayersContainer(

    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(text = stringResource(id = R.string.players), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(10.dp))
        LazyVerticalGrid(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            columns = GridCells.Adaptive(minSize = 70.dp)
        ) {
            items(GamePlayersList.PlayersList) { player ->
                PlayerDataCard(player)
            }
            item {
                AddPlayerCard()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayersContainerPreview() {
    PartyPuzzTheme {
        GamePlayersList.setBaseNumberOfPlayers(4)
        PlayersContainer()
    }
}