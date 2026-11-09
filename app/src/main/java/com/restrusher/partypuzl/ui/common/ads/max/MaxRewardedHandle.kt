package com.restrusher.partypuzl.ui.common.ads.max

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import com.restrusher.partypuzl.ui.common.ads.RewardedAdHandle

/**
 * [RewardedAdHandle] around MAX's per-ad-unit shared [MaxRewardedAd]. That instance outlives this
 * handle, so [detach] only drops the listener instead of destroying the ad.
 */
class MaxRewardedHandle(adUnitId: String) : RewardedAdHandle {
    private var isLoaded by mutableStateOf(false)

    override val isReady get() = isLoaded

    private val rewardedAd = MaxRewardedAd.getInstance(adUnitId)
    private var onRewarded: () -> Unit = {}

    private val listener = object : MaxRewardedAdListener {
        override fun onAdLoaded(ad: MaxAd) { isLoaded = true }
        override fun onAdLoadFailed(adUnitId: String, error: MaxError) { isLoaded = false }
        override fun onUserRewarded(ad: MaxAd, reward: MaxReward) = onRewarded()
        override fun onAdDisplayed(ad: MaxAd) = Unit
        override fun onAdHidden(ad: MaxAd) = Unit
        override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) = Unit
        override fun onAdClicked(ad: MaxAd) = Unit
    }

    override fun reload() {
        rewardedAd.setListener(listener)
        isLoaded = rewardedAd.isReady
        if (!isLoaded) rewardedAd.loadAd()
    }

    override fun show(activity: Activity, onRewarded: () -> Unit) {
        if (!rewardedAd.isReady) return
        this.onRewarded = onRewarded
        isLoaded = false
        rewardedAd.showAd(activity)
    }

    fun detach() = rewardedAd.setListener(null)
}
