package com.restrusher.partypuzl

import android.app.Application
import com.restrusher.partypuzl.data.billing.BillingManager
import com.restrusher.partypuzl.ui.common.AppOpenAdManager
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.UnityAds
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
        UnityAds.initialize(
            this,
            BuildConfig.UNITY_GAME_ID,
            BuildConfig.USE_TEST_ADS,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    appOpenAdManager.loadAd()
                }
                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError,
                    message: String
                ) {}
            }
        )
        billingManager.connect()
    }
}