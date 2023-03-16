package com.github.overtane.audiotester.player

import android.media.AudioTrack

data class StreamInfo(val totalFrames: Int, var bufferSizeInFrames: Int) {
    var deviceId: Int = 0
    var performanceMode: Int = 0
    var framesStreamed: Int = 0
    var latencyMs: Double = 0.0
    var underruns: Int = 0

    override fun toString(): String {
        return "Fast track ${fastTrack()}, Buffer size $bufferSizeInFrames, Device $deviceId\n" +
                "Latency avg ${latencyMs.toInt()} ms, Underruns ${underruns}\n" +
                "Frames $framesStreamed"
    }

    private fun fastTrack(): String =
        if (performanceMode == AudioTrack.PERFORMANCE_MODE_LOW_LATENCY) "YES" else "NO"
}
