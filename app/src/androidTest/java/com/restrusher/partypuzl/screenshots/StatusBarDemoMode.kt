package com.restrusher.partypuzl.screenshots

import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry

/**
 * SystemUI demo mode: a fixed 12:00 clock, full battery and wifi, and no notification icons, so
 * every store screenshot shows the same tidy status bar whatever the device is doing.
 */
object StatusBarDemoMode {

    fun enter() {
        shell("settings put global sysui_demo_allowed 1")
        demo("enter")
        demo("clock -e hhmm 1200")
        demo("battery -e level 100 -e plugged false")
        demo("network -e wifi show -e level 4 -e fully true -e mobile hide")
        demo("notifications -e visible false")
    }

    fun exit() = demo("exit")

    private fun demo(command: String) =
        shell("am broadcast -a com.android.systemui.demo -e command $command")

    private fun shell(command: String) {
        val output = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        ParcelFileDescriptor.AutoCloseInputStream(output).use { it.readBytes() }
    }
}
