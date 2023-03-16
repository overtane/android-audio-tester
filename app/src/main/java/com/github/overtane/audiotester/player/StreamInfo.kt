package com.github.overtane.audiotester.player

data class StreamInfo(val totalFrames : Int, var bufferSizeInFrames : Int) {
    var deviceId : Int = 0
    var performanceMode : Int = 0
    var framesStreamed : Int = 0
    var latencyMs : Double = 0.0

    override fun toString() : String {
        return framesStreamed.toString()
    }
}
