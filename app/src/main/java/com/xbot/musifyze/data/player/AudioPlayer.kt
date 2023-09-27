package com.xbot.musifyze.data.player

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val state: StateFlow<AudioPlayerState>

    fun setAudioQueue(audioList: List<Uri>)
    fun play(audioUri: Uri? = null)
    fun pause()
    fun skipToNext()
    fun skipToPrevious()
}