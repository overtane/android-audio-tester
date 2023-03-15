package com.github.overtane.audiotester.audiotrack

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

sealed class AudioSource(val durationMs: Int) {

    abstract fun nextSamples(size: Int): ShortArray

    class SineWave(val frequencyHz: Int, private val sampleRate: Int, durationMs: Int) :
        AudioSource(durationMs) {
        private val amplitude = AMPLITUDE * Short.MAX_VALUE
        private val angularFreq = 2 * PI * frequencyHz
        private var n : Int = 0
        override fun nextSamples(size: Int): ShortArray {
            n = (n + size) % sampleRate
            return ShortArray(size) { i ->
                (amplitude * sin(angularFreq * (n + i) / sampleRate)).toInt().toShort()
            }
        }

        override fun toString(): String {
            return "Sine wave ${frequencyHz}Hz, ${durationMs/1000}s"
        }
        companion object {
            private const val AMPLITUDE = 1.0F
        }
    }

    class WhiteNoise(durationMs: Int) : AudioSource(durationMs) {
        override fun nextSamples(size: Int) = ShortArray(size) {
            Random.nextInt(-Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        override fun toString(): String {
            return "White noise ${durationMs/1000}s"
        }
    }

    class Silence(durationMs: Int) : AudioSource(durationMs) {
        override fun nextSamples(size: Int) = ShortArray(size) { 0 }

        override fun toString(): String {
            return "Silence ${durationMs/1000}s"
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
