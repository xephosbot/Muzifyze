package com.xbot.musifyze.data.models

import android.net.Uri

data class AudioDataModel(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val albumCoverUri: Uri?
)
