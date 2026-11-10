package com.restrusher.partypuzl.data.packBank

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PackBankCacheTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val directory get() = File(folder.root, "pack_bank")
    private val cache get() = PackBankCache(directory)

    @Test
    fun `nothing is cached on a fresh install`() = runTest {
        assertNull(cache.read())
    }

    @Test
    fun `a written copy reads back with its etag`() = runTest {
        cache.write(CachedPackBank("{}", "\"abc\""))
        assertEquals(CachedPackBank("{}", "\"abc\""), cache.read())
    }

    @Test
    fun `a copy without an etag drops the old one`() = runTest {
        cache.write(CachedPackBank("v1", "e1"))
        cache.write(CachedPackBank("v2", null))
        assertEquals(CachedPackBank("v2", null), cache.read())
    }

    @Test
    fun `no temp files are left behind`() = runTest {
        cache.write(CachedPackBank("v1", "e1"))
        assertEquals(setOf("packs_v1.json", "packs_v1.etag"), directory.list()!!.toSet())
    }
}
