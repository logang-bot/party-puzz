package com.restrusher.partypuzl.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// Brand ramp — theme invariant.
//
// These are the raw palette entries from the PartyPuzz design system. They read
// the same in light and dark because they are only ever used as *fills*: mode
// cards, deal cards and outcome medallions are colored gradients with white ink
// on top in both themes. Anything that needs to sit legibly on the page
// background instead uses `AppColors.brandAccent`, which does flip.
// ─────────────────────────────────────────────────────────────

val BrandTeal = Color(0xFF2EB6C6)       // --brand
val BrandTealSoft = Color(0xFF9FDCE4)   // --brand-soft, the dark-theme accent
val BrandTealDeep = Color(0xFF1C7A87)   // --brand-deep
val BrandTealLight = Color(0xFF0F5B66)  // light-theme --brand-deep, the light accent
val BrandTealShade = Color(0xFF1C4F5C)  // gradient end stop
val BrandInk = Color(0xFF062028)        // ink that sits on top of a brand fill

val AccentPink = Color(0xFFFF5B8A)
val AccentCoral = Color(0xFFFF8A5C)
val AccentYellow = Color(0xFFFFD25A)
val AccentLime = Color(0xFFA8E063)
val AccentViolet = Color(0xFF8B6CFF)
val AccentSky = Color(0xFF5EC1FF)
val AccentRose = Color(0xFFC23368)

val DangerRed = Color(0xFFFF6B6B)       // --danger
val SuccessGreen = Color(0xFF4ADE80)    // --success

/** Fallback mode gradient, for a game mode we have no palette for. */
val ModeFallbackStart = Color(0xFF2A4060)
val ModeFallbackEnd = Color(0xFF162840)

/** Reward / punishment medallions. Only used by the Couples and Bar outcome decks. */
val OutcomePunishRoseDeep = Color(0xFF7A2140)
val OutcomePunishPlum = Color(0xFF3A1030)
val OutcomePunishCrimson = Color(0xFFFF2E63)
val OutcomePunishMidnight = Color(0xFF1A0B2E)

/**
 * Mini-game accent cycle. Shared by Follow the Spot, Hot Potato and Tap War,
 * which each used to carry their own identical copy of this list.
 */
val MiniGameAccents = listOf(
    Color(0xFFFF80AB),
    Color(0xFF80D8FF),
    Color(0xFFCCFF90),
    Color(0xFFFFFF8D)
)

/** Simon Says pad colors. Deliberately their own palette — the pads read as a toy, not as brand. */
val SimonGreen = Color(0xFF4CAF50)
val SimonRed = Color(0xFFF44336)
val SimonBlue = Color(0xFF2196F3)
val SimonYellow = Color(0xFFFFEB3B)

/** Quiz answer feedback. Deeper than the accent ramp so white label text stays legible. */
val AnswerCorrectGreen = Color(0xFF2E7D32)
val AnswerWrongRed = Color(0xFFC62828)

// ─────────────────────────────────────────────────────────────
// App background gradients
//
// Light is a straight top-to-bottom cream→peach ramp. Dark is a radial glow
// anchored at the top center, matching the design's
// `radial-gradient(120% 60% at 50% 0%, …)`.
// ─────────────────────────────────────────────────────────────

val backgroundGradientDarkTop = Color(0xFF18424A)
val backgroundGradientDarkMid = Color(0xFF0F2A30)
val backgroundGradientDarkEnd = Color(0xFF0B1F24)

val backgroundGradientLightStart = Color(0xFFFFF5E6)
val backgroundGradientLightMid = Color(0xFFFFE6D6)
val backgroundGradientLightEnd = Color(0xFFFFD9C2)

// ─────────────────────────────────────────────────────────────
// Light scheme — warm cream surfaces, cool teal-black ink.
//
// The ink ramp is deliberately cool (--ink #0E2630 through --ink-4) against the
// warm background. Matching the ink to the background's warmth is what made the
// previous olive-seeded scheme read as muddy.
// ─────────────────────────────────────────────────────────────

val primaryLight = BrandTeal
val onPrimaryLight = BrandInk
val primaryContainerLight = Color(0xFFD3F1F5)
val onPrimaryContainerLight = Color(0xFF06333B)
val secondaryLight = Color(0xFF6A4FD0)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFE7E0FF)
val onSecondaryContainerLight = Color(0xFF23105E)
val tertiaryLight = Color(0xFF3F6B21)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFC6EFA0)
val onTertiaryContainerLight = Color(0xFF0E2600)
val errorLight = Color(0xFFC22B2B)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF410002)
val backgroundLight = Color(0xFFFFF5E6)
val onBackgroundLight = Color(0xFF0E2630)          // --ink
val surfaceLight = Color(0xFFFFF5E6)
val onSurfaceLight = Color(0xFF0E2630)
val surfaceVariantLight = Color(0xFFEDE3D6)
val onSurfaceVariantLight = Color(0xFF324851)      // --ink-2
val outlineLight = Color(0xFF6A7D85)               // --ink-3
val outlineVariantLight = Color(0xFFD7D0C4)        // ≈ --line-strong over cream
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF0E2630)
val inverseOnSurfaceLight = Color(0xFFF1F7F8)
val inversePrimaryLight = BrandTealSoft
val surfaceDimLight = Color(0xFFE8DCCB)
val surfaceBrightLight = Color(0xFFFFFDF9)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)   // --bg-1, the card surface
val surfaceContainerLowLight = Color(0xFFFDF8F1)
val surfaceContainerLight = Color(0xFFFAF3E9)
val surfaceContainerHighLight = Color(0xFFF3EBE0)     // --bg-3
val surfaceContainerHighestLight = Color(0xFFEDE3D6)

// ─────────────────────────────────────────────────────────────
// Dark scheme — the design's teal surface ramp (--bg-0 through --bg-3),
// replacing the neutral gray-blue the Material Theme Builder produced.
// ─────────────────────────────────────────────────────────────

val primaryDark = BrandTeal
val onPrimaryDark = BrandInk
val primaryContainerDark = Color(0xFF1B3D44)
val onPrimaryContainerDark = BrandTealSoft
val secondaryDark = Color(0xFFC9BAFF)
val onSecondaryDark = Color(0xFF2B1B66)
val secondaryContainerDark = Color(0xFF3C2C82)
val onSecondaryContainerDark = Color(0xFFE7E0FF)
val tertiaryDark = AccentLime
val onTertiaryDark = Color(0xFF1B3300)
val tertiaryContainerDark = Color(0xFF2E4A12)
val onTertiaryContainerDark = Color(0xFFC6EFA0)
val errorDark = DangerRed
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF0B1F24)             // --bg-0
val onBackgroundDark = Color(0xFFF1F7F8)           // --ink
val surfaceDark = Color(0xFF0B1F24)
val onSurfaceDark = Color(0xFFF1F7F8)
val surfaceVariantDark = Color(0xFF163840)
val onSurfaceVariantDark = Color(0xFFC5D3D6)       // --ink-2
val outlineDark = Color(0xFF8AA0A5)                // --ink-3
val outlineVariantDark = Color(0xFF324247)         // ≈ --line-strong over --bg-0
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFF1F7F8)
val inverseOnSurfaceDark = Color(0xFF0E2630)
val inversePrimaryDark = BrandTealLight
val surfaceDimDark = Color(0xFF071A1E)
val surfaceBrightDark = Color(0xFF2A5158)
val surfaceContainerLowestDark = Color(0xFF071A1E)
val surfaceContainerLowDark = Color(0xFF0F2A30)    // --bg-1
val surfaceContainerDark = Color(0xFF163840)       // --bg-2
val surfaceContainerHighDark = Color(0xFF1F4951)   // --bg-3
val surfaceContainerHighestDark = Color(0xFF275A63)
