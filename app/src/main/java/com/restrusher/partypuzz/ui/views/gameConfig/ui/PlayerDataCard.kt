package com.restrusher.partypuzz.ui.views.gameConfig.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import java.io.File

private val CARD_WIDTH = 80.dp
private val CARD_HEIGHT = 120.dp

@OptIn(ExperimentalSharedTransitionApi::class)
private val boundsTransform = BoundsTransform { _: Rect, _: Rect ->
    tween(durationMillis = 500, easing = FastOutSlowInEasing)
}

@Composable
fun PlayerDataCard(
    player: Player, modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                player.photoPath != null -> AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(player.photoPath))
                        .build(),
                    contentDescription = stringResource(id = R.string.player_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                player.avatarName != null -> {
                    val resId = context.resources.getIdentifier(
                        player.avatarName, "drawable", context.packageName
                    )
                    Image(
                        painter = painterResource(id = if (resId != 0) resId else R.drawable.img_dummy_avatar),
                        contentDescription = stringResource(id = R.string.player_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> Image(
                    painter = painterResource(id = R.drawable.img_dummy_avatar),
                    contentDescription = stringResource(id = R.string.player_avatar),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, colorResource(id = R.color.black))
                        )
                    )
                    .padding(top = 20.dp, start = 6.dp, end = 6.dp, bottom = 5.dp)
            ) {
                Text(
                    text = player.nickName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = colorResource(id = R.color.white)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AddPlayerCard(
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier) {
    Box(modifier = Modifier.sharedBounds(
        rememberSharedContentState(key = "bounds"),
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = boundsTransform,
    )) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                .padding(horizontal = 10.dp, vertical = 25.dp)

        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = stringResource(
                        id = R.string.player_avatar
                    ),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onPrimaryContainer),
                    modifier = Modifier
                        .width(68.dp)
                        .height(68.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = stringResource(id = R.string.add),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(widthDp = 90)
@Composable
fun PlayerDataCardPreview() {
    PartyPuzzTheme {
        PlayerDataCard(
            player = Player.getEmptyPlayer(),
            modifier = Modifier
                .width(CARD_WIDTH)
                .height(CARD_HEIGHT)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(widthDp = 90)
@Composable
fun AddPlayerCardPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                AddPlayerCard(
                    animatedVisibilityScope = this,
                    modifier = Modifier
                        .width(CARD_WIDTH)
                        .height(CARD_HEIGHT)
                )
            }
        }
    }
}
