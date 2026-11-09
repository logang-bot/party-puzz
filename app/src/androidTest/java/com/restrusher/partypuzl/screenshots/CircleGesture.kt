package com.restrusher.partypuzl.screenshots

import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.math.cos
import kotlin.math.sin

data class Circle(val centerX: Float, val centerY: Float, val radius: Float)

/** Draws a finger circle on the screen through UiAutomation, for Circle Master's canvas. */
object CircleGesture {
    private const val STEPS = 60
    private const val STEP_MS = 12L

    fun draw(circle: Circle) {
        val downTime = SystemClock.uptimeMillis()
        inject(MotionEvent.ACTION_DOWN, downTime, circle.pointAt(0))
        (1..STEPS).forEach { step ->
            SystemClock.sleep(STEP_MS)
            inject(MotionEvent.ACTION_MOVE, downTime, circle.pointAt(step))
        }
        inject(MotionEvent.ACTION_UP, downTime, circle.pointAt(STEPS))
    }

    private fun Circle.pointAt(step: Int): Pair<Float, Float> {
        val angle = 2 * Math.PI * step / STEPS
        return Pair(centerX + radius * cos(angle).toFloat(), centerY + radius * sin(angle).toFloat())
    }

    private fun inject(action: Int, downTime: Long, point: Pair<Float, Float>) {
        val now = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(downTime, now, action, point.first, point.second, 0)
        event.source = InputDevice.SOURCE_TOUCHSCREEN
        InstrumentationRegistry.getInstrumentation().uiAutomation.injectInputEvent(event, true)
        event.recycle()
    }
}
