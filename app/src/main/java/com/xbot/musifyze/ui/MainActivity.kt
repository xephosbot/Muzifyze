package com.xbot.musifyze.ui

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.xbot.musifyze.services.PlaybackService
import com.xbot.musifyze.ui.theme.MusifyzeTheme
import com.xbot.musifyze.ui.utils.MediaComponentProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            //TODO: Разрешение было предоставлено, вы можете продолжить работу с аудио медиа
        } else {
            //TODO: Разрешение было отклонено, вы должны обработать эту ситуацию
        }
    }

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        browserFuture = MediaBrowser.Builder(this, sessionToken).buildAsync()

        setContent {
            MusifyzeTheme {
                MediaComponentProvider(
                    mediaControllerFuture = controllerFuture,
                    mediaBrowserFuture = browserFuture
                ) {
                    MusifyzeApp()
                }
            }
        }

        requestAudioPermission()
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaController.releaseFuture(controllerFuture)
        MediaBrowser.releaseFuture(browserFuture)
    }
}
