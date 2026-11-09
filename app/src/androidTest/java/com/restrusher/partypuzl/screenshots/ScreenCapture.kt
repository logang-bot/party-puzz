package com.restrusher.partypuzl.screenshots

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.services.storage.TestStorage

/**
 * Saves whole-screen PNGs, status bar included, through TestStorage. Gradle copies those files to
 * the PC before it uninstalls the app, and `collectStoreScreenshots` sorts them into
 * `store-screenshots/`. It reads the screen through UiAutomation, so it never waits for Compose
 * to go idle — the mini-games animate forever.
 */
object ScreenCapture {
    private val storage by lazy { TestStorage() }

    fun save(fileName: String) {
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        checkNotNull(bitmap) { "UiAutomation returned no screenshot for $fileName" }
        storage.openOutputFile("$fileName.png").use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
