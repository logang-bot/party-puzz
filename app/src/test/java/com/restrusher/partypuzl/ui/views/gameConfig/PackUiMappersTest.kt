package com.restrusher.partypuzl.ui.views.gameConfig

import com.restrusher.partypuzl.data.local.appData.appDataSource.QuestionPackCatalog
import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackUiMappersTest {

    private val spicy = QuestionPackCatalog.PREMIUM_SPICY
    private val owned = PremiumAccess(sessionUnlocks = emptySet(), isAdFree = true)

    @Test
    fun `a premium pack takes its count from the downloaded content`() {
        val model = premiumModel(promptCounts = mapOf(spicy.id to 20))
        assertEquals(20, model.promptCount)
        assertFalse(model.needsDownload)
    }

    @Test
    fun `a premium pack with nothing downloaded needs a download`() {
        assertTrue(premiumModel(promptCounts = emptyMap()).needsDownload)
    }

    @Test
    fun `an official pack never needs a download`() {
        val definition = QuestionPackCatalog.all.first {
            it.tier == PackTier.OFFICIAL && it.category != PackCategory.MINI_GAME
        }
        val entity = QuestionPackEntity(definition.id, definition.tier, definition.category, true, true)
        val model = listOf(entity).toCatalogUiModels(owned, emptyMap()).single()
        assertFalse(model.needsDownload)
    }

    private fun premiumModel(promptCounts: Map<String, Int>): PackUiModel {
        val entity = QuestionPackEntity(spicy.id, spicy.tier, spicy.category, isEnabled = true, isUnlocked = true)
        return listOf(entity).toCatalogUiModels(owned, promptCounts).single()
    }
}
