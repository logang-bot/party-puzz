package com.restrusher.partypuzl.data.billing

import com.android.billingclient.api.Purchase
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PurchaseEntitlementSyncTest {

    private val preferences = mockk<UserPreferencesRepository>(relaxed = true)
    private val sync = PurchaseEntitlementSync(preferences)

    @Test
    fun `an owned premium purchase grants premium`() = runTest {
        sync.syncOwned(listOf(purchase(Purchase.PurchaseState.PURCHASED)))
        coVerify { preferences.setAdFree(true) }
    }

    @Test
    fun `premium missing from owned purchases is revoked`() = runTest {
        sync.syncOwned(emptyList())
        coVerify { preferences.setAdFree(false) }
    }

    @Test
    fun `a pending purchase does not count as owned`() = runTest {
        sync.syncOwned(listOf(purchase(Purchase.PurchaseState.PENDING)))
        coVerify { preferences.setAdFree(false) }
    }

    @Test
    fun `another product does not grant premium`() = runTest {
        sync.syncOwned(listOf(purchase(Purchase.PurchaseState.PURCHASED, product = "other")))
        coVerify { preferences.setAdFree(false) }
    }

    @Test
    fun `a completed update grants premium and reports it`() = runTest {
        val outcome = sync.recordUpdate(listOf(purchase(Purchase.PurchaseState.PURCHASED)))
        assertEquals(PurchaseOutcome.Purchased, outcome)
        coVerify { preferences.setAdFree(true) }
    }

    @Test
    fun `a pending update is reported without granting`() = runTest {
        val outcome = sync.recordUpdate(listOf(purchase(Purchase.PurchaseState.PENDING)))
        assertEquals(PurchaseOutcome.Pending, outcome)
        coVerify(exactly = 0) { preferences.setAdFree(any()) }
    }

    @Test
    fun `an update with nothing usable is a failure that revokes nothing`() = runTest {
        assertEquals(PurchaseOutcome.Failed, sync.recordUpdate(emptyList()))
        coVerify(exactly = 0) { preferences.setAdFree(any()) }
    }

    @Test
    fun `only owned unacknowledged premium purchases need acknowledging`() {
        val unacknowledged = purchase(Purchase.PurchaseState.PURCHASED)
        val purchases = listOf(
            unacknowledged,
            purchase(Purchase.PurchaseState.PURCHASED, acknowledged = true),
            purchase(Purchase.PurchaseState.PENDING)
        )
        assertEquals(listOf(unacknowledged), sync.needingAcknowledgement(purchases))
    }

    private fun purchase(
        state: Int,
        product: String = PREMIUM_PRODUCT_ID,
        acknowledged: Boolean = false
    ): Purchase = mockk {
        every { products } returns listOf(product)
        every { purchaseState } returns state
        every { isAcknowledged } returns acknowledged
    }
}
