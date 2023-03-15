package com.github.overtane.audiotester.player

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
        // Prime track with one buffer
        val buf = stream.source.nextSamples(playback.bufferSizeInFrames)
        val written = playback.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
        playback.play()
        Log.d(TAG, "Device id ${playback.routedDevice.id}")
        Log.d(TAG, "Performance mode ${playback.performanceMode}")
        withTimeout(duration.toLong()) {
            while (isActive) {
                val buf = stream.source.nextSamples(playback.bufferSizeInFrames)
                val written = playback.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
                //Log.d(TAG, "Wrote $written samples")
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