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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.local.appData.appModels.GameMode
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.GameModeCard(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onPlayClick: (Int, Int, Int) -> Unit,
    gameMode: GameMode,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A5A6B))
            .clickable { onPlayClick(gameMode.name, gameMode.imageId, gameMode.description) }
    ) {
        Image(
            painter = painterResource(id = gameMode.imageId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxHeight(0.8f)
                .align(Alignment.BottomEnd)
                .alpha(0.15f)
                .blur(6.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = stringResource(R.string.card_mode_label),
                style = MaterialTheme.typography.labelMedium,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = gameMode.name),
                style = MaterialTheme.typography.displayMedium,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
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
                        R.drawable.img_standard_illustration,
                        R.string.standard_game_mode,
                        R.string.standard_description
                    )
                )
            }
        }
    }
}
