package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.restrusher.partypuzl.data.models.Player
import kotlinx.coroutines.delay

private const val CYCLE_STEPS = 12
private const val SWAP_MS = 180
private const val FAST_STEP_MS = 90
private const val SLOW_STEP_MS = 420
private const val LANDING_LETTER_SPACING_SP = 6f

/**
 * Names rising through the centre, one swapping for the next, the last of them landing.
 *
 * Keyed on the step rather than the name: in a small party the same name comes round again, and
 * [AnimatedContent] would treat a repeat as no change at all and simply skip the swap. The landing
 * is the run's final step rather than a separate composable, so the name it replaces still slides
 * out the way every other one did instead of vanishing under it.
 */
@Composable
internal fun CyclingName(
    path: NameCyclePath,
    step: Int,
    scopes: PlayerRevealScopes?,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = step,
        transitionSpec = {
            (slideInVertically(tween(SWAP_MS)) { it / 2 } + fadeIn(tween(SWAP_MS)))
                .togetherWith(slideOutVertically(tween(SWAP_MS)) { -it / 2 } + fadeOut(tween(SWAP_MS)))
        },
        label = "cycling name",
        modifier = modifier
    ) { current ->
        if (current == path.lastStep) LandedName(name = path.picked, scopes = scopes)
        else NameText(text = path.nameAt(current), modifier = Modifier.fillMaxWidth())
    }
}

/**
 * The picked name arriving. One spring drives both halves of the landing: an overshooting scale,
 * and letter spacing closing from [LANDING_LETTER_SPACING_SP] to nothing, so the name reads as
 * snapping into focus rather than merely appearing.
 */
@Composable
private fun LandedName(name: String, scopes: PlayerRevealScopes?, modifier: Modifier = Modifier) {
    val settle = remember { Animatable(0f) }
    LaunchedEffect(name) { settle.animateTo(1f, landingSpring) }

    NameText(
        text = name,
        letterSpacing = (LANDING_LETTER_SPACING_SP * (1f - settle.value)).sp,
        modifier = modifier
            .playerNameReveal(scopes)
            .graphicsLayer {
                scaleX = LANDING_SCALE_FROM + (1f - LANDING_SCALE_FROM) * settle.value
                scaleY = scaleX
            }
    )
}

@Composable
private fun NameText(
    text: String,
    modifier: Modifier = Modifier,
    letterSpacing: TextUnit = TextUnit.Unspecified
) {
    Text(
        text = text,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        letterSpacing = letterSpacing,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

internal suspend fun cycleNames(path: NameCyclePath, onStep: (Int) -> Unit) {
    val run = DeceleratingRun(path.steps.size, FAST_STEP_MS, SLOW_STEP_MS)
    repeat(path.steps.size) { step ->
        onStep(step)
        if (step < path.lastStep) delay(run.stepMillis(step))
    }
}

/**
 * The names the centre flicks through, ending on the picked player.
 *
 * The run is a fixed [CYCLE_STEPS] long however many players there are and wherever the pick falls
 * among them, so every turn's intro takes exactly as long as the last one's. The lead-up is drawn
 * from the *other* players only: the picked name appearing mid-run would either give the answer
 * away or, landing next to itself, produce a swap with nothing to swap.
 */
internal data class NameCyclePath(val steps: List<String>) {
    val lastStep: Int get() = steps.size - 1

    val picked: String get() = steps.last()

    fun nameAt(step: Int): String = steps[step]

    companion object {
        fun of(players: List<Player>, picked: Player): NameCyclePath {
            val others = players.map { it.nickName } - picked.nickName
            if (others.isEmpty()) return NameCyclePath(listOf(picked.nickName))
            val lead = generateSequence { others.shuffled() }.flatten().take(CYCLE_STEPS - 1)
            return NameCyclePath(lead.toList() + picked.nickName)
        }
    }
}
