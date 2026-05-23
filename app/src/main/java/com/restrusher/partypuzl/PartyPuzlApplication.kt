package com.restrusher.partypuzl

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.restrusher.partypuzl.data.billing.BillingManager
import com.restrusher.partypuzl.ui.common.AppOpenAdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PartyPuzlApplication : Application() {

    @Inject
    lateinit var appOpenAdManager: AppOpenAdManager

    @Inject
    lateinit var billingManager: BillingManager

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        appOpenAdManager.loadAd()
        billingManager.connect()
    }
}