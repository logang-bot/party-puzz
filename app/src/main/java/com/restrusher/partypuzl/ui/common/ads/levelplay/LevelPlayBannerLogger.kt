package com.restrusher.partypuzl.ui.common.ads.levelplay

import android.util.Log
import com.unity3d.mediation.LevelPlayAdError
import com.unity3d.mediation.LevelPlayAdInfo
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener

/** Logs each banner load result under the `LevelPlayBanner` tag, so a missing banner shows why. */
internal object LevelPlayBannerLogger : LevelPlayBannerAdViewListener {
    private const val TAG = "LevelPlayBanner"

    override fun onAdLoaded(adInfo: LevelPlayAdInfo) {
        Log.d(TAG, "loaded ${adInfo.adUnitId} from ${adInfo.adNetwork}")
    }

    override fun onAdLoadFailed(error: LevelPlayAdError) {
        Log.w(TAG, "failed ${error.adUnitId}: ${error.errorCode} ${error.errorMessage}")
    }
}
