package com.restrusher.partypuzz.ui.views.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appModels.GameMode
import com.restrusher.partypuzz.ui.common.gameModeTheme
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GameModeCard(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPlayClick: (Int, Int, Int) -> Unit,
    gameMode: GameMode,
    modifier: Modifier = Modifier,
    selectedPartyName: String? = null
) {
    val theme = gameModeTheme(gameMode.name)
    // Apply slight transparency only to the background, keeping text fully opaque
    val cardGradient = theme.gradientColors.map { it.copy(alpha = 0.9f) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(cardGradient))
            .clickable { onPlayClick(gameMode.name, gameMode.imageId, gameMode.description) }
    ) {
        Image(
            painter = painterResource(id = theme.iconId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alpha = 0.15f,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier
                .fillMaxHeight(0.65f)
                .aspectRatio(1f)
                .align(Alignment.TopEnd)
                .offset(x = 83.dp, y = (-48).dp)
        )
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.card_mode_label),
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(id = gameMode.name),
                    style = MaterialTheme.typography.displayMedium,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState(key = "game/${gameMode.name}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = 400) }
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = gameMode.description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState(key = "game/${gameMode.description}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ -> tween(durationMillis = 400) }
                    )
                )
            }
            val tapToPlayText = if (selectedPartyName != null)
                stringResource(R.string.tap_to_play_with, selectedPartyName)
            else
                stringResource(R.string.tap_to_play)
            AnimatedContent(
                targetState = tapToPlayText,
                transitionSpec = {
                    (slideInVertically(tween(300)) { it / 2 } + fadeIn(tween(300))) togetherWith
                        (slideOutVertically(tween(300)) { -it / 2 } + fadeOut(tween(300)))
                },
                label = "tapToPlay",
                modifier = Modifier.fillMaxWidth()
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun GameModeCardPreview() {
    PartyPuzzTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                GameModeCard(
                    animatedVisibilityScope = this,
                    onPlayClick = { _, _, _ -> },
                    gameMode = GameMode(
                        R.drawable.ic_standard,
                        R.string.standard_game_mode,
                        R.string.standard_description
                    )
                )
            }
        }
    }
}
