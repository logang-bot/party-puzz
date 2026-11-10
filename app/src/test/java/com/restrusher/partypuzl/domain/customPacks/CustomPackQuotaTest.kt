package com.restrusher.partypuzl.domain.customPacks

import com.restrusher.partypuzl.data.local.dao.CustomPackSummary
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import com.restrusher.partypuzl.data.repositories.interfaces.CustomPackRepository
import com.restrusher.partypuzl.domain.customPacks.CustomPackQuota.Companion.MAX_FREE_ENTRIES
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomPackQuotaTest {

    @Test
    fun `a free user with no packs can create one`() {
        assertTrue(CustomPackQuota(isPremium = false, packCount = 0).canCreatePack)
    }

    @Test
    fun `a free user at the pack cap cannot create another`() {
        assertFalse(CustomPackQuota(isPremium = false, packCount = 1).canCreatePack)
    }

    @Test
    fun `a free user already over the pack cap is still blocked, not broken`() {
        assertFalse(CustomPackQuota(isPremium = false, packCount = 3).canCreatePack)
    }

    @Test
    fun `a free pack accepts entries up to the cap and no further`() {
        val quota = CustomPackQuota(isPremium = false, packCount = 1)
        assertTrue(quota.canAddEntry(MAX_FREE_ENTRIES - 1))
        assertFalse(quota.canAddEntry(MAX_FREE_ENTRIES))
    }

    @Test
    fun `premium has no caps`() {
        val quota = CustomPackQuota(isPremium = true, packCount = 40)
        assertTrue(quota.canCreatePack)
        assertTrue(quota.canAddEntry(500))
    }

    @Test
    fun `the use case counts every pack and reads the premium flag`() = runTest {
        val preferences = mockk<UserPreferencesRepository> { every { isAdFree } returns flowOf(false) }
        val packs = List(2) { mockk<CustomPackSummary>() }
        val repository = mockk<CustomPackRepository> { every { getSummaries() } returns flowOf(packs) }

        val quota = ObserveCustomPackQuotaUseCase(preferences, repository)().first()

        assertEquals(CustomPackQuota(isPremium = false, packCount = 2), quota)
    }
}
