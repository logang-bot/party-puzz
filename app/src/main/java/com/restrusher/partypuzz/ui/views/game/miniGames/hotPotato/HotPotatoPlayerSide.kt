package com.restrusher.partypuzz.ui.views.game.miniGames.hotPotato

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzz.R
import com.restrusher.partypuzz.data.models.Gender
import com.restrusher.partypuzz.data.models.InterestedIn
import com.restrusher.partypuzz.data.models.Player
import com.restrusher.partypuzz.ui.theme.PartyPuzzTheme
import com.restrusher.partypuzz.ui.views.game.common.PlayerPhoto

@Composable
internal fun HotPotatoHolderCard(
    uiState: HotPotatoState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            uiState.loserIndex != null -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                LoserContent(loser = uiState.loser)
            }

            uiState.isGameRunning -> AnimatedContent(
                targetState = uiState.currentHolderIndex,
                transitionSpec = {
                    // New holder rises from below and scales up from "next" size
                    (slideInVertically(tween(380)) { it } +
                     scaleIn(tween(380), initialScale = 0.45f) +
                     fadeIn(tween(250))) togetherWith
                    // Old holder slides up and out
                    (slideOutVertically(tween(300)) { -it } + fadeOut(tween(200)))
                },
                contentAlignment = Alignment.Center,
                label = "holderTransition"
            ) { holderIndex ->
                val holder = uiState.players.getOrNull(holderIndex)
                val next = if (uiState.players.size > 1)
                    uiState.players[(holderIndex + 1) % uiState.players.size] else null
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    ActiveContent(holder = holder, nextHolder = next)
                }
            }
        }
    }
}

@Composable
private fun ActiveContent(holder: Player?, nextHolder: Player?) {
    Box(modifier = Modifier.size(96.dp).clip(CircleShape)) {
        if (holder != null) PlayerPhoto(player = holder, modifier = Modifier.fillMaxSize())
    }
    Spacer(Modifier.height(16.dp))
    Text(
        text = holder?.nickName.orEmpty(),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(text = "\uD83E\uDD54", fontSize = 48.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.hot_potato_tap_to_pass),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(alpha = 0.85f),
        textAlign = TextAlign.Center
    )
    if (nextHolder != null) {
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.hot_potato_next),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.50f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape)) {
                PlayerPhoto(player = nextHolder, modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = nextHolder.nickName,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun LoserContent(loser: Player?) {
    Text(text = "\uD83D\uDCA5", fontSize = 64.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.hot_potato_boom),
        style = MaterialTheme.typography.displayMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = loser?.nickName.orEmpty(),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = stringResource(R.string.hot_potato_loser_label),
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.80f),
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.tap_to_dismiss),
        style = MaterialTheme.typography.bodySmall,
        color = Color.White.copy(alpha = 0.45f),
        textAlign = TextAlign.Center
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun HolderCardActivePreview() {
    val players = listOf(
        Player(1, "Alice", Gender.Female, InterestedIn.Man),
        Player(2, "Bob", Gender.Male, InterestedIn.Woman)
    )
    PartyPuzzTheme {
        HotPotatoHolderCard(
            uiState = HotPotatoState(players = players, currentHolderIndex = 0, isGameRunning = true),
            modifier = Modifier.fillMaxSize()
        )
    }
}
