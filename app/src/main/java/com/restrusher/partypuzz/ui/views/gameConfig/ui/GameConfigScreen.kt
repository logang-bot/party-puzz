package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GameConfigScreen(
    setAppBarTitle: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    gameModeName: Int,
    gameModeImage: Int,
    onCreatePlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = stringResource(id = R.string.prepare_your_party)
    LaunchedEffect(key1 = title) {
        delay(100)
        setAppBarTitle(title)
    }
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = gameModeName),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .sharedElement(state = rememberSharedContentState(key = "game/${gameModeName}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 400)
                            })
                )
                Spacer(modifier = Modifier
                    .width(10.dp)
                    .weight(1f))
                Image(
                    painter = painterResource(id = gameModeImage),
                    contentDescription = stringResource(id = R.string.game_mode_image),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(85.dp)
                        .padding(vertical = 20.dp)
                        .sharedElement(state = rememberSharedContentState(key = "game/${gameModeImage}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 400)
                            })
                )
            }
            Text(text = stringResource(id = R.string.gather_your_friends_for_a_night_of_fun), style = MaterialTheme.typography.labelLarge)
            OptionsContainer()
            Spacer(modifier = Modifier.height(10.dp))
            PlayersContainer(
                animatedVisibilityScope = animatedVisibilityScope,
                onAddPlayerClick = onCreatePlayerClick,
                modifier = Modifier.fillMaxWidth())
        }
        StartGameButton(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp))
    }
}

@Composable
fun StartGameButton(
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {

        },
        modifier = modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary
                )
            ),
            shape = ButtonDefaults.shape
        ),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
    ) {
        Text(text = stringResource(id = R.string.start_game).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
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
                    gameModeImage = R.drawable.img_partypuzz_mode_illustration,
                    onCreatePlayerClick = {}
                )
            }
        }
    }
}
