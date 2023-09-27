package com.xbot.musifyze.ui.models

import android.net.Uri
import com.xbot.musifyze.data.models.AudioDataModel

data class AudioModel(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val albumCoverUri: Uri?
)

fun AudioDataModel.toModel(): AudioModel {
    return AudioModel(
        id = this.id,
        uri = this.uri,
        title = this.title,
        artist = this.artist,
        album = this.album,
        duration = this.duration.toString(),
        albumCoverUri = this.albumCoverUri
    )
}
