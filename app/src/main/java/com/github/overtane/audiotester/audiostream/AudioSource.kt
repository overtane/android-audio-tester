package com.github.overtane.audiotester.audiostream


import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

sealed class AudioSource(open val durationMs: Int) : Parcelable {

    abstract fun nextSamples(size: Int): ShortArray

    @Parcelize
    object Nothing : AudioSource(0) {
        override fun nextSamples(size: Int) = shortArrayOf()
    }

    @Parcelize
    class SineWave(
        val freqHz: Int,
        private val sampleRate: Int,
        private val channelCount: Int,
        override val durationMs: Int
    ) : AudioSource(durationMs) {
        @IgnoredOnParcel
        private val amplitude = AMPLITUDE * Short.MAX_VALUE

        @IgnoredOnParcel
        private val angularFreq = 2 * PI * freqHz / sampleRate.toDouble()

        @IgnoredOnParcel
        private var start: Int = 0 // start phase

        override fun nextSamples(size: Int): ShortArray {
            start = (start + size / channelCount) % sampleRate
            return ShortArray(size) { i ->
                (amplitude * sin(angularFreq * (start + i / channelCount))).toInt().toShort()
            }
        }

        override fun toString(): String {
            return "Sine wave $freqHz Hz ${durationMs.div(1000)} s"
        }

        companion object {
            private const val AMPLITUDE = 1.0
        }
    }

    @Parcelize
    class WhiteNoise(override val durationMs: Int) : AudioSource(durationMs) {
        override fun nextSamples(size: Int) = ShortArray(size) {
            Random.nextInt(-Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        override fun toString(): String {
            return "White noise ${durationMs.div(1000)} s"
        }
    }

    @Parcelize
    class Silence(override val durationMs: Int) : AudioSource(durationMs) {
        override fun nextSamples(size: Int) = ShortArray(size) { 0 }

        override fun toString(): String {
            return "Silence ${durationMs.div(1000)} s"
        }
    }

    @Parcelize
    class AudioBuffer(
        override val durationMs: Int,
        private val samples: ShortArray
    ) : AudioSource(0) {
        @IgnoredOnParcel
        private var cursor: Int = 0
        fun reset() {
            cursor = 0
        }

        override fun nextSamples(size: Int): ShortArray {
            val len = if (cursor + size > samples.size) samples.size - cursor else size
            val subarray = samples.copyOfRange(cursor, cursor + len)
            cursor += size
            return subarray
        }
    }

    @Parcelize
    class Sound(val name: String, val url: String, override val durationMs: Int) :
        AudioSource(durationMs) {
        override fun nextSamples(size: Int) = ShortArray(size) { 0 }

        override fun toString(): String {
            return "Sound ${durationMs.div(1000)} s, $name"
        }
    }
}
