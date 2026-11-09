package com.restrusher.partypuzl.ads

import android.app.Activity
import android.os.Bundle
import com.unity3d.mediation.LevelPlay

/**
 * Debug builds only. Opens the LevelPlay Test Suite, which lists every network adapter with its
 * integration status and lets you load a test ad from each one. Start the app once first so the SDK
 * is initialised, then:
 * `adb shell am start -n com.restrusher.partypuzl/.ads.LevelPlayTestSuiteActivity`
 */
class LevelPlayTestSuiteActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LevelPlay.launchTestSuite(this)
        finish()
    }
}
