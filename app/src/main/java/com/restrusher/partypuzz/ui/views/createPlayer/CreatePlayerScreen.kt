package com.restrusher.partypuzz.ui.views.createPlayer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.CreatePlayerScreen(
    setAppBarTitle: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    createPlayerViewModel: CreatePlayerViewModel = viewModel()
) {
    setAppBarTitle(stringResource(id = R.string.create_player))
    var playerName by remember { mutableStateOf("") }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .sharedBounds(
                rememberSharedContentState(key = "bounds"),
                animatedVisibilityScope = animatedVisibilityScope,
                enter = fadeIn(
                    tween(
                        2000,
                        easing = FastOutSlowInEasing
                    )
                ),
                exit = fadeOut(
                    tween(
                        2000,
                        easing = FastOutSlowInEasing
                    )
                ),
                boundsTransform = BoundsTransform { _: Rect, _: Rect ->
                    tween(durationMillis = 2000, easing = FastOutSlowInEasing)
                }
            ),
    ) {

        Box(
            modifier = Modifier
                .width(300.dp)
                .height(300.dp)
                .clip(CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_dummy_avatar),
                contentDescription = stringResource(
                    id = R.string.player_avatar
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = R.drawable.ic_plus), contentDescription = "fds",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onTertiary), modifier = Modifier
                            .width(30.dp)
                            .height(30.dp))
                    Text(text = "Take new photo", color = MaterialTheme.colorScheme.onSecondary)
                }
            }
        }

        Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )) {
            Text(text = stringResource(id = R.string.generate_random_image))
        }

        Spacer(modifier = Modifier.height(20.dp))
        NameContainer(value = playerName, onValueChanged = { playerName = it })
    }
}

@Composable
fun NameContainer(
    value: String, onValueChanged: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(text = stringResource(id = R.string.players_name)) },
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
        )
    )

    Button(onClick = { /*TODO*/ }, colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary
    )) {
        Text(text = stringResource(id = R.string.generate_random_name))
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
fun CreatePlayerScreenPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                CreatePlayerScreen({}, animatedVisibilityScope = this)
            }
        }
    }
}