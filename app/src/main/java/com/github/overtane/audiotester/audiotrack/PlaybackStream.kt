package com.github.overtane.audiotester.audiotrack

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import com.github.overtane.audiotester.TAG
import kotlin.random.Random


class PlaybackStream ( private val stream : StreamDescriptor ) {

    private val minBuffSize =
        AudioTrack.getMinBufferSize(stream.sampleRate, stream.channelCount, SAMPLE_FORMAT)

    private val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(stream.usage)
                .setContentType(stream.contentType)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(SAMPLE_FORMAT)
                .setSampleRate(stream.sampleRate)
                .setChannelMask(stream.channelMask)
                .build()
        )
        .setBufferSizeInBytes(minBuffSize)
        .setPerformanceMode(PERFORMANCE_MODE)
        .setTransferMode(TRANSFER_MODE)
        .build()

    fun play(seconds : Int) {
        Log.d(TAG, "Starting audio track for $seconds seconds")
        // White noise
        val buf = ShortArray(track.bufferSizeInFrames)
            { Random.nextInt(-Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort() }
        val written = track.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
        track.flush()
        track.play()
        Log.d(TAG, "Device id ${track.routedDevice.id}")
        Log.d(TAG, "Performance mode ${track.performanceMode}")
        // TODO Adjust round to seconds
        repeat(seconds - 1) {
            val buf = ShortArray(track.bufferSizeInFrames)
                { Random.nextInt(-Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort() }
            val written = track.write(buf, 0, buf.size, AudioTrack.WRITE_BLOCKING)
            //Log.d(TAG, "Wrote $written samples")
        }
    }

    fun stop() {
        Log.d(TAG, "Stopping audio track")
        track.stop()
        track.release()
    }

    companion object {
        private const val SAMPLE_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val PERFORMANCE_MODE = AudioTrack.PERFORMANCE_MODE_LOW_LATENCY
        private const val TRANSFER_MODE = AudioTrack.MODE_STREAM
    }

}
