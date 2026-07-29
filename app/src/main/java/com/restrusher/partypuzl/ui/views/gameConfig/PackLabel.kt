package com.restrusher.partypuzl.ui.views.gameConfig

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * A pack's display name.
 *
 * Built-in packs name themselves with a string resource so they translate; a custom pack's name is
 * whatever the user typed and has no resource behind it. `PackUiModel` used to hold a bare
 * `@StringRes Int`, which made a user-typed name impossible to represent — this is the seam that
 * lets both kinds share one row.
 */
sealed interface PackLabel {
    data class Resource(@StringRes val id: Int) : PackLabel
    data class Literal(val value: String) : PackLabel
}

@Composable
fun PackLabel.text(): String = when (this) {
    is PackLabel.Resource -> stringResource(id)
    is PackLabel.Literal -> value
}
