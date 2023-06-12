package com.github.overtane.audiotester.recorder

data class RecordStat(val sampleRate: Int, val durationMs: Int, var bufferSizeInFrames: Int) {
    val totalFrames = sampleRate * durationMs / 1000
    var deviceId: Int = 0
    var framesStreamed: Int = 0
    var latencyMs: Int = 0

    override fun toString(): String {
        val bufferSizeInMs: Int = bufferSizeInFrames / (sampleRate / 1000)
        return "Record: Device $deviceId, Buffer size $bufferSizeInFrames ($bufferSizeInMs ms), " +
                "Latency $latencyMs ms, Frames $framesStreamed"
    }
}
