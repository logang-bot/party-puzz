package com.restrusher.partypuzl.ui.views.customPacks.entry

import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.ui.views.customPacks.model.EntryDeal
import com.restrusher.partypuzl.ui.views.customPacks.model.deal
import com.restrusher.partypuzl.ui.views.customPacks.model.defaultType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateCustomEntryStateTest {

    @Test
    fun `blank text can never be saved`() {
        CustomEntryType.entries.forEach { type ->
            val state = CreateCustomEntryState(type = type, text = "   ", optionA = "a", optionB = "b")
            assertFalse("$type saved with blank text", state.canSave)
        }
    }

    @Test
    fun `trivia needs both answers`() {
        val trivia = CreateCustomEntryState(type = CustomEntryType.TRIVIA, text = "2 + 2?")
        assertFalse(trivia.copy(optionA = "4").canSave)
        assertFalse(trivia.copy(optionB = "5").canSave)
        assertTrue(trivia.copy(optionA = "4", optionB = "5").canSave)
    }

    @Test
    fun `truths dares and sticky dares need only their text`() {
        listOf(CustomEntryType.TRUTH, CustomEntryType.DARE, CustomEntryType.STICKY_DARE).forEach { type ->
            assertTrue(CreateCustomEntryState(type = type, text = "something").canSave)
        }
    }

    @Test
    fun `truths and dares sit under the same deal card`() {
        assertEquals(EntryDeal.TRUTH_OR_DARE, CustomEntryType.TRUTH.deal)
        assertEquals(EntryDeal.TRUTH_OR_DARE, CustomEntryType.DARE.deal)
    }

    @Test
    fun `every deal's default type maps back to that deal`() {
        EntryDeal.entries.forEach { deal -> assertEquals(deal, deal.defaultType.deal) }
    }
}
