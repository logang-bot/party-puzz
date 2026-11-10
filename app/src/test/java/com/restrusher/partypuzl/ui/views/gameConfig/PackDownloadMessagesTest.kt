package com.restrusher.partypuzl.ui.views.gameConfig

import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.packBank.PackBankError
import com.restrusher.partypuzl.data.packBank.PackBankRefresh
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PackDownloadMessagesTest {

    @Test
    fun `a download that worked or was not needed shows nothing`() {
        listOf(PackBankRefresh.Updated, PackBankRefresh.Unchanged, PackBankRefresh.Skipped)
            .forEach { assertNull(it.failureMessageRes) }
    }

    @Test
    fun `each failure explains itself`() {
        assertEquals(R.string.pack_download_error_no_connection, failed(PackBankError.NO_CONNECTION))
        assertEquals(R.string.pack_download_error_rate_limited, failed(PackBankError.RATE_LIMITED))
        assertEquals(R.string.pack_download_error_unavailable, failed(PackBankError.SERVER_UNAVAILABLE))
        assertEquals(R.string.pack_download_error_unavailable, failed(PackBankError.INVALID_CONTENT))
    }

    private fun failed(error: PackBankError) = PackBankRefresh.Failed(error).failureMessageRes
}
