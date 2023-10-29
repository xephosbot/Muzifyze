package com.xbot.musifyze.ui.features.radio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xbot.musifyze.data.player.AudioAnalyzer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RadioViewModel constructor(audioAnalyzer: AudioAnalyzer) : ViewModel() {

    val state: StateFlow<RadioScreenState> = audioAnalyzer.dataFlow.map {
        RadioScreenState(
            fft = it.fft,
            waveform = it.waveform
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = RadioScreenState()
    )
}