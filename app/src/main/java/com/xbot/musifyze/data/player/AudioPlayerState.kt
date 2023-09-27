package com.xbot.musifyze.data.player

import android.net.Uri

data class AudioPlayerState(
    val queue: List<Uri> = emptyList(),
    val state: State = State.STOPPED,
    val playbackTime: Long = 0
) {
    enum class State {
        PLAYING,
        PAUSED,
        STOPPED
    }
}