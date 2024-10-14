package com.restrusher.partypuzz.ui.views.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.appModels.GameMode
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GameModeCard(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: (Int, Int) -> Unit,
    gameMode: GameMode,
    modifier: Modifier = Modifier
) {
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Text("Shadow Order")
//
//        Box(
//            modifier = Modifier
//                .size(100.dp)
//                .shadow(
//                    elevation = 10.dp,
//                    spotColor = Color.Red,
//                    shape = RoundedCornerShape(8.dp)
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Hello World")
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
//        Box(
//            modifier = Modifier
//                .background(Color.Red)
//                .size(100.dp)
//                .shadow(
//                    elevation = 10.dp,
//                    shape = RoundedCornerShape(8.dp)
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Hello World")
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        Box(
//            modifier = Modifier
//                .shadow(
//                    elevation = 10.dp,
//                    shape = RoundedCornerShape(8.dp)
//                )
//                .background(Color.Red)
//                .size(100.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Hello World")
//        }
//    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            .clickable {
                onClick(gameMode.name, gameMode.imageId)
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Image(
                    painter = painterResource(id = gameMode.imageId),
                    contentDescription = stringResource(id = R.string.game_mode_image),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(200.dp)
                        .padding(bottom = 10.dp)
                        .sharedElement(state = rememberSharedContentState(key = "game/${gameMode.imageId}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 400)
                            })
                )
                Text(
                    text = stringResource(id = gameMode.name),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.sharedElement(state = rememberSharedContentState(key = "game/${gameMode.name}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(durationMillis = 400)
                        })
                )
                Text(
                    text = stringResource(id = gameMode.description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light
                )
            }

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary,
                    containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = stringResource(id = R.string.play),
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 20.dp))
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Preview(showBackground = true)
fun GameModeCardPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                GameModeCard(
                    animatedVisibilityScope = this, onClick = { _, _ -> }, gameMode = GameMode(
                        R.drawable.img_solo_mode_illustration,
                        R.string.solo_game_mode,
                        R.string.solo_description
                    )
                )
            }
        }
    }
}