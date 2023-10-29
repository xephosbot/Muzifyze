package com.xbot.musifyze.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.guava.await

@Composable
fun MediaComponentProvider(
    mediaControllerFuture: ListenableFuture<MediaController>? = null,
    mediaBrowserFuture: ListenableFuture<MediaBrowser>? = null,
    content: @Composable () -> Unit
) {
    val mediaControllerState = remember { mutableStateOf<MediaController?>(null) }
    val mediaBrowserState = remember { mutableStateOf<MediaBrowser?>(null) }

    LaunchedEffect(key1 = mediaControllerFuture, key2 = mediaBrowserFuture) {
        mediaControllerState.value = mediaControllerFuture?.await()
        mediaBrowserState.value = mediaBrowserFuture?.await()
    }

    CompositionLocalProvider(
        value = LocalMediaComponent provides MediaComponent(
            controller = mediaControllerState.value,
            browser = mediaBrowserState.value
        ),
        content = content
    )
}

data class MediaComponent(
    val controller: MediaController? = null,
    val browser: MediaBrowser? = null
)

val LocalMediaComponent = staticCompositionLocalOf { MediaComponent() }
