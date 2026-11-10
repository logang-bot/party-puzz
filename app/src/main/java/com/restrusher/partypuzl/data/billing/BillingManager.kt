package com.restrusher.partypuzl.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext context: Context,
    private val entitlementSync: PurchaseEntitlementSync
) : PremiumBilling, PurchasesUpdatedListener {

    private val _outcomes = MutableSharedFlow<PurchaseOutcome>(extraBufferCapacity = OUTCOME_BUFFER)
    override val outcomes: SharedFlow<PurchaseOutcome> = _outcomes.asSharedFlow()

    private val _formattedPrice = MutableStateFlow<String?>(null)
    override val formattedPrice: StateFlow<String?> = _formattedPrice.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var productDetails: ProductDetails? = null

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enableAutoServiceReconnection()
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    override fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode != BillingResponseCode.OK) return
                queryProductDetails()
                refreshEntitlement()
            }

            override fun onBillingServiceDisconnected() = Unit
        })
    }

    override fun refreshEntitlement() {
        billingClient.queryPurchasesAsync(OWNED_PURCHASES_QUERY) { result, owned ->
            if (result.responseCode == BillingResponseCode.OK) scope.launch { syncOwned(owned) }
        }
    }

    override fun launchPurchaseFlow(activity: Activity) {
        val details = productDetails ?: return report(PurchaseOutcome.Unavailable)
        billingClient.launchBillingFlow(activity, flowParamsFor(details))
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingResponseCode.OK -> scope.launch { recordUpdate(purchases.orEmpty()) }
            BillingResponseCode.USER_CANCELED -> report(PurchaseOutcome.Cancelled)
            BillingResponseCode.ITEM_ALREADY_OWNED -> refreshEntitlement()
            else -> report(PurchaseOutcome.Failed)
        }
    }

    private fun queryProductDetails() {
        billingClient.queryProductDetailsAsync(PRODUCT_DETAILS_QUERY) { _, result ->
            val details = result.productDetailsList.firstOrNull()
            productDetails = details
            _formattedPrice.value = details?.defaultOffer()?.formattedPrice
        }
    }

    private suspend fun syncOwned(owned: List<Purchase>) {
        entitlementSync.syncOwned(owned)
        acknowledge(owned)
    }

    private suspend fun recordUpdate(updated: List<Purchase>) {
        report(entitlementSync.recordUpdate(updated))
        acknowledge(updated)
    }

    private fun report(outcome: PurchaseOutcome) {
        _outcomes.tryEmit(outcome)
    }

    private fun acknowledge(purchases: List<Purchase>) {
        entitlementSync.needingAcknowledgement(purchases).forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) {}
        }
    }

    private fun flowParamsFor(details: ProductDetails): BillingFlowParams {
        val product = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details)
        details.defaultOffer()?.offerToken?.let(product::setOfferToken)
        return BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(product.build()))
            .build()
    }

    private fun ProductDetails.defaultOffer() = oneTimePurchaseOfferDetailsList?.firstOrNull()

    private companion object {
        const val OUTCOME_BUFFER = 4

        val OWNED_PURCHASES_QUERY: QueryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val PRODUCT_DETAILS_QUERY: QueryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PREMIUM_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
    }
}
