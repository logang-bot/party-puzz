package com.restrusher.partypuzl.ui.views.home

import com.restrusher.partypuzl.data.local.entities.PlayerEntity
import com.restrusher.partypuzl.data.models.Gender
import com.restrusher.partypuzl.data.models.InterestedIn
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeDisplayUtilsTest {

    @Test
    fun `only the first word of a name is shown`() {
        assertEquals("Maria", "  Maria   José Pérez ".shortenedName())
    }

    @Test
    fun `long first names are cut to ten characters with an ellipsis`() {
        assertEquals("Maximilian…", "Maximiliano".shortenedName())
        assertEquals("Maximilian", "Maximilian".shortenedName())
    }

    @Test
    fun `the slice shows three names and counts the rest`() {
        val players = listOf("Ana", "Bo", "Cy", "Di", "Ed").map(::player)
        assertEquals(listOf("Ana", "Bo", "Cy") to 2, playerNamesSlice(players))
    }

    @Test
    fun `a small party shows everyone with nothing left over`() {
        assertEquals(listOf("Ana") to 0, playerNamesSlice(listOf(player("Ana"))))
    }

    private fun player(name: String) =
        PlayerEntity(nickName = name, gender = Gender.Unknown, interestedIn = InterestedIn.Both)
}
