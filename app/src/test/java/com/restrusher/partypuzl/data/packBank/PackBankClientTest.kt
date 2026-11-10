package com.restrusher.partypuzl.data.packBank

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.ServerSocket
import kotlin.concurrent.thread

/**
 * Runs the real `HttpURLConnection` code against a throwaway local server. The server is a bare
 * socket because unit tests compile against Android's library, which has no JDK `HttpServer`.
 */
class PackBankClientTest {

    private val server = ServerSocket(0)
    private var receivedEtag: String? = null

    @After
    fun tearDown() = server.close()

    @Test
    fun `a 200 returns the body and its etag`() = runTest {
        respondOnce("200 OK", body = BODY, etag = ETAG)
        assertEquals(PackBankFetch.Fetched(BODY, ETAG), client().fetch(etag = null))
    }

    @Test
    fun `the cached etag is sent and a 304 means not modified`() = runTest {
        respondOnce("304 Not Modified")
        assertEquals(PackBankFetch.NotModified, client().fetch(ETAG))
        assertEquals(ETAG, receivedEtag)
    }

    @Test
    fun `no etag is sent on a first download`() = runTest {
        respondOnce("200 OK", body = BODY)
        client().fetch(etag = null)
        assertNull(receivedEtag)
    }

    @Test
    fun `a server error is a failure`() = runTest {
        respondOnce("500 Internal Server Error", body = "oops")
        assertEquals(PackBankFetch.Failed, client().fetch(etag = null))
    }

    @Test
    fun `a missing file is a failure`() = runTest {
        respondOnce("404 Not Found", body = "not found")
        assertEquals(PackBankFetch.Failed, client().fetch(etag = null))
    }

    @Test
    fun `an unreachable host is a failure, not a crash`() = runTest {
        val closedPort = ServerSocket(0).use { it.localPort }
        assertEquals(PackBankFetch.Failed, PackBankClient("http://127.0.0.1:$closedPort/").fetch(null))
    }

    private fun client() = PackBankClient("http://127.0.0.1:${server.localPort}/v1/packs.json")

    private fun respondOnce(status: String, body: String = "", etag: String? = null) {
        thread(isDaemon = true) {
            server.accept().use { socket ->
                val headers = socket.getInputStream().bufferedReader().let { reader ->
                    generateSequence { reader.readLine() }.takeWhile { it.isNotEmpty() }.toList()
                }
                receivedEtag = headers.firstOrNull { it.startsWith("If-None-Match:", ignoreCase = true) }
                    ?.substringAfter(':')?.trim()
                socket.getOutputStream().write(response(status, body, etag).toByteArray())
            }
        }
    }

    private fun response(status: String, body: String, etag: String?): String = buildString {
        append("HTTP/1.1 $status\r\n")
        etag?.let { append("ETag: $it\r\n") }
        append("Content-Length: ${body.toByteArray().size}\r\n")
        append("Connection: close\r\n\r\n")
        append(body)
    }

    private companion object {
        const val BODY = """{"schemaVersion": 1}"""
        const val ETAG = "\"abc123\""
    }
}
