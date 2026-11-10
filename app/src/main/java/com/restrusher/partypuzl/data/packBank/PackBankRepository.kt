package com.restrusher.partypuzl.data.packBank

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The premium packs, from the device cache first and the network second.
 *
 * Nothing here can fail loudly. Until the first download succeeds [content] is empty, and every
 * failure after that keeps whatever was there — the app never loses packs because the site is down.
 */
@Singleton
class PackBankRepository @Inject constructor(
    private val cache: PackBankCache,
    private val client: PackBankClient,
    private val parser: PackBankParser
) {

    private val _content = MutableStateFlow(PackBankContent.EMPTY)
    val content: StateFlow<PackBankContent> = _content.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val mutex = Mutex()
    private var isCacheLoaded = false
    private var cachedEtag: String? = null
    private var lastCheckedAtMillis: Long? = null

    /** Makes the cached copy available without touching the network. */
    suspend fun loadCache() = mutex.withLock { loadCacheOnce() }

    /** Skipped while the last check is under [REFRESH_INTERVAL_MILLIS] old, unless [force]. */
    suspend fun refresh(force: Boolean = false) = mutex.withLock {
        loadCacheOnce()
        if (!force && isRecentlyChecked()) return@withLock
        _isRefreshing.value = true
        try {
            apply(client.fetch(cachedEtag))
        } finally {
            _isRefreshing.value = false
        }
    }

    private suspend fun loadCacheOnce() {
        if (isCacheLoaded) return
        isCacheLoaded = true
        val cached = cache.read() ?: return
        parser.parse(cached.body)?.let { _content.value = it }
        cachedEtag = cached.etag
    }

    private fun isRecentlyChecked(): Boolean {
        val checkedAt = lastCheckedAtMillis ?: return false
        return System.currentTimeMillis() - checkedAt < REFRESH_INTERVAL_MILLIS
    }

    private suspend fun apply(fetch: PackBankFetch) {
        when (fetch) {
            is PackBankFetch.Fetched -> store(fetch)
            PackBankFetch.NotModified -> markChecked()
            PackBankFetch.Failed -> Unit
        }
    }

    /** A download that doesn't parse is ignored, so a broken publish can't wipe a good cache. */
    private suspend fun store(fetch: PackBankFetch.Fetched) {
        val parsed = parser.parse(fetch.body) ?: return
        cache.write(CachedPackBank(fetch.body, fetch.etag))
        _content.value = parsed
        cachedEtag = fetch.etag
        markChecked()
    }

    private fun markChecked() {
        lastCheckedAtMillis = System.currentTimeMillis()
    }

    private companion object {
        const val REFRESH_INTERVAL_MILLIS = 12 * 60 * 60 * 1000L
    }
}
