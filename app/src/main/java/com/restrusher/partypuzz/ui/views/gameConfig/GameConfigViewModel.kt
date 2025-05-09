package com.restrusher.partypuzz.ui.views.gameConfig

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GameConfigViewModel @Inject constructor(): ViewModel() {
    private val _uiState = MutableStateFlow(GameConfigState())
    val uiState: StateFlow<GameConfigState> = _uiState.asStateFlow()

//    fun addPlayer() {
//
//    }

}