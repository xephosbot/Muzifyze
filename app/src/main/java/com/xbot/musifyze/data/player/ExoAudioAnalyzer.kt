package com.xbot.musifyze.data.player

import android.media.audiofx.Visualizer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ExoAudioAnalyzer @Inject constructor(audioSessionId: Int): AudioAnalyzer {

    private val visualizer: Visualizer = Visualizer(audioSessionId).apply {
        captureSize = 32768
        scalingMode = Visualizer.SCALING_MODE_AS_PLAYED
    }

    private var fftData = ByteArray(0)
    private var waveformData = ByteArray(0)

    override val dataFlow: Flow<AudioAnalyzerData> = callbackFlow {

        visualizer.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                waveform?.let { waveformData = it }
                trySend(AudioAnalyzerData(fftData, waveformData))
            }

            override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                fft?.let { fftData = it }
                trySend(AudioAnalyzerData(fftData, waveformData))
            }
        }, Visualizer.getMaxCaptureRate(), true, true)
        visualizer.enabled = true

        awaitClose {
            visualizer.enabled = false
            visualizer.release()
        }
    }
}