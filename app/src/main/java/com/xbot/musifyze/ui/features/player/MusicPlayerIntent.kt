package com.xbot.musifyze.ui.features.player

import android.net.Uri

sealed class MusicPlayerIntent {
    data class Play(val uri: Uri? = null) : MusicPlayerIntent()
    data object Pause : MusicPlayerIntent()
}