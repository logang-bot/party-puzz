package com.restrusher.partypuzl.ui.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.data.models.packAccent
import com.restrusher.partypuzl.ui.theme.color

/**
 * How a pack looks, for every pack there is.
 *
 * The three spice levels are the whole vocabulary: a built-in pack declares a [SpiceLevel] in
 * `QuestionPackCatalog` and an authored one is given a level on the create screen, and both
 * resolve their icon and accent through here. That is what keeps the setup screen showing only
 * looks a user could have produced themselves — a pack cannot wear an icon the create screen does
 * not offer, because there is nowhere to declare one.
 *
 * Lives in `ui/common` rather than beside either feature because both of them read it, and
 * because the data layer must not depend on Compose — `SpiceLevel` stays a plain enum and gets
 * its looks attached here, at the UI boundary.
 */

@get:DrawableRes
val SpiceLevel.iconRes: Int
    get() = when (this) {
        SpiceLevel.MILD -> R.drawable.ic_lightbulb
        SpiceLevel.MEDIUM -> R.drawable.ic_random
        SpiceLevel.SPICY -> R.drawable.ic_whatshot
    }

val SpiceLevel.accent: Color
    get() = packAccent.color

@get:StringRes
val SpiceLevel.labelRes: Int
    get() = when (this) {
        SpiceLevel.MILD -> R.string.spice_mild
        SpiceLevel.MEDIUM -> R.string.spice_medium
        SpiceLevel.SPICY -> R.string.spice_spicy
    }
