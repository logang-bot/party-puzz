package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.views.game.common.PlayerPhoto

/**
 * The turn's deal picker. Whichever category was played last — by anyone — is promoted to a hero
 * card; the rest collapse into compact tiles. Truth or Dare promotes as two separate cards so the
 * player commits to a side up front.
 */
@Composable
internal fun DealChoiceContent(
    uiState: GameScreenState,
    onDealChosen: (GameDealType, TruthOrDareChoice?) -> Unit,
    onSurpriseRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        CurrentPlayerHeader(player = uiState.selectedPlayer)
        Spacer(Modifier.height(20.dp))

        val hero = uiState.resolvedHeroDealType
        if (hero == GameDealType.TRUTH_OR_DARE) {
            DealHeroCard(
                accent = truthAccent,
                onClick = { onDealChosen(GameDealType.TRUTH_OR_DARE, TruthOrDareChoice.TRUTH) }
            )
            Spacer(Modifier.height(12.dp))
            DealHeroCard(
                accent = dareAccent,
                onClick = { onDealChosen(GameDealType.TRUTH_OR_DARE, TruthOrDareChoice.DARE) }
            )
        } else {
            DealHeroCard(accent = hero.accent, onClick = { onDealChosen(hero, null) })
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            uiState.compactDealTypes.forEach { dealType ->
                DealCompactCard(
                    accent = dealType.accent,
                    onClick = { onDealChosen(dealType, null) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        SurpriseMeButton(onClick = onSurpriseRequested)
    }
}

@Composable
private fun CurrentPlayerHeader(player: Player?, modifier: Modifier = Modifier) {
    if (player == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        PlayerPhoto(
            player = player,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
        )
        Spacer(Modifier.size(12.dp))
        Column {
            Text(
                text = stringResource(R.string.its_your_turn).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
            Text(
                text = player.nickName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SurpriseMeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val outline = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(dealCompactShape)
            .border(1.dp, outline, dealCompactShape)
            .clickable { onClick() }
            .padding(vertical = 14.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_random),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.surprise_me),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private val previewPlayers = listOf(
    Player(1, "Alice", Gender.Female, InterestedIn.Man),
    Player(2, "Bruno", Gender.Male, InterestedIn.Woman),
    Player(3, "Cleo", Gender.Female, InterestedIn.Both)
)

@Preview(name = "DealChoice – hero Truth or Dare – Light", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun DealChoiceTruthOrDareLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.background(Color(0xFFFFF5E6)).fillMaxSize()) {
            DealChoiceContent(
                uiState = GameScreenState(
                    players = previewPlayers,
                    selectedPlayer = previewPlayers.first(),
                    heroDealType = GameDealType.TRUTH_OR_DARE
                ),
                onDealChosen = { _, _ -> },
                onSurpriseRequested = {}
            )
        }
    }
}

@Preview(name = "DealChoice – hero Mini-game – Dark", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun DealChoiceMiniGameDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.background(Color(0xFF0B1F24)).fillMaxSize()) {
            DealChoiceContent(
                uiState = GameScreenState(
                    players = previewPlayers,
                    selectedPlayer = previewPlayers[1],
                    heroDealType = GameDealType.MINI_GAME
                ),
                onDealChosen = { _, _ -> },
                onSurpriseRequested = {}
            )
        }
    }
}
