package com.restrusher.partypuzl.ui.common.purchase

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.restrusher.partypuzl.data.billing.PremiumBilling
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Backs every "buy premium" button. Results are not handled here — `MainActivity` reports every
 * [com.restrusher.partypuzl.data.billing.PurchaseOutcome], and the unlock itself arrives through
 * `isAdFree`.
 */
@HiltViewModel
class PremiumPurchaseViewModel @Inject constructor(
    private val premiumBilling: PremiumBilling
) : ViewModel() {

    val formattedPrice: StateFlow<String?> = premiumBilling.formattedPrice

    fun purchase(activity: Activity) = premiumBilling.launchPurchaseFlow(activity)
}
