package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.appDataSource.GamePlayersList

@Composable
fun NamesContainer(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            GamePlayersList.setBaseNumberOfPlayers(4)
            items(GamePlayersList.PlayersList) { player ->
                PlayerDataCard(player)
            }
        }

        TextButton(onClick = { /*TODO*/ }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = stringResource(
                        id = R.string.add_player
                    )
                )
                Text(text = stringResource(id = R.string.add_player))
            }
        }
    }
}