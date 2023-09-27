package com.xbot.musifyze.data.repositories

import com.xbot.musifyze.data.models.AudioDataModel

interface AudioRepository {
    suspend fun getAllAudioFromDevice(): List<AudioDataModel>
}
