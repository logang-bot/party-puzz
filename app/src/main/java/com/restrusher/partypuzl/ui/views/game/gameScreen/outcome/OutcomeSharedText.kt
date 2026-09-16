package com.restrusher.partypuzl.ui.views.game.gameScreen.outcome

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal const val OUTCOME_STAGE_MS = 420

private const val OUTCOME_TEXT_KEY = "outcome_text"

/**
 * Carries the landed line from the reel's centre window to the reveal's headline.
 *
 * Only the reel's settled row and the reveal's message claim this, one per stage, because a
 * shared-element key may be claimed once per layout.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.outcomeTextBounds(
    scope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
): Modifier = with(scope) {
    this@outcomeTextBounds.sharedBounds(
        rememberSharedContentState(key = OUTCOME_TEXT_KEY),
        animatedVisibilityScope = animatedVisibilityScope,
        boundsTransform = BoundsTransform { _, _ ->
            tween(OUTCOME_STAGE_MS, easing = FastOutSlowInEasing)
        },
        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
    )
}
