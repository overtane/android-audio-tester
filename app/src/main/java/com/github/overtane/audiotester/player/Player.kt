package com.github.overtane.audiotester.player

import android.media.AudioTimestamp
import android.media.AudioTrack
import android.util.Log
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioStream
import kotlinx.coroutines.*

class Player(private val stream : AudioStream) {

    private val playback = stream.buildPlayback()
    private lateinit var player : Deferred<Unit>

    suspend fun playAsync() = coroutineScope {
        player = async { play() }
        Log.d(TAG, "Playback started")
        async { waitPlayerFinished() }
    }

    private suspend fun play() {
        val duration = stream.source.durationMs
        Log.d(TAG, "Audio format: $stream")
        Log.d(TAG, "Audio source: ${stream.source}")
        // Prime track with one full buffer
        var buf = stream.source.nextSamples(playback.bufferSizeInFrames * playback.channelCount)
        var written = playback.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
        playback.play()
        Log.d(TAG, "Device id ${playback.routedDevice.id}")
        Log.d(TAG, "Buffer size in samples ${playback.bufferSizeInFrames * playback.channelCount}")
        Log.d(TAG, "Performance mode ${playback.performanceMode}")
        Log.d(TAG, "Channel configuration ${playback.channelConfiguration}, channel count ${playback.channelCount}")
        Log.d(TAG, "Wrote $written samples: ${buf[0]}, ${buf[1]}, ${buf[2]}, ${buf[3]}")
        withTimeout(duration.toLong()) {
            while (isActive) { // is active until cancelled
                buf = stream.source.nextSamples(playback.bufferSizeInFrames  * playback.channelCount)
                written = playback.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
                Log.d(TAG, "Wrote $written samples: ${buf[0]}, ${buf[1]}, ${buf[2]}, ${buf[3]}")
                var timestamp = AudioTimestamp()
                if (playback.getTimestamp(timestamp)) {
                    Log.d(TAG, "Timestamp ${timestamp.nanoTime}, ${timestamp.framePosition}")
                }
            }
        }
    }

    private suspend fun waitPlayerFinished() = runBlocking {
        player.join()
        Log.d(TAG, "Playback stopped")
        playback.stop()
        playback.release()
    }

    fun stop() {
        player.cancel()
    }
}