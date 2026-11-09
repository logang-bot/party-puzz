package com.restrusher.partypuzl.ui.common.ads.levelplay

/** Exponential backoff for failed full-screen loads: 2, 4, 8 … 64 s. */
internal object LevelPlayRetryDelay {
    private const val MAX_EXPONENT = 6

    fun millisFor(attempt: Int): Long = 1000L shl attempt.coerceIn(1, MAX_EXPONENT)
}
