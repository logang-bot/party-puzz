package com.restrusher.partypuzl.ui.common.ads.levelplay

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.restrusher.partypuzl.ui.common.ads.RewardedAdHandle
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.rewarded.LevelPlayReward
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener

/**
 * [RewardedAdHandle] around one [LevelPlayRewardedAd]. LevelPlay ad objects may only exist after
 * init succeeds, so the ad is created on the first [reload], which the provider defers until then.
 */
class LevelPlayRewardedHandle(adUnitId: String) : RewardedAdHandle {
    private var isLoaded by mutableStateOf(false)

    override val isReady get() = isLoaded

    private var onRewarded: () -> Unit = {}
    private val rewardedAd by lazy { LevelPlayRewardedAd(adUnitId).also { it.setListener(listener) } }

    private val listener = object : LevelPlayRewardedAdListener {
        override fun onAdLoaded(adInfo: LevelPlayAdInfo) { isLoaded = true }
        override fun onAdLoadFailed(error: LevelPlayAdError) { isLoaded = false }
        override fun onAdDisplayed(adInfo: LevelPlayAdInfo) = Unit
        override fun onAdRewarded(reward: LevelPlayReward, adInfo: LevelPlayAdInfo) = onRewarded()
    }

    override fun reload() {
        isLoaded = rewardedAd.isAdReady()
        if (!isLoaded) rewardedAd.loadAd()
    }

    override fun show(activity: Activity, onRewarded: () -> Unit) {
        if (!isLoaded || !rewardedAd.isAdReady()) return
        this.onRewarded = onRewarded
        isLoaded = false
        rewardedAd.showAd(activity)
    }
}
