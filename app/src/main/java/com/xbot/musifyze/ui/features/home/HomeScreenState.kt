package com.xbot.musifyze.ui.features.home

import com.xbot.musifyze.data.models.AudioDataModel
import com.xbot.musifyze.ui.models.AudioModel

data class HomeScreenState(
    val audioList: List<AudioDataModel> = emptyList(),
    val currentAudio: AudioModel? = null
)