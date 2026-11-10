package com.restrusher.partypuzl.ui.views.gameConfig

import androidx.annotation.StringRes
import com.restrusher.partypuzl.R
import com.restrusher.partypuzl.data.packBank.PackBankError
import com.restrusher.partypuzl.data.packBank.PackBankRefresh

/** The snackbar for a download the user was waiting on, or null when nothing went wrong. */
@get:StringRes
internal val PackBankRefresh.failureMessageRes: Int?
    get() = (this as? PackBankRefresh.Failed)?.error?.messageRes

@get:StringRes
internal val PackBankError.messageRes: Int
    get() = when (this) {
        PackBankError.NO_CONNECTION -> R.string.pack_download_error_no_connection
        PackBankError.RATE_LIMITED -> R.string.pack_download_error_rate_limited
        PackBankError.SERVER_UNAVAILABLE,
        PackBankError.INVALID_CONTENT -> R.string.pack_download_error_unavailable
    }
