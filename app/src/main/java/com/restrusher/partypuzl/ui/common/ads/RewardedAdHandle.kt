package com.restrusher.partypuzl.ui.common.ads

import android.app.Activity

/** A rewarded ad for one [AdPlacement]. [show] consumes it; call [reload] to fetch the next one. */
interface RewardedAdHandle {
    val isReady: Boolean
    fun reload()
    fun show(activity: Activity, onRewarded: () -> Unit)
}
