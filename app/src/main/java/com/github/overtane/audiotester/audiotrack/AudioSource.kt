package com.github.overtane.audiotester.audiotrack

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

sealed class AudioSource(val durationMs: Int) {

    val durationOut = String.format("%d", durationMs / 1000)

    abstract fun nextSamples(size: Int): ShortArray

    class SineWave(
        private val freqHz: Int,
        private val sampleRate: Int,
        private val channelCount: Int,
        durationMs: Int
    ) : AudioSource(durationMs) {
        private val amplitude = AMPLITUDE * Short.MAX_VALUE
        private val angularFreq = 2 * PI * freqHz / sampleRate.toDouble()
        private var start: Int = 0 // start phase
        override fun nextSamples(size: Int): ShortArray {
            start = (start + size / channelCount) % sampleRate
            return ShortArray(size) { i ->
                (amplitude * sin(angularFreq * (start + i / channelCount))).toInt().toShort()
            }
        }

        override fun toString(): String {
            return "Sine wave $freqHz Hz $durationOut s"
        }

        companion object {
            private const val AMPLITUDE = 1.0
        }
    }

    class WhiteNoise(durationMs: Int) : AudioSource(durationMs) {
        override fun nextSamples(size: Int) = ShortArray(size) {
            Random.nextInt(-Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        override fun toString(): String {
            return "White noise $durationOut s"
        }
    }

    class Silence(durationMs: Int) : AudioSource(durationMs) {
        override fun nextSamples(size: Int) = ShortArray(size) { 0 }

        override fun toString(): String {
            return "Silence $durationOut s"
        }
    }

    class SpeechSample() : AudioSource(0) {
        override fun nextSamples(size: Int): ShortArray {
            return ShortArray(0)
        }
    }

    class AudioSample() : AudioSource(0) {
        override fun nextSamples(size: Int): ShortArray {
            return ShortArray(0)
        }
    }
}
