package com.restrusher.partypuzl.ui.views.licenses

import com.restrusher.partypuzl.data.licenses.OpenSourceLibrary

data class LicensesState(
    val isLoading: Boolean = true,
    val libraries: List<OpenSourceLibrary> = emptyList(),
    /** The library whose full licence text is open in the dialog, or null when it is closed. */
    val selectedLibrary: OpenSourceLibrary? = null
)
