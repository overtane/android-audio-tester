package com.github.overtane.audiotester.player

import android.media.AudioTimestamp
import android.media.AudioTrack
import android.util.Log
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioStream
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class Player(private val stream: AudioStream) {

    private val playback = stream.buildPlayback()
    private lateinit var player: Deferred<Unit>

    private var status =
        StreamInfo(stream.sampleRate * stream.source.durationMs / 1000, playback.bufferSizeInFrames)

    init {
        Log.d(TAG, "Audio format: $stream")
        Log.d(TAG, "Audio source: ${stream.source}")
    }

    fun status() : Flow<StreamInfo> = flow {
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
        status.framesStreamed += written
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
                // TODO mutual exclusion
                status.framesStreamed += written / playback.channelCount
                status.underruns = playback.underrunCount
                //Log.d(TAG, "Wrote $written samples: ${buf[0]}, ${buf[1]}, ${buf[2]}, ${buf[3]}")
                //var timestamp = AudioTimestamp()
                // TODO calculate latency
                //if (playback.getTimestamp(timestamp)) {
                //    Log.d(TAG, "Timestamp ${timestamp.nanoTime}, ${timestamp.framePosition}")
                //}
            }
            Log.d(TAG, "Playback loop exited")
        }
        Log.d(TAG, "Playback time is up")
    }

    fun stop() {
        player.cancel()
    }

    companion object {
        private const val EMIT_FREQ_HZ = 5
    }
}