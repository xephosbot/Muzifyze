package com.xbot.musifyze.ui.features.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xbot.musifyze.data.player.AudioPlayer
import com.xbot.musifyze.data.repositories.AudioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val audioPlayer: AudioPlayer,
    private val audioRepository: AudioRepository
) : ViewModel() {

    private val _state: MutableStateFlow<HomeScreenState> = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val audioList = audioRepository.getAllAudioFromDevice()

            _state.update {
                it.copy(
                    audioList = audioList
                )
            }
        }
    }

    fun play(uri: Uri) {
        audioPlayer.play(uri)
    }
}