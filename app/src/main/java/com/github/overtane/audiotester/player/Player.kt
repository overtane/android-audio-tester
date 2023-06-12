package com.github.overtane.audiotester.player

import android.media.AudioTimestamp
import android.media.AudioTrack
import android.util.Log
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiostream.AudioStream
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class Player(private val stream: AudioStream) {

    private val playback = stream.buildPlayback()
    private lateinit var player: Deferred<Unit>

    private var status =
        PlaybackStat(stream.sampleRate, stream.source.durationMs, playback.bufferSizeInFrames)

    init {
        Log.d(TAG, "Audio format: $stream")
        Log.d(TAG, "Audio source: ${stream.source}")
    }

    fun status() : Flow<PlaybackStat> = flow {
        emit(status)
        do {
            delay((1000 / EMIT_FREQ_HZ).toLong())
            emit(status)
        } while (playback.playState == AudioTrack.PLAYSTATE_PLAYING)
    }

    suspend fun play() = coroutineScope {
        player = async { playLoop() }
        Log.d(TAG, "Playback started")
        player.join()
        Log.d(TAG, "Playback stopped")
        playback.stop()
        playback.release()
    }

    private suspend fun playLoop() {
        val duration = stream.source.durationMs
        // Prime track with one full buffer
        var buf = stream.source.nextSamples(playback.bufferSizeInFrames * playback.channelCount)
        var written = playback.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
        status.framesStreamed += written / playback.channelCount
        playback.play()
        status.deviceId = playback.routedDevice.id
        status.performanceMode = playback.performanceMode
        status.bufferSizeInFrames = playback.bufferSizeInFrames
        Log.d(TAG, "Device id ${playback.routedDevice.id}")
        Log.d(TAG, "Buffer size in samples ${playback.bufferSizeInFrames * playback.channelCount}")
        Log.d(TAG, "Performance mode ${playback.performanceMode}")
        //Log.d(TAG, "Wrote $written samples: ${buf[0]}, ${buf[1]}, ${buf[2]}, ${buf[3]}")

        withTimeout(duration.toLong()) {
            while (isActive) { // is active until cancelled
                buf = stream.source.nextSamples(playback.bufferSizeInFrames * playback.channelCount)
                written = playback.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
                // calculate latency for the first sample we just wrote
                // Note: we should know quite exactly when the first sample was written.
                // Here it is assumed that next frame will be written 'a short while' after latency
                // has been calculated (i.e. on next round)
                // TODO mutual exclusion
                status.framesStreamed += written / playback.channelCount
                status.underruns = playback.underrunCount
                status.latencyMs = latencyMs()
                //Log.d(TAG, "Wrote $written samples: ${buf[0]}, ${buf[1]}, ${buf[2]}, ${buf[3]}")
            }
            Log.d(TAG, "Playback loop exited")
        }
        Log.d(TAG, "Playback time is up")
    }

    fun stop() {
        player.cancel()
    }

    fun isPlaying() : Boolean {
        return playback.playState == AudioTrack.PLAYSTATE_PLAYING
    }

    /**
     * Calculate the current latency between writing a frame to the output stream and
     * the same frame being presented to the audio hardware.
     *
     * Here's how the calculation works:
     *
     * 1) Get the time a particular frame was presented to the audio hardware
     * 2) From this extrapolate the time which the *next* audio frame written to the stream
     *    will be presented
     * 3) Assume that the next audio frame is written at the current time
     * 4) currentLatency = nextFramePresentationTime - nextFrameWriteTime
     *
     */
    private fun latencyMs() : Int {
        val timestamp = AudioTimestamp()
        if (!playback.getTimestamp(timestamp)) {
            return 0
        }
        val writeTimeNs = System.nanoTime() // approximate time when first sample is written
        // Log.d(TAG, "Timestamp ${timestamp.nanoTime}, ${timestamp.framePosition}")
        val frameDelta = status.framesStreamed - timestamp.framePosition
        val frameDeltaNs = (frameDelta * NANOS_PER_SECOND) / playback.sampleRate
        val frameHwTimeNs = timestamp.nanoTime + frameDeltaNs
        val latencyMs = (frameHwTimeNs - writeTimeNs) / NANOS_PER_MILLIS
        //Log.d(TAG,"Calculated latency $latencyMs")
        return latencyMs.toInt()
    }

    companion object {
        private const val EMIT_FREQ_HZ = 5
        private const val NANOS_PER_SECOND = 1000000000
        private const val NANOS_PER_MILLIS = 1000000
    }
}