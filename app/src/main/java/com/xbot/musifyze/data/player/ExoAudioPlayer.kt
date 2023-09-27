package com.xbot.musifyze.data.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoAudioPlayer @Inject constructor(
    private val exoPlayer: ExoPlayer
) : AudioPlayer {

    private val _state: MutableStateFlow<InternalState> = MutableStateFlow(InternalState())
    override val state: StateFlow<AudioPlayerState> = _state.stateMap(InternalState::toAudioPlayerState)

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)

                val playbackState = when (state) {
                    Player.STATE_READY -> if (exoPlayer.playWhenReady) AudioPlayerState.State.PLAYING else AudioPlayerState.State.PAUSED
                    Player.STATE_ENDED -> AudioPlayerState.State.STOPPED
                    else -> _state.value.playbackState
                }

                if (state == Player.STATE_READY || state == Player.STATE_BUFFERING) {
                    updateState { copy(playbackTime = exoPlayer.contentPosition) }
                }

                updateState { copy(playbackState = playbackState) }
            }
        })
    }

    override fun setAudioQueue(audioList: List<Uri>) {
        updateState { copy(audioQueue = audioList) }
    }

    override fun play(audioUri: Uri?) {
        if (_state.value.playbackState != AudioPlayerState.State.PAUSED) {
            val mediaItem = audioUri?.let { MediaItem.fromUri(it) }
                ?: _state.value.audioQueue.getOrNull(_state.value.currentTrackIndex)?.let { MediaItem.fromUri(it) }

            if (mediaItem != null) {
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            }
        }
        exoPlayer.play()
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun skipToNext() {
        if (_state.value.audioQueue.isNotEmpty()) {
            updateState { copy(
                currentTrackIndex = (_state.value.currentTrackIndex + 1) % _state.value.audioQueue.size)
            }
            play()
        }
    }

    override fun skipToPrevious() {
        if (_state.value.audioQueue.isNotEmpty()) {
            updateState { copy(
                currentTrackIndex = (_state.value.currentTrackIndex - 1 + _state.value.audioQueue.size) % _state.value.audioQueue.size)
            }
            play()
        }
    }

    private inline fun updateState(transform: InternalState.() -> InternalState) {
        _state.update { it.transform() }
    }

    private data class InternalState(
        val audioQueue: List<Uri> = emptyList(),
        val currentTrackIndex: Int = 0,
        val playbackState: AudioPlayerState.State = AudioPlayerState.State.STOPPED,
        val playbackTime: Long = 0
    ) {
        fun toAudioPlayerState(): AudioPlayerState {
            return AudioPlayerState(audioQueue, playbackState, playbackTime)
        }
    }
}