package com.restrusher.partypuzz.ui.views.gameConfig

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameConfigViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(GameConfigState())
    val uiState: StateFlow<GameConfigState> = _uiState.asStateFlow()

//    fun addPlayer() {
//
//    }

}