package com.restrusher.partypuzl.data.licenses

/**
 * One third-party library bundled in the app, with the licence terms it ships under.
 *
 * [licenseText] holds the full text of every licence the library declares, which is what the
 * Apache 2.0 licence (and most others) require a binary distribution to carry. When the build
 * could not resolve a text it falls back to the licence's URL.
 */
data class OpenSourceLibrary(
    val id: String,
    val name: String,
    val version: String?,
    val licenseNames: String,
    val licenseText: String
)
