package com.restrusher.partypuzl.data.packBank

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackBankRepositoryTest {

    private val cache = mockk<PackBankCache>(relaxed = true)
    private val client = mockk<PackBankClient>()
    private val parser = mockk<PackBankParser>()
    private val repository = PackBankRepository(cache, client, parser)

    private val cached = content(version = 1)
    private val fresh = content(version = 2)

    @Test
    fun `the cached copy is available before any download`() = runTest {
        givenCache("old", etag = "e1")
        repository.loadCache()
        assertEquals(cached, repository.content.value)
    }

    @Test
    fun `a new download replaces the content and the cache`() = runTest {
        givenCache("old", etag = "e1")
        coEvery { client.fetch("e1") } returns PackBankFetch.Fetched("new", "e2")
        repository.refresh()
        assertEquals(fresh, repository.content.value)
        coVerify { cache.write(CachedPackBank("new", "e2")) }
    }

    @Test
    fun `a failed download keeps the cached content`() = runTest {
        givenCache("old", etag = "e1")
        coEvery { client.fetch(any()) } returns PackBankFetch.Failed
        repository.refresh()
        assertEquals(cached, repository.content.value)
        assertFalse(repository.isRefreshing.value)
    }

    @Test
    fun `a download that does not parse never overwrites the cache`() = runTest {
        givenCache("old", etag = "e1")
        coEvery { client.fetch(any()) } returns PackBankFetch.Fetched("broken", "e2")
        every { parser.parse("broken") } returns null
        repository.refresh()
        assertEquals(cached, repository.content.value)
        coVerify(exactly = 0) { cache.write(any()) }
    }

    @Test
    fun `refreshing is reported only while the download runs`() = runTest {
        givenCache("old", etag = "e1")
        var refreshingDuringFetch = false
        coEvery { client.fetch(any()) } coAnswers {
            refreshingDuringFetch = repository.isRefreshing.value
            PackBankFetch.NotModified
        }
        repository.refresh()
        assertTrue(refreshingDuringFetch)
        assertFalse(repository.isRefreshing.value)
    }

    @Test
    fun `not modified keeps the cached content`() = runTest {
        givenCache("old", etag = "e1")
        coEvery { client.fetch("e1") } returns PackBankFetch.NotModified
        repository.refresh()
        assertEquals(cached, repository.content.value)
        coVerify(exactly = 0) { cache.write(any()) }
    }

    @Test
    fun `a recent check is not repeated unless forced`() = runTest {
        givenCache("old", etag = "e1")
        coEvery { client.fetch(any()) } returns PackBankFetch.NotModified
        repository.refresh()
        repository.refresh()
        coVerify(exactly = 1) { client.fetch(any()) }
        repository.refresh(force = true)
        coVerify(exactly = 2) { client.fetch(any()) }
    }

    @Test
    fun `a failed check is retried on the next refresh`() = runTest {
        coEvery { cache.read() } returns null
        coEvery { client.fetch(null) } returns PackBankFetch.Failed
        repository.refresh()
        repository.refresh()
        coVerify(exactly = 2) { client.fetch(null) }
    }

    private fun givenCache(body: String, etag: String?) {
        coEvery { cache.read() } returns CachedPackBank(body, etag)
        every { parser.parse(body) } returns cached
        every { parser.parse("new") } returns fresh
    }

    private fun content(version: Int) = PackBankContent(
        contentVersion = version,
        packs = mapOf("premium_mix" to mapOf("en" to RemotePackPrompts(truths = listOf("v$version"))))
    )
}
