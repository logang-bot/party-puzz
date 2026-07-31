package com.restrusher.partypuzl.ui.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.models.SpiceLevel
import com.restrusher.partypuzl.data.models.packAccent
import com.restrusher.partypuzl.data.preferences.ThemeMode
import com.restrusher.partypuzl.ui.theme.Ink
import com.restrusher.partypuzl.ui.theme.PartyPuzlTheme
import com.restrusher.partypuzl.ui.theme.appBackground
import com.restrusher.partypuzl.ui.theme.color
import com.restrusher.partypuzl.ui.theme.ink

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

/**
 * The `ic_spice_*` set is **dedicated to spice** and referenced from nowhere else in the app, which
 * is the point of it. The levels used to borrow `ic_lightbulb`, `ic_random` and `ic_whatshot` —
 * glyphs that also mean Truth, "randomise this player" and Bar punishment — so retuning the spice
 * look was impossible without dragging four unrelated screens along with it. These three can be
 * redrawn freely.
 *
 * They carry **no colour of their own**: the drawables are white strokes on nothing, and the
 * `Icon(tint = …)` at the call site is the single colour authority. Baking the accent into the
 * file would look correct only for as long as every render site happens to tint.
 */
@get:DrawableRes
val SpiceLevel.iconRes: Int
    get() = when (this) {
        SpiceLevel.MILD -> R.drawable.ic_spice_mild_brain
        SpiceLevel.MEDIUM -> R.drawable.ic_spice_medium_sparkle
        SpiceLevel.SPICY -> R.drawable.ic_spice_spicy_flame
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

/**
 * The three icons at the three sizes they are actually drawn at — 15 dp in the create screen's
 * spice selector, 18 dp in a setup-screen pack row, 25 dp in the pack editor header. The 15 dp
 * column is the one that matters: thin strokes are what break down there.
 */
@Composable
private fun SpiceIconsSample() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpiceLevel.entries.forEach { level ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                listOf(15, 18, 25).forEach { size ->
                    Icon(
                        painter = painterResource(level.iconRes),
                        contentDescription = null,
                        tint = level.accent,
                        modifier = Modifier.size(size.dp)
                    )
                }
                Text(
                    text = stringResource(level.labelRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.ink(Ink.Secondary),
                    modifier = Modifier.width(80.dp)
                )
            }
        }
    }
}

@Preview(name = "SpiceIcons – Light", showBackground = true, widthDp = 360)
@Composable
private fun SpiceIconsLightPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.appBackground().padding(16.dp)) { SpiceIconsSample() }
    }
}

@Preview(name = "SpiceIcons – Dark", showBackground = true, widthDp = 360)
@Composable
private fun SpiceIconsDarkPreview() {
    PartyPuzlTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.appBackground().padding(16.dp)) { SpiceIconsSample() }
    }
}
