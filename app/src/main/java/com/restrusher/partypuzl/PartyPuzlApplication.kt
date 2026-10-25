package com.restrusher.partypuzl

import android.app.Application
import com.restrusher.partypuzl.data.billing.BillingManager
import com.restrusher.partypuzl.ui.common.ads.AdsProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PartyPuzlApplication : Application() {

    @Inject
    lateinit var adsProvider: AdsProvider

    @Inject
    lateinit var billingManager: BillingManager

    override fun onCreate() {
        super.onCreate()
        adsProvider.initialize()
        billingManager.connect()
    }
}