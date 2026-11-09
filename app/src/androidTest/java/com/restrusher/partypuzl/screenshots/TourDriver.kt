package com.restrusher.partypuzl.screenshots

import android.content.Intent
import android.os.SystemClock
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

/**
 * Launches the screenshot host for one variant and drives it scene by scene.
 *
 * It taps through UiAutomator rather than a Compose test rule on purpose: a Compose rule takes
 * over the app's animation clock, so every enter transition freezes on its first frame and the
 * shots come out blank. UiAutomator leaves the app running in real time.
 */
class TourDriver(private val args: TourArgs) {

    lateinit var demo: SeededDemo
        private set

    private lateinit var scenario: ActivityScenario<ScreenshotHostActivity>
    private var shotNumber = 0
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val device by lazy { UiDevice.getInstance(instrumentation) }

    fun launch(firstShotNumber: Int) {
        shotNumber = firstShotNumber
        instrumentation.runOnMainSync { AppCompatDelegate.setApplicationLocales(locales()) }
        scenario = ActivityScenario.launch(hostIntent())
        demo = awaitSeededDemo()
        settle(LONG_SETTLE_MS)
    }

    fun navigate(route: Any) {
        scenario.onActivity { it.navigate(route) }
        settle(LONG_SETTLE_MS)
    }

    fun tapText(@StringRes textRes: Int) {
        val text = instrumentation.targetContext.getString(textRes)
        tap(findText(containsIgnoringCase(text), FIND_TIMEOUT_MS), "containing \"$text\"")
    }

    fun tapExactText(@StringRes textRes: Int) {
        val text = instrumentation.targetContext.getString(textRes)
        tap(findText(equalsIgnoringCase(text), FIND_TIMEOUT_MS), "equal to \"$text\"")
    }

    fun tapTextIfShown(@StringRes textRes: Int) {
        val text = instrumentation.targetContext.getString(textRes)
        findText(containsIgnoringCase(text), SHORT_SETTLE_MS)?.click()
        settle(SHORT_SETTLE_MS)
    }

    fun tapScreenCenter() {
        device.click(device.displayWidth / 2, device.displayHeight / 2)
        settle(SHORT_SETTLE_MS)
    }

    fun drawCircle() {
        val width = device.displayWidth.toFloat()
        CircleGesture.draw(Circle(width / 2, device.displayHeight * 0.5f, width * 0.3f))
    }

    fun settle(millis: Long) = SystemClock.sleep(millis)

    fun capture(name: String) {
        shotNumber++
        ScreenCapture.save("%s__%02d_%s".format(args.outputPrefix, shotNumber, name))
    }

    fun close() = scenario.close()

    private fun locales() = LocaleListCompat.forLanguageTags(args.languageTag)

    private fun hostIntent() =
        Intent(instrumentation.targetContext, ScreenshotHostActivity::class.java)
            .putExtra(ScreenshotHostActivity.EXTRA_LANGUAGE, args.languageTag)
            .putExtra(ScreenshotHostActivity.EXTRA_THEME, args.themeMode.name)

    private fun awaitSeededDemo(): SeededDemo {
        var seeded: SeededDemo? = null
        val deadline = SystemClock.uptimeMillis() + SEED_TIMEOUT_MS
        while (seeded == null && SystemClock.uptimeMillis() < deadline) {
            scenario.onActivity { seeded = it.seededDemo.value }
            SystemClock.sleep(SHORT_SETTLE_MS)
        }
        return checkNotNull(seeded) { "Demo data was not seeded within $SEED_TIMEOUT_MS ms" }
    }

    private fun findText(pattern: Pattern, timeoutMs: Long): UiObject2? =
        device.wait(Until.findObject(By.text(pattern)), timeoutMs)

    private fun tap(target: UiObject2?, description: String) {
        checkNotNull(target) { "No on-screen text $description within $FIND_TIMEOUT_MS ms" }.click()
        settle(SHORT_SETTLE_MS)
    }

    private fun containsIgnoringCase(text: String): Pattern =
        Pattern.compile(".*${Pattern.quote(text)}.*", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)

    private fun equalsIgnoringCase(text: String): Pattern =
        Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE)

    companion object {
        const val SHORT_SETTLE_MS = 600L
        const val LONG_SETTLE_MS = 1_800L
        private const val FIND_TIMEOUT_MS = 5_000L
        private const val SEED_TIMEOUT_MS = 15_000L
    }
}
