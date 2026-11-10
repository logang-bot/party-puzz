package com.restrusher.partypuzl.data.billing

/** What happened to a purchase the user started, reported once so the app can tell them. */
sealed interface PurchaseOutcome {
    data object Purchased : PurchaseOutcome
    data object Pending : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data object Failed : PurchaseOutcome

    /** The product never loaded — no Play connection, or the app was not installed from Play. */
    data object Unavailable : PurchaseOutcome
}
