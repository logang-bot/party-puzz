package com.restrusher.partypuzl.ui.views.customPacks.entry

import com.restrusher.partypuzl.data.local.entities.CustomEntryEntity
import com.restrusher.partypuzl.data.models.CustomEntryDraft
import com.restrusher.partypuzl.data.models.CustomEntryType
import com.restrusher.partypuzl.data.models.DEFAULT_STICKY_DURATION_SECONDS
import com.restrusher.partypuzl.data.models.ENTRY_TEXT_MAX
import com.restrusher.partypuzl.data.models.TRIVIA_TEXT_MAX
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import com.restrusher.partypuzl.testutil.MainDispatcherRule
import com.restrusher.partypuzl.ui.views.customPacks.model.EntryDeal
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class CreateCustomEntryViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = mockk<CustomPackRepository>(relaxed = true)
    private val viewModel = CreateCustomEntryViewModel(repository)
    private val state get() = viewModel.uiState.value

    @Test
    fun `entry text is capped at the authoring limit`() {
        viewModel.onTextChange("x".repeat(ENTRY_TEXT_MAX + 20))
        assertEquals(ENTRY_TEXT_MAX, state.text.length)
    }

    @Test
    fun `trivia questions have a tighter cap`() {
        viewModel.onDealChange(EntryDeal.TRIVIA)
        viewModel.onTextChange("x".repeat(ENTRY_TEXT_MAX))
        assertEquals(TRIVIA_TEXT_MAX, state.text.length)
    }

    @Test
    fun `re-picking the current deal keeps a dare a dare`() {
        viewModel.onKindChange(CustomEntryType.DARE)
        viewModel.onDealChange(EntryDeal.TRUTH_OR_DARE)
        assertEquals(CustomEntryType.DARE, state.type)
    }

    @Test
    fun `picking another deal switches to its default type`() {
        viewModel.onDealChange(EntryDeal.STICKY_DARE)
        assertEquals(CustomEntryType.STICKY_DARE, state.type)
    }

    @Test
    fun `an entry that cannot be saved never reaches the repository`() = runTest(mainRule.dispatcher) {
        viewModel.onSave {}
        advanceUntilIdle()
        coVerify(exactly = 0) { repository.saveEntry(any()) }
    }

    @Test
    fun `a saved truth is trimmed and carries no sticky or trivia fields`() = runTest(mainRule.dispatcher) {
        viewModel.onTextChange("  a truth  ")
        viewModel.onOptionAChange("leftover")
        val draft = savedDraft()
        assertEquals("a truth", draft.text)
        assertNull(draft.durationSeconds)
        assertNull(draft.optionA)
        assertNull(draft.correctOption)
    }

    @Test
    fun `a saved sticky dare keeps its duration`() = runTest(mainRule.dispatcher) {
        viewModel.onDealChange(EntryDeal.STICKY_DARE)
        viewModel.onTextChange("hop")
        viewModel.onDurationChange(600)
        assertEquals(600, savedDraft().durationSeconds)
    }

    @Test
    fun `a saved trivia entry keeps its trimmed answers`() = runTest(mainRule.dispatcher) {
        viewModel.onDealChange(EntryDeal.TRIVIA)
        viewModel.onTextChange("2 + 2?")
        viewModel.onOptionAChange(" 4 ")
        viewModel.onOptionBChange("5")
        viewModel.onCorrectOptionChange('A')
        val draft = savedDraft()
        assertEquals(listOf("4", "5"), listOf(draft.optionA, draft.optionB))
        assertEquals('A', draft.correctOption)
    }

    @Test
    fun `loading an existing entry fills the form`() = runTest(mainRule.dispatcher) {
        coEvery { repository.getEntry("e1") } returns stickyEntry()
        viewModel.load("pack", "e1")
        advanceUntilIdle()
        assertEquals(CustomEntryType.STICKY_DARE, state.type)
        assertEquals("spin", state.text)
        assertEquals(1800, state.durationSeconds)
        assertEquals("e1", state.entryId)
    }

    @Test
    fun `a new entry starts as a truth with the default duration`() = runTest(mainRule.dispatcher) {
        viewModel.load("pack", null)
        advanceUntilIdle()
        assertEquals(CustomEntryType.TRUTH, state.type)
        assertEquals(DEFAULT_STICKY_DURATION_SECONDS, state.durationSeconds)
        assertEquals("pack", state.packId)
    }

    @Test
    fun `loading twice reads the repository once`() = runTest(mainRule.dispatcher) {
        viewModel.load("pack", null)
        viewModel.load("other", null)
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.getSummaryOnce(any()) }
        assertEquals("pack", state.packId)
    }

    private fun TestScope.savedDraft(): CustomEntryDraft {
        val draft = slot<CustomEntryDraft>()
        coEvery { repository.saveEntry(capture(draft)) } returns Unit
        viewModel.onSave {}
        advanceUntilIdle()
        return draft.captured
    }

    private fun stickyEntry() =
        CustomEntryEntity("e1", "pack", CustomEntryType.STICKY_DARE, "spin", 1800, null, null, null)
}
