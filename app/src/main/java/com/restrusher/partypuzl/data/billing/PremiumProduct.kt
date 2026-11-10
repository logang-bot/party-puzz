package com.restrusher.partypuzl.data.billing

import com.android.billingclient.api.Purchase

/**
 * The one-time product that unlocks premium: every premium pack and unlimited custom packs.
 *
 * The id still reads `remove_ads` because it was created in Play Console under that name, and Play
 * product ids can never be renamed or reused. The name and description users see live in the
 * console, so the id itself is never shown.
 */
const val PREMIUM_PRODUCT_ID = "remove_ads"

internal val Purchase.isPremiumOwned: Boolean
    get() = PREMIUM_PRODUCT_ID in products && purchaseState == Purchase.PurchaseState.PURCHASED

internal val Purchase.isPremiumPending: Boolean
    get() = PREMIUM_PRODUCT_ID in products && purchaseState == Purchase.PurchaseState.PENDING
