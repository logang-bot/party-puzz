package com.restrusher.partypuzl.ui.common.ads.meta

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import com.restrusher.partypuzl.ui.common.ads.RewardedAdHandle

class MetaRewardedAd(
    context: Context,
    placementId: String
) : RewardedAdHandle {
    private var isLoaded by mutableStateOf(false)

    override val isReady get() = isLoaded

    private val rewardedAd = RewardedVideoAd(context, placementId)
    private var onRewarded: () -> Unit = {}

    private val listener = object : RewardedVideoAdListener {
        override fun onAdLoaded(ad: Ad) { isLoaded = true }
        override fun onError(ad: Ad, error: AdError) { isLoaded = false }
        override fun onRewardedVideoCompleted() = onRewarded()
        override fun onRewardedVideoClosed() = Unit
        override fun onAdClicked(ad: Ad) = Unit
        override fun onLoggingImpression(ad: Ad) = Unit
    }

    override fun reload() {
        rewardedAd.loadAd(rewardedAd.buildLoadAdConfig().withAdListener(listener).build())
    }

    override fun show(activity: Activity, onRewarded: () -> Unit) {
        if (!isLoaded || rewardedAd.isAdInvalidated) return
        this.onRewarded = onRewarded
        isLoaded = false
        rewardedAd.show()
    }

    fun destroy() = rewardedAd.destroy()
}
