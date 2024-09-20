package com.restrusher.partypuzz.ui.views.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.appDataSource.GameModesDatasource
import com.restrusher.partypuzz.data.appModels.GameMode

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreen(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onGameOptionSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary
                )
            )
        )
    ) {
        Column {
            Box(
                modifier = Modifier.padding(
                    top = 15.dp, bottom = 30.dp, start = 15.dp, end = 15.dp
                )
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.welcome),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(id = R.string.glad_to_have_you_back, "John"),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val gamesModes = GameModesDatasource.gameModesList
                Column(
                    modifier = modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    GameModeCard(
                        animatedVisibilityScope,
                        onGameOptionSelected,
                        gamesModes.elementAt(0),
                        modifier = modifier.weight(1.2f)
                    )
                    GameModeCard(
                        animatedVisibilityScope,
                        onGameOptionSelected,
                        gamesModes.elementAt(1),
                        modifier = modifier.weight(0.8f)
                    )
                }
                Column(
                    modifier = modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    GameModeCard(
                        animatedVisibilityScope,
                        onGameOptionSelected,
                        gamesModes.elementAt(2),
                        modifier = modifier.weight(0.8f)
                    )
                    GameModeCard(
                        animatedVisibilityScope,
                        onGameOptionSelected,
                        gamesModes.elementAt(3),
                        modifier = modifier.weight(1.2f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GameModeCard(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: (Int, Int) -> Unit,
    gameMode: GameMode,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            .clickable {
                onClick(gameMode.name, gameMode.imageId)
            }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(3.dp)
        ) {
            Image(
                painter = painterResource(id = gameMode.imageId),
                contentDescription = stringResource(id = R.string.game_mode_image),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(100.dp)
                    .padding(bottom = 10.dp)
                    .sharedElement(
                        state = rememberSharedContentState(key = "game/${gameMode.imageId}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(durationMillis = 400)
                        }
                    )
            )
            Text(
                text = stringResource(id = gameMode.name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.sharedElement(
                    state = rememberSharedContentState(key = "game/${gameMode.name}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 400)
                    }
                )
            )
            Text(
                text = stringResource(id = gameMode.description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}