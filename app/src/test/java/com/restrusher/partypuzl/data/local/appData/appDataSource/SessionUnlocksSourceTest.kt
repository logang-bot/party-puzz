package com.restrusher.partypuzl.data.local.appData.appDataSource

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionUnlocksSourceTest {

    @After
    fun tearDown() = SessionUnlocksSource.clear()

    @Test
    fun `an unlocked pack is reported as unlocked`() {
        SessionUnlocksSource.unlock("premium_spicy")
        assertTrue(SessionUnlocksSource.isUnlocked("premium_spicy"))
        assertFalse(SessionUnlocksSource.isUnlocked("premium_nsfw"))
    }

    @Test
    fun `the flow exposes every unlocked pack`() {
        SessionUnlocksSource.unlock("a")
        SessionUnlocksSource.unlock("b")
        SessionUnlocksSource.unlock("a")
        assertEquals(setOf("a", "b"), SessionUnlocksSource.unlockedPackIds.value)
    }

    @Test
    fun `clearing drops every session unlock`() {
        SessionUnlocksSource.unlock("premium_spicy")
        SessionUnlocksSource.clear()
        assertFalse(SessionUnlocksSource.isUnlocked("premium_spicy"))
        assertTrue(SessionUnlocksSource.unlockedPackIds.value.isEmpty())
    }
}
