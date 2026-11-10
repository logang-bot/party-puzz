package com.restrusher.partypuzl.data.packs

import com.restrusher.partypuzl.data.local.entities.QuestionPackEntity
import com.restrusher.partypuzl.data.models.PackCategory
import com.restrusher.partypuzl.data.models.PackTier
import com.restrusher.partypuzl.data.models.StickyDarePrompt
import com.restrusher.partypuzl.data.packBank.PackBankContent
import com.restrusher.partypuzl.data.packBank.PackBankRepository
import com.restrusher.partypuzl.data.packBank.RemotePackPrompts
import com.restrusher.partypuzl.data.packBank.RemoteStickyDare
import com.restrusher.partypuzl.data.repositories.interfaces.QuestionPackRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumPackContentLoaderTest {

    private val questionPackRepository = mockk<QuestionPackRepository>()
    private val packBankRepository = mockk<PackBankRepository>(relaxed = true)
    private val resolver = mockk<QuestionPromptResolver> {
        every { durationLabel(180) } returns "3 minutes"
    }
    private val loader = PremiumPackContentLoader(questionPackRepository, packBankRepository, resolver)

    @Test
    fun `only enabled premium packs are dealt`() = runTest {
        givenPacks(premium("spicy", enabled = true), premium("nsfw", enabled = false))
        givenBank("spicy" to RemotePackPrompts(truths = listOf("a")), "nsfw" to RemotePackPrompts(truths = listOf("b")))
        assertEquals(listOf("a"), loader.load().truths)
    }

    @Test
    fun `sticky dares get a localised duration label`() = runTest {
        givenPacks(premium("spicy", enabled = true))
        givenBank("spicy" to RemotePackPrompts(stickyDares = listOf(RemoteStickyDare("s", "doing s", 180))))
        assertEquals(listOf(StickyDarePrompt("s", "doing s", "3 minutes", 180)), loader.load().stickyDares)
    }

    @Test
    fun `a pack that has not downloaded contributes nothing`() = runTest {
        givenPacks(premium("spicy", enabled = true))
        givenBank()
        assertTrue(loader.load().isEmpty)
    }

    private fun givenPacks(vararg packs: QuestionPackEntity) {
        coEvery { questionPackRepository.getPacksOnce() } returns packs.toList()
    }

    private fun givenBank(vararg packs: Pair<String, RemotePackPrompts>) {
        val content = PackBankContent(1, packs.associate { (id, prompts) -> id to mapOf("en" to prompts) })
        every { packBankRepository.content } returns MutableStateFlow(content)
    }

    private fun premium(id: String, enabled: Boolean) = QuestionPackEntity(
        id = id,
        tier = PackTier.PREMIUM,
        category = PackCategory.TRUTH_OR_DARE,
        isEnabled = enabled,
        isUnlocked = true
    )
}
