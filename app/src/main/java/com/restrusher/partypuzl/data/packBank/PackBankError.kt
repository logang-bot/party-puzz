package com.restrusher.partypuzl.data.packBank

/** Why a pack bank download didn't produce usable content. */
enum class PackBankError {
    /** No route to the server: offline, DNS failure, refused connection or timeout. */
    NO_CONNECTION,

    /**
     * HTTP 429. GitHub Pages answers this when it throttles a site, which is the closest signal
     * there is to its soft bandwidth limit being exceeded.
     */
    RATE_LIMITED,

    /** Any other HTTP error, such as 404 when the file or the Pages site is gone, or 5xx. */
    SERVER_UNAVAILABLE,

    /** The server answered, but the file isn't a pack bank this app can read. */
    INVALID_CONTENT
}

/** What one [PackBankRepository.refresh] call did. */
sealed interface PackBankRefresh {
    data object Updated : PackBankRefresh
    data object Unchanged : PackBankRefresh

    /** The last successful check is recent enough that the network was not asked. */
    data object Skipped : PackBankRefresh
    data class Failed(val error: PackBankError) : PackBankRefresh
}
