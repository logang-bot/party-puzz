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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appModels.GameMode
import com.restrusher.partypuzz.data.local.entities.PlayerEntity
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GameModeCard(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPlayClick: (Int, Int) -> Unit,
    gameMode: GameMode,
    players: List<PlayerEntity>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            .clickable { onPlayClick(gameMode.name, gameMode.imageId) }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxSize()
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
                        .sharedElement(
                            state = rememberSharedContentState(key = "game/${gameMode.imageId}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> tween(durationMillis = 400) }
                        )
                )
                Text(
                    text = stringResource(id = gameMode.name),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState(key = "game/${gameMode.name}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = 400) }
                    )
                )
                Text(
                    text = stringResource(id = gameMode.description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light
                )
            }

            val prefix = stringResource(R.string.game_mode_tap_prefix)
            val modeName = stringResource(id = gameMode.name)
            val suffix = stringResource(R.string.game_mode_tap_suffix)
            val (displayedNames, remaining) = playerNamesSlice(players)
            val andXMore = if (remaining > 0) stringResource(R.string.and_x_more, remaining) else null
            val tapText = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$prefix ")
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)) {
                    append(modeName)
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(" $suffix")
                }
                if (displayedNames.isNotEmpty()) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" with ")
                    }
                    displayedNames.forEachIndexed { index, name ->
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)) {
                            append(name)
                        }
                        if (index < displayedNames.lastIndex) append(", ")
                    }
                    if (andXMore != null) {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)) {
                            append(" $andXMore")
                        }
                    }
                }
            }
            Text(
                text = tapText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall,
                fontSize = 20.sp,
                maxLines = 2,
                lineHeight = 22.sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
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
                    animatedVisibilityScope = this,
                    onPlayClick = { _, _ -> },
                    gameMode = GameMode(
                        R.drawable.img_solo_mode_illustration,
                        R.string.solo_game_mode,
                        R.string.solo_description
                    ),
                    players = emptyList()
                )
            }
        }
    }
}
