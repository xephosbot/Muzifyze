package com.xbot.musifyze.ui.features.radio

data class RadioScreenState(
    val fft: ByteArray = ByteArray(0),
    val waveform: ByteArray = ByteArray(0)
) {
    override fun equals(other: Any?): Boolean = false

    override fun hashCode(): Int {
        var result = fft.contentHashCode()
        result = 31 * result + waveform.contentHashCode()
        return result
    }
}