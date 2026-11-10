package com.restrusher.partypuzl.data.billing

import com.android.billingclient.api.Purchase
import com.restrusher.partypuzl.data.preferences.UserPreferencesRepository
import javax.inject.Inject

/** Turns what Play reports about purchases into the stored premium flag. */
class PurchaseEntitlementSync @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    /**
     * [owned] is the complete list Play holds for this account, so a premium purchase missing
     * from it was refunded or revoked and the flag goes back to false.
     */
    suspend fun syncOwned(owned: List<Purchase>) {
        userPreferencesRepository.setAdFree(owned.any { it.isPremiumOwned })
    }

    /** Only grants premium. A missing purchase here means "not this update", not "refunded". */
    suspend fun recordUpdate(updated: List<Purchase>): PurchaseOutcome {
        val outcome = outcomeOf(updated)
        if (outcome == PurchaseOutcome.Purchased) userPreferencesRepository.setAdFree(true)
        return outcome
    }

    fun needingAcknowledgement(purchases: List<Purchase>): List<Purchase> =
        purchases.filter { it.isPremiumOwned && !it.isAcknowledged }

    private fun outcomeOf(updated: List<Purchase>): PurchaseOutcome = when {
        updated.any { it.isPremiumOwned } -> PurchaseOutcome.Purchased
        updated.any { it.isPremiumPending } -> PurchaseOutcome.Pending
        else -> PurchaseOutcome.Failed
    }
}
