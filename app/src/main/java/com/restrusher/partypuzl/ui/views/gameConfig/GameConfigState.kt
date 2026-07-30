package com.restrusher.partypuzl.ui.views.gameConfig

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.restrusher.partypuzl.data.models.PackAccent
import com.restrusher.partypuzl.data.models.PackTier

/**
 * A pack as the setup screen needs it — catalog metadata joined with the user's stored state
 * and any session unlock earned from a rewarded ad.
 */
data class PackUiModel(
    val id: String,
    val tier: PackTier,
    /** A resource for built-in packs, a literal for the ones the user wrote. */
    val name: PackLabel,
    @DrawableRes val iconRes: Int,
    val accent: PackAccent,
    val promptCount: Int,
    val isEnabled: Boolean,
    /** True if the pack is playable — free, purchased, or unlocked for this session. */
    val isUnlocked: Boolean,
    /** True only when the unlock came from a rewarded ad and dies with the process. */
    val isSessionUnlocked: Boolean = false
)

data class GameConfigState(
    val isLoading: Boolean = false,
    val officialPacks: List<PackUiModel> = emptyList(),
    val premiumPacks: List<PackUiModel> = emptyList(),
    val customPacks: List<PackUiModel> = emptyList(),
    /** Non-null while the unlock bottom sheet is open, holding the pack being unlocked. */
    val unlockTarget: PackUiModel? = null,
    /** One-shot message resource; cleared by [GameConfigViewModel.onMessageShown]. */
    @StringRes val messageRes: Int? = null
) {
    /**
     * The game needs something to draw from. Gates the Start button alongside the player count,
     * since every pack can now be switched off.
     *
     * Custom packs count: `QuestionPackContentLoader` pools their entries into the same deck, so a
     * lone enabled custom pack is a perfectly playable game. Leaving them out blocked Start on a
     * non-empty deck.
     */
    val hasEnabledPack: Boolean
        get() = officialPacks.any { it.isEnabled } ||
                premiumPacks.any { it.isEnabled } ||
                customPacks.any { it.isEnabled }
}
