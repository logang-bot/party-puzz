package com.restrusher.partypuzl.ui.common.purchase

import androidx.annotation.StringRes
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.billing.PurchaseOutcome

/** Null for a cancel: the user backed out themselves, so there is nothing to tell them. */
@get:StringRes
val PurchaseOutcome.messageRes: Int?
    get() = when (this) {
        PurchaseOutcome.Purchased -> R.string.purchase_outcome_purchased
        PurchaseOutcome.Pending -> R.string.purchase_outcome_pending
        PurchaseOutcome.Failed -> R.string.purchase_outcome_failed
        PurchaseOutcome.Unavailable -> R.string.unlock_purchase_unavailable
        PurchaseOutcome.Cancelled -> null
    }
