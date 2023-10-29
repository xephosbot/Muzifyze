package com.xbot.musifyze.ui.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import com.xbot.musifyze.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlin.math.floor

@Composable
fun rememberMediaPlayback(): MediaPlayback {
    val controller = LocalMediaComponent.current.controller
    val scope = rememberCoroutineScope()
    return remember(controller, scope) {
        MediaPlayback(controller, scope)
    }
}

@Composable
fun rememberMediaLibrary(): MediaLibrary {
    val browser = LocalMediaComponent.current.browser
    val scope = rememberCoroutineScope()
    return remember(browser, scope) {
        MediaLibrary(browser, scope)
    }
}

@Stable
class MediaPlayback(
    private val controller: MediaController?,
    private val scope: CoroutineScope
) {
    var state by mutableStateOf(State())
        private set

    init {
        controller?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)

                val playbackState = state.toPlaybackState()
                updateState { copy(playbackState = playbackState) }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)

                val mediaData = mediaItem?.toAudio()
                updateState { copy(current = mediaData) }
            }
        })

        scope.launch {
            while (true) {
                val currPosition = controller?.currentPosition ?: 0L
                if (state.playbackTime != currPosition) {
                    updateState { copy(playbackTime = currPosition) }
                }
                delay(POSITION_UPDATE_INTERVAL_MILLIS)
            }
        }

        restoreState()
    }

    fun play(audio: MediaData.Audio? = null) {
        if (audio != null) {
            controller?.setMediaItem(audio.toMediaItem())
        }

        controller?.prepare()
        controller?.play()
    }

    fun pause() {
        controller?.pause()
    }

    private fun restoreState() {
        if (controller == null)
            return

        updateState {
            copy(
                current = controller.currentMediaItem?.toAudio(),
                playbackState = controller.playbackState.toPlaybackState()
            )
        }
    }

    private inline fun updateState(transform: State.() -> State) {
        state = state.transform()
    }

    private fun Int.toPlaybackState(): State.PlaybackState {
        if (controller == null) return State.PlaybackState.STOPPED
        return when (this) {
            Player.STATE_BUFFERING -> State.PlaybackState.LOADING
            Player.STATE_READY -> if (controller.playWhenReady) State.PlaybackState.PLAYING else State.PlaybackState.PAUSED
            Player.STATE_ENDED -> State.PlaybackState.STOPPED
            else -> state.playbackState
        }
    }

    data class State(
        val current: MediaData.Audio? = null,
        val playbackState: PlaybackState = PlaybackState.STOPPED,
        val playbackTime: Long = 0
    ) {
        enum class PlaybackState {
            PLAYING,
            PAUSED,
            LOADING,
            STOPPED
        }
    }

    companion object {
        private const val POSITION_UPDATE_INTERVAL_MILLIS = 100L
    }
}

@Stable
class MediaLibrary(
    private val mediaBrowser: MediaBrowser?,
    private val scope: CoroutineScope
) {
    var categories = mutableStateListOf<MediaData.Folder>()
        private set

    init {
        scope.launch {
            val root = mediaBrowser?.getLibraryRoot(null)?.await()?.value
            val categoriesResult = root?.let {
                mediaBrowser?.getChildren(it.mediaId, 0, Int.MAX_VALUE, null)?.await()
            }
            categories.addAll(categoriesResult?.value?.map(MediaItem::toFolder) ?: emptyList())
        }
    }

    suspend fun getItemsInFolder(folder: MediaData.Folder): List<MediaData> {
        val itemsResult = mediaBrowser?.getChildren(folder.id, 0, Int.MAX_VALUE, null)?.await()
        return itemsResult?.value?.map {
            when (it.mediaMetadata.mediaType) {
                MediaMetadata.MEDIA_TYPE_MUSIC -> it.toAudio()
                else -> it.toFolder()
            }
        } ?: emptyList()
    }
}

sealed class MediaData {
    data class Audio(
        val id: String,
        val title: String?,
        val artist: String?,
        val album: String?,
        val uri: Uri?,
        val albumUri: Uri?,
        val duration: Long
    ) : MediaData()

    data class Folder(
        val id: String,
        val title: String?,
        val albumUri: Uri?
    ) : MediaData()
}

fun MediaData.Audio.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setUri(uri)
        .setMediaId(id)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(albumUri)
                .build()
        )
        .build()
}

fun MediaItem.toAudio(): MediaData.Audio {
    val retriever = MediaMetadataRetriever()
    localConfiguration?.uri?.let {
        retriever.setDataSource(it.toString())
    }
    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
    return MediaData.Audio(
        id = mediaId,
        title = mediaMetadata.title?.toString(),
        artist = mediaMetadata.artist?.toString(),
        album = mediaMetadata.albumTitle?.toString(),
        uri = localConfiguration?.uri,
        albumUri = mediaMetadata.artworkUri,
        duration = duration
    )
}

fun MediaItem.toFolder(): MediaData.Folder {
    return MediaData.Folder(
        id = mediaId,
        title = mediaMetadata.title?.toString(),
        albumUri = mediaMetadata.artworkUri
    )
}

fun Long.formatTime(context: Context): String {
    val totalSeconds = floor(this / 1E3).toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds - (minutes * 60)
    return if (this < 0) context.getString(R.string.duration_unknown)
    else context.getString(R.string.duration_format).format(minutes, remainingSeconds)
}
