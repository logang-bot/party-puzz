package com.restrusher.partypuzl.ui

import android.os.Bundle

/**
 * Tells a recreated process apart from a configuration change.
 *
 * Android can kill the app while it is in the background and later rebuild the last screen
 * from saved state. The game session — players, party id, mode — lives only in memory, so a rebuilt game screen would start with nothing. After such a
 * restart the app opens on Home instead; everything worth keeping is already in Room.
 */
internal object ProcessRestart {
    private var activityCreated = false

    /** The state `MainActivity` should restore: none when the process was recreated. */
    fun stateToRestore(savedInstanceState: Bundle?): Bundle? {
        val isProcessRestart = savedInstanceState != null && !activityCreated
        activityCreated = true
        return if (isProcessRestart) null else savedInstanceState
    }
}
