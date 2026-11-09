package com.restrusher.partypuzl.ads

import android.app.Activity
import android.os.Bundle
import com.applovin.sdk.AppLovinSdk

/**
 * Debug builds only. Opens AppLovin's Mediation Debugger, which lists every network adapter with
 * its init status and lets you force a test ad from one network:
 * `adb shell am start -n com.restrusher.partypuzl/.ads.MediationDebuggerActivity`
 */
class MediationDebuggerActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLovinSdk.getInstance(this).showMediationDebugger()
        finish()
    }
}
