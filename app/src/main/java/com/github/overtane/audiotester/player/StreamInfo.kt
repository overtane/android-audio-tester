package com.github.overtane.audiotester.player

import android.media.AudioTrack

data class StreamInfo(val sampleRate : Int, val durationMs : Int, var bufferSizeInFrames: Int) {
    val totalFrames = sampleRate * durationMs / 1000
    var deviceId: Int = 0
    var performanceMode: Int = 0
    var framesStreamed: Int = 0
    var latencyMs: Int = 0
    var underruns: Int = 0

    override fun toString(): String {
        val bufferSizeInMs : Int = bufferSizeInFrames / (sampleRate / 1000)
        return "Device $deviceId, Fast track ${fastTrack()}, Buffer size $bufferSizeInFrames ($bufferSizeInMs ms), " +
                "Underruns $underruns, Latency $latencyMs ms"
    }

    private fun fastTrack(): String =
        if (performanceMode == AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) "YES" else "NO"
}
