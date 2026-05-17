package com.restrusher.partypuzz

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.restrusher.partypuzz.ui.common.AppOpenAdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PartyPuzzApplication : Application() {

    @Inject
    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        appOpenAdManager.loadAd()
    }
}