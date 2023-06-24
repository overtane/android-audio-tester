package com.github.overtane.audiotester.recorder

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecordStat(
    val sampleRate: Int,
    val durationMs: Int,
    val recording: MutableList<Short>
) : Parcelable {

    @IgnoredOnParcel
    var bufferSizeInFrames: Int = 0

    @IgnoredOnParcel
    val totalFrames = sampleRate * durationMs / 1000

    @IgnoredOnParcel
    var deviceId: Int = 0

    @IgnoredOnParcel
    var framesStreamed: Int = 0

    @IgnoredOnParcel
    var latencyMs: Int = 0

    override fun toString(): String {
        val bufferSizeInMs: Int = bufferSizeInFrames / (sampleRate / 1000)
        return "Record: Device $deviceId, Buffer size $bufferSizeInFrames ($bufferSizeInMs ms), " +
                "Latency $latencyMs ms, Frames $framesStreamed"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecordStat

        if (sampleRate != other.sampleRate) return false
        if (durationMs != other.durationMs) return false
        if (bufferSizeInFrames != other.bufferSizeInFrames) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sampleRate
        result = 31 * result + durationMs
        result = 31 * result + bufferSizeInFrames
        return result
    }
}
