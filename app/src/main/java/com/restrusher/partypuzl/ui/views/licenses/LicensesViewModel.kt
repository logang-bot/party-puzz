package com.restrusher.partypuzl.ui.views.licenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restrusher.partypuzl.data.licenses.OpenSourceLibrary
import com.restrusher.partypuzl.data.licenses.OpenSourceLicensesSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LicensesViewModel @Inject constructor(
    private val licensesSource: OpenSourceLicensesSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(LicensesState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val libraries = licensesSource.load()
            _uiState.update { it.copy(isLoading = false, libraries = libraries) }
        }
    }

    fun openLicense(library: OpenSourceLibrary) = _uiState.update { it.copy(selectedLibrary = library) }

    fun closeLicense() = _uiState.update { it.copy(selectedLibrary = null) }
}
