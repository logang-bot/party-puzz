package com.restrusher.partypuzl.ui.views.gameConfig

import com.restrusher.partypuzl.data.models.PackAccent
import com.restrusher.partypuzl.data.models.PackTier
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameConfigStateTest {

    @Test
    fun `start is blocked when every pack is switched off`() {
        val state = GameConfigState(
            officialPacks = listOf(pack(PackTier.OFFICIAL, enabled = false)),
            premiumPacks = listOf(pack(PackTier.PREMIUM, enabled = false)),
            customPacks = listOf(pack(PackTier.CUSTOM, enabled = false))
        )
        assertFalse(state.hasEnabledPack)
    }

    @Test
    fun `a lone enabled custom pack is enough to start`() {
        val state = GameConfigState(
            officialPacks = listOf(pack(PackTier.OFFICIAL, enabled = false)),
            customPacks = listOf(pack(PackTier.CUSTOM, enabled = true))
        )
        assertTrue(state.hasEnabledPack)
    }

    @Test
    fun `any enabled official or premium pack is enough to start`() {
        assertTrue(GameConfigState(officialPacks = listOf(pack(PackTier.OFFICIAL, true))).hasEnabledPack)
        assertTrue(GameConfigState(premiumPacks = listOf(pack(PackTier.PREMIUM, true))).hasEnabledPack)
    }

    private fun pack(tier: PackTier, enabled: Boolean) = PackUiModel(
        id = tier.name,
        tier = tier,
        name = PackLabel.Literal(tier.name),
        iconRes = 0,
        accent = PackAccent.TEAL,
        promptCount = 1,
        isEnabled = enabled,
        isUnlocked = true
    )
}
