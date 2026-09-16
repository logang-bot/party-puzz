package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import com.restrusher.partypuzl.data.models.Player
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground

private const val PROMOTION_MS = 320

private val pickerPreviewPlayers = listOf(
    Player(1, "Alice", Gender.Female, InterestedIn.Man),
    Player(2, "Bruno", Gender.Male, InterestedIn.Woman)
)

/**
 * The hero slot and the compact row, and the movement between them.
 *
 * Tapping a compact tile only *promotes* it — the prompt comes on the tap after that, so a player
 * sees what they picked before they are committed to it, and Truth or Dare gets to open its two
 * sides rather than rolling one. Both slots are rebuilt for the new hero inside one
 * [AnimatedContent]; matching [SharedTransitionScope] keys carry each card between the bounds it
 * had and the bounds it is getting, so the tapped tile rises into the hero slot while the outgoing
 * hero drops into the row.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun DealPicker(
    uiState: GameScreenState,
    onDealPromoted: (GameDealType) -> Unit,
    onDealChosen: (GameDealType, TruthOrDareChoice?) -> Unit,
    modifier: Modifier = Modifier
) {
    SharedTransitionLayout(modifier = modifier) {
        AnimatedContent(
            targetState = uiState.pickerHeroDealType,
            transitionSpec = {
                fadeIn(tween(PROMOTION_MS)) togetherWith fadeOut(tween(PROMOTION_MS))
            },
            label = "deal promotion"
        ) { hero ->
            Column {
                DealHeroSlot(
                    animatedVisibilityScope = this@AnimatedContent,
                    hero = hero,
                    showRevealHint = hero == uiState.promotedDealType,
                    onDealChosen = onDealChosen
                )
                Spacer(Modifier.height(12.dp))
                DealCompactRow(
                    animatedVisibilityScope = this@AnimatedContent,
                    dealTypes = uiState.availableDealTypes - hero,
                    onDealPromoted = onDealPromoted
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.DealHeroSlot(
    animatedVisibilityScope: AnimatedVisibilityScope,
    hero: GameDealType,
    showRevealHint: Boolean,
    onDealChosen: (GameDealType, TruthOrDareChoice?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (hero == GameDealType.TRUTH_OR_DARE) {
        TruthOrDareHeroPair(
            animatedVisibilityScope = animatedVisibilityScope,
            onDealChosen = onDealChosen,
            modifier = modifier
        )
        return
    }
    DealHeroCard(
        accent = hero.accent,
        onClick = { onDealChosen(hero, null) },
        modifier = modifier.then(dealBounds(animatedVisibilityScope, hero)),
        showRevealHint = showRevealHint
    )
}

/**
 * Truth and Dare are two cards for one category, and a key may only be claimed once per layout, so
 * Truth carries the category's bounds and Dare arrives on its own.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.TruthOrDareHeroPair(
    animatedVisibilityScope: AnimatedVisibilityScope,
    onDealChosen: (GameDealType, TruthOrDareChoice?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        DealHeroCard(
            accent = truthAccent,
            onClick = { onDealChosen(GameDealType.TRUTH_OR_DARE, TruthOrDareChoice.TRUTH) },
            modifier = Modifier.then(
                dealBounds(animatedVisibilityScope, GameDealType.TRUTH_OR_DARE)
            )
        )
        Spacer(Modifier.height(12.dp))
        with(animatedVisibilityScope) {
            DealHeroCard(
                accent = dareAccent,
                onClick = { onDealChosen(GameDealType.TRUTH_OR_DARE, TruthOrDareChoice.DARE) },
                modifier = Modifier.animateEnterExit(
                    enter = fadeIn(tween(PROMOTION_MS)) +
                            scaleIn(tween(PROMOTION_MS), initialScale = 0.92f),
                    exit = fadeOut(tween(PROMOTION_MS))
                )
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.dealBounds(
    animatedVisibilityScope: AnimatedVisibilityScope,
    dealType: GameDealType
): Modifier = Modifier.sharedBounds(
    rememberSharedContentState(key = "deal_$dealType"),
    animatedVisibilityScope = animatedVisibilityScope,
    boundsTransform = BoundsTransform { _, _ ->
        tween(durationMillis = PROMOTION_MS, easing = FastOutSlowInEasing)
    },
    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.DealCompactRow(
    animatedVisibilityScope: AnimatedVisibilityScope,
    dealTypes: List<GameDealType>,
    onDealPromoted: (GameDealType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier) {
        dealTypes.forEach { dealType ->
            DealCompactCard(
                accent = dealType.accent,
                onClick = { onDealPromoted(dealType) },
                modifier = Modifier
                    .weight(1f)
                    .then(dealBounds(animatedVisibilityScope, dealType))
            )
        }
    }
}

@Preview(name = "DealPicker – promoted", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun DealPickerPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().fillMaxSize().padding(24.dp)) {
            DealPicker(
                uiState = GameScreenState(
                    players = pickerPreviewPlayers,
                    heroDealType = GameDealType.TRUTH_OR_DARE,
                    promotedDealType = GameDealType.GENERAL_KNOWLEDGE
                ),
                onDealPromoted = {},
                onDealChosen = { _, _ -> }
            )
        }
    }
}

@Preview(name = "DealPicker – truth or dare", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun DealPickerDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().fillMaxSize().padding(24.dp)) {
            DealPicker(
                uiState = GameScreenState(
                    players = pickerPreviewPlayers,
                    heroDealType = GameDealType.TRUTH_OR_DARE
                ),
                onDealPromoted = {},
                onDealChosen = { _, _ -> }
            )
        }
    }
}
