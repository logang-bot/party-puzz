package com.restrusher.partypuzl.ui.views.game.gameScreen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

internal const val PLAYER_REVEAL_MS = 420

/** Where the name and the photo start their landing, as a fraction of their settled size. */
internal const val LANDING_SCALE_FROM = 0.88f

/** The one spring the landing rides: the name and the photo arrive on the same beat. */
internal val landingSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

private const val PHOTO_KEY = "player_reveal_photo"
private const val NAME_KEY = "player_reveal_name"

/**
 * The two scopes a shared element needs to cross a deal phase change: the layout that owns the
 * overlay it is drawn in, and the transition it is travelling on. Null wherever the turn header is
 * drawn outside a phase transition, which leaves the photo and name unkeyed and simply static.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
internal data class PlayerRevealScopes(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope
)

@OptIn(ExperimentalSharedTransitionApi::class)
private val playerRevealBounds = BoundsTransform { _, _ ->
    tween(durationMillis = PLAYER_REVEAL_MS, easing = FastOutSlowInEasing)
}

/** Carries the picked player's photo from the centre of the turn intro into the turn header. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.playerPhotoReveal(scopes: PlayerRevealScopes?): Modifier =
    if (scopes == null) this else with(scopes.sharedTransitionScope) {
        this@playerPhotoReveal.sharedElement(
            rememberSharedContentState(key = PHOTO_KEY),
            animatedVisibilityScope = scopes.animatedVisibilityScope,
            boundsTransform = playerRevealBounds
        )
    }

/** The same trip for the name, which scales from display to headline size along the way. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.playerNameReveal(scopes: PlayerRevealScopes?): Modifier =
    if (scopes == null) this else with(scopes.sharedTransitionScope) {
        this@playerNameReveal.sharedBounds(
            rememberSharedContentState(key = NAME_KEY),
            animatedVisibilityScope = scopes.animatedVisibilityScope,
            boundsTransform = playerRevealBounds,
            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                contentScale = ContentScale.FillHeight,
                alignment = Alignment.Center
            )
        )
    }
