package com.restrusher.partypuzl.data.billing

import android.app.Activity
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * The app's view of Play Billing. Ownership itself is not exposed here — it lands in
 * `UserPreferencesRepository.isAdFree`, which every screen already observes.
 */
interface PremiumBilling {
    val outcomes: SharedFlow<PurchaseOutcome>

    /** Localised price from Play, or null until the product has loaded. */
    val formattedPrice: StateFlow<String?>

    fun connect()

    /** Re-reads owned purchases, so refunds relock and cleared pending payments unlock. */
    fun refreshEntitlement()

    /** Reports [PurchaseOutcome.Unavailable] through [outcomes] when the product isn't loaded. */
    fun launchPurchaseFlow(activity: Activity)
}
