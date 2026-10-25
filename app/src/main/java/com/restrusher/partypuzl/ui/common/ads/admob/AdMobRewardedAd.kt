package com.restrusher.partypuzl.ui.common.ads.admob

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.restrusher.partypuzl.ui.common.ads.RewardedAdHandle

class AdMobRewardedAd(
    private val context: Context,
    private val adUnitId: String
) : RewardedAdHandle {
    private var rewardedAd by mutableStateOf<RewardedAd?>(null)

    override val isReady get() = rewardedAd != null

    private val loadCallback = object : RewardedAdLoadCallback() {
        override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
        override fun onAdFailedToLoad(error: LoadAdError) { rewardedAd = null }
    }

    override fun reload() {
        RewardedAd.load(context, adUnitId, AdRequest.Builder().build(), loadCallback)
    }

    override fun show(activity: Activity, onRewarded: () -> Unit) {
        rewardedAd?.let { ad ->
            ad.show(activity) { onRewarded() }
            rewardedAd = null
        }
    }
}
