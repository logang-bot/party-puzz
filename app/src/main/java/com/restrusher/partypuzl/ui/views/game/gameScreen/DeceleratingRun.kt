package com.restrusher.partypuzl.ui.views.game.gameScreen

import kotlin.math.pow

private const val RAMP_EXPONENT = 3f

/**
 * A run of discrete steps that holds each of the early ones briefly and stretches only the last
 * two or three, so the run reads as slowing *onto* its final step rather than easing uniformly
 * across all of them.
 *
 * The cube of the progress fraction is what back-loads it: at the halfway point a step still runs
 * at an eighth of the way between [fastStepMs] and [slowStepMs].
 */
internal data class DeceleratingRun(
    val stepCount: Int,
    val fastStepMs: Int,
    val slowStepMs: Int
) {
    fun stepMillis(step: Int): Long {
        val progress = (step / (stepCount - 1).coerceAtLeast(1).toFloat()).pow(RAMP_EXPONENT)
        return (fastStepMs + (slowStepMs - fastStepMs) * progress).toLong()
    }
}
