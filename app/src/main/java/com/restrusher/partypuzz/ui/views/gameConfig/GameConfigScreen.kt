package com.restrusher.partypuzz.ui.views.gameConfig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.appDataSource.GamePlayersList
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GameConfigScreen(
    setAppBarTitle: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    gameModeName: Int,
    gameModeImage: Int,
    modifier: Modifier = Modifier
) {
    setAppBarTitle(stringResource(id = R.string.prepare_your_party))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = gameModeName),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .sharedElement(state = rememberSharedContentState(key = "game/${gameModeName}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 400)
                    })
        )
        Image(
            painter = painterResource(id = gameModeImage),
            contentDescription = stringResource(id = R.string.game_mode_image),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(170.dp)
                .padding(vertical = 20.dp)
                .sharedElement(state = rememberSharedContentState(key = "game/${gameModeImage}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 400)
                    })
        )

        Spacer(modifier = Modifier.height(10.dp))

        NamesContainer()

        Spacer(modifier = Modifier.height(5.dp))

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

        OptionsContainer()

    }
}

@Composable
fun NamesContainer(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.enter_the_players_name),
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
    LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        GamePlayersList.setBaseNumberOfPlayers(2)
        items(GamePlayersList.PlayersList) { player ->
            PlayerDataCard(player)
        }
    }
}

@Composable
fun PlayerDataCard(
    player: Player, modifier: Modifier = Modifier
) {
    var playerName by remember { mutableStateOf("") }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ), modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            PlayerAvatar()
            TextField(value = playerName,
                onValueChange = { playerName = it },
                singleLine = true,
                placeholder = {
                    Text(
                        stringResource(id = R.string.players_name),
                        Modifier.alpha(0.5f)
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = painterResource(id = R.drawable.ic_male),
                contentDescription = stringResource(
                    id = R.string.player_avatar
                ),
                modifier = Modifier.padding(end = 5.dp)
            )
        }
    }
}

@Composable
fun PlayerAvatar() {
    Box(modifier = Modifier.padding(end = 5.dp)) {
        Image(
            painter = painterResource(id = R.drawable.img_dummy_avatar),
            contentDescription = stringResource(
                id = R.string.player_avatar
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
fun OptionsContainer() {
    Column {
        Text(
            text = stringResource(id = R.string.customize_your_party),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            OptionCard(stringResource(id = R.string.bar_mode))
            OptionCard(stringResource(id = R.string.can_skip_questions))
            OptionCard(stringResource(id = R.string.unlimited_mode))
        }
    }

}

@Composable
fun OptionCard(
    optionName: String,
    modifier: Modifier = Modifier
) {
    var isChecked by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Text(text = optionName, style = MaterialTheme.typography.labelLarge,  modifier = Modifier.weight(1f))
        Switch(checked = isChecked, onCheckedChange = { isChecked = it })
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun GameConfigScreenPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                GameConfigScreen(
                    setAppBarTitle = { },
                    animatedVisibilityScope = this,
                    gameModeName = R.string.party_puzz_game_mode,
                    gameModeImage = R.drawable.img_partypuzz_mode_illustration
                )
            }
        }
    }
}