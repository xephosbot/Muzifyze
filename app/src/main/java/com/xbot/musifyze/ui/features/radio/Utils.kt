package com.xbot.musifyze.ui.features.radio

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlin.experimental.and
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.log10
import kotlin.math.min

@Composable
fun rememberAudioVisualizerState() = remember {
    AudioVisualizerState()
}

class AudioVisualizerState {
    private val _bitmap by mutableStateOf(Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888))
    val bitmap: Bitmap get() = _bitmap

    private var magnitudes = floatArrayOf()
    private val maxMagnitude = calculateMagnitude(128f, 128f)

    @Composable
    fun UpdateBitmap(fft: ByteArray, waveform: ByteArray) {
        val canvas = remember(bitmap) { Canvas(bitmap) }
        val paint = remember(bitmap) { Paint() }

        magnitudes = fftToMagnitudes(fft)

        if (magnitudes.isEmpty())
            return

        bitmap.eraseColor(Color.TRANSPARENT)

        Log.e("TAGGGGGGG", waveform.map { (it.toInt() and 0xFF) }.joinToString(" "))

        for (i in 0 until min(BITMAP_WIDTH, magnitudes.size)) {
            paint.color = Color.rgb(magnitudes[i].toInt(), 0, 0)
            canvas.drawPoint(i.toFloat(), 1f, paint)

            paint.color = Color.rgb((waveform[i].toInt() and 0xFF), 0, 0)
            canvas.drawPoint(i.toFloat(), 0f, paint)
        }
    }

    private fun fftToMagnitudes(fft: ByteArray): FloatArray {
        if (fft.isEmpty())
            return floatArrayOf()

        val n = fft.size
        val currentMagnitudes = FloatArray(n / 2 + 1)
        currentMagnitudes[0] = abs((fft[0] and 0xFF.toByte()).toFloat())
        currentMagnitudes[n / 2] = abs((fft[0] and 0xFF.toByte()).toFloat())

        for (k in 1 until n / 2) {
            val index = k * 2
            val real = (fft[index] and 0xFF.toByte()).toFloat()
            val imaginary = (fft[index + 1] and 0xFF.toByte()).toFloat()
            currentMagnitudes[k] = 100 * log10(hypot(real, imaginary))
        }

        return currentMagnitudes
    }

    private fun calculateMagnitude(r: Float, i: Float): Float {
        return if (i == 0f && r == 0f) 0f else 10 * log10(hypot(r, i))
    }

    companion object {
        const val BITMAP_WIDTH = 512
        const val BITMAP_HEIGHT = 2
    }
}
