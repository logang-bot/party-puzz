package com.restrusher.partypuzl.ui.common.ads.max

/** Exponential backoff for failed full-screen loads, as AppLovin recommends: 2, 4, 8 … 64 s. */
internal object MaxRetryDelay {
    private const val MAX_EXPONENT = 6

    fun millisFor(attempt: Int): Long = 1000L shl attempt.coerceIn(1, MAX_EXPONENT)
}
