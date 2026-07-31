package com.restrusher.partypuzl.data.models

/**
 * Which accent a pack is colour-coded with on the setup screen.
 *
 * A *name*, not a colour: the catalog says a pack is [ROSE], and `ui/theme` decides what rose
 * looks like. Keeping the enum here rather than an `androidx.compose.ui.graphics.Color` is what
 * stops the data layer depending on a UI framework — see `PackAccent.color` in `ui/theme`.
 */
enum class PackAccent {
    TEAL,
    PINK,
    CORAL,
    VIOLET,
    SKY,
    ROSE,
    LIME,
    YELLOW,
}

/**
 * The accent a pack inherits from how spicy it is — the only way any pack, built-in or authored,
 * gets a colour. See `SpiceLook.kt` in `ui/common` for the matching icons.
 */
val SpiceLevel.packAccent: PackAccent
    get() = when (this) {
        SpiceLevel.MILD -> PackAccent.TEAL
        SpiceLevel.MEDIUM -> PackAccent.VIOLET
        SpiceLevel.SPICY -> PackAccent.PINK
    }
