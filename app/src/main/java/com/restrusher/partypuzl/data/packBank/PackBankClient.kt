package com.restrusher.partypuzl.data.packBank

import com.restrusher.partypuzl.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

sealed interface PackBankFetch {
    data class Fetched(val body: String, val etag: String?) : PackBankFetch
    data object NotModified : PackBankFetch
    data object Failed : PackBankFetch
}

/**
 * Downloads `packs.json`. Never throws: no network, a timeout or any non-200/304 answer is
 * [PackBankFetch.Failed]. Sending the cached ETag lets an unchanged file come back as a bodiless
 * 304.
 */
class PackBankClient(private val url: String) {

    @Inject
    constructor() : this(BuildConfig.PACK_BANK_URL)

    suspend fun fetch(etag: String?): PackBankFetch = withContext(Dispatchers.IO) {
        runCatching { request(etag) }.getOrDefault(PackBankFetch.Failed)
    }

    private fun request(etag: String?): PackBankFetch {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MILLIS
        connection.readTimeout = TIMEOUT_MILLIS
        etag?.let { connection.setRequestProperty("If-None-Match", it) }
        return try {
            responseOf(connection)
        } finally {
            connection.disconnect()
        }
    }

    private fun responseOf(connection: HttpURLConnection): PackBankFetch =
        when (connection.responseCode) {
            HttpURLConnection.HTTP_OK -> PackBankFetch.Fetched(
                body = connection.inputStream.bufferedReader().use { it.readText() },
                etag = connection.getHeaderField("ETag")
            )
            HttpURLConnection.HTTP_NOT_MODIFIED -> PackBankFetch.NotModified
            else -> PackBankFetch.Failed
        }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
    }
}
