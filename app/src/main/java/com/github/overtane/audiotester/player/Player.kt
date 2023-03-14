package com.github.overtane.audiotester.player

import android.media.AudioTrack
import android.util.Log
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioStream
import kotlinx.coroutines.*
import kotlin.random.Random

class Player(private val stream : AudioStream) {

    private val playback = stream.buildPlayback()
    private lateinit var player : Deferred<Unit>

    suspend fun playAsync() = coroutineScope {
        player = async { play() }
        Log.d(TAG, "Playback started")
        async { waitPlayerFinished() }
    }

    private suspend fun play() {
        Log.d(TAG, "Starting audio track for ${stream.duration/1000} seconds")
        // Prime track with one buffer
        // TODO AudioSource.start(buf.size)
        val buf = ShortArray(playback.bufferSizeInFrames)
            { Random.nextInt(-Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort() }
        val written = playback.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
        playback.play()
        Log.d(TAG, "Device id ${playback.routedDevice.id}")
        Log.d(TAG, "Performance mode ${playback.performanceMode}")
        withTimeout(stream.duration.toLong()) {
            while (isActive) {
                // TODO AudioSource.next(buf.size)
                val buf = ShortArray(playback.bufferSizeInFrames)
                    { Random.nextInt(-Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort() }
                val written = playback.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
                //Log.d(TAG, "Wrote $written samples")
            }
        }
    }

    suspend fun waitPlayerFinished() = runBlocking {
        player.join()
        Log.d(TAG, "Playback stopped")
        playback.stop()
        playback.release()
    }

    fun stop() {
        player.cancel()
    }
}