package com.github.overtane.audiotester.datastore

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.audiostream.AudioType
import kotlinx.coroutines.withTimeout
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import java.util.concurrent.TimeoutException

private const val BUFFER_TIMEOUT_US: Long = 1000000
private const val TRACK = 0
object Decoder {

    suspend fun decodeMp3(url: String): AudioStream {
        val extractor = MediaExtractor()
        val codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_MPEG)

        withTimeout(10000.toLong()) {
            extractor.apply {
                setDataSource(url)
                selectTrack(TRACK)
            }
        }

        codec.configure(extractor.getTrackFormat(TRACK), null, null, 0)

        Log.d(TAG, "Start decode $url")
        var format = codec.outputFormat
        val samples = mutableListOf<Short>() // this collects decoded samples

        with(codec) {
            start()
            while (true) {
                val hasData = dequeueInputBuffer(BUFFER_TIMEOUT_US).let { id ->
                    if (id < 0) {
                        throw TimeoutException()
                    }
                    getInputBuffer(id)?.let {
                        // extract directly to decoder buffer
                        val n = extractor.readSampleData(it, 0)
                        if (n < 0) {
                            extractor.release()
                            false
                        } else {
                            queueInputBuffer(id, 0, n, 0, 0)
                            extractor.advance()
                        }
                    } ?: false
                }
                if (!hasData) {
                    break
                }
                val info = MediaCodec.BufferInfo()
                dequeueOutputBuffer(info, BUFFER_TIMEOUT_US).let { id ->
                    when {
                        id == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            format = codec.outputFormat
                            Log.d(TAG, "Output format changed")
                        }

                        id < 0 -> {
                            throw TimeoutException()
                        }

                        else -> {
                            getOutputBuffer(id)?.let {
                                samples += shortArrayFromByteBuffer(it).asList()
                                releaseOutputBuffer(id, false)
                            }
                        }
                    }
                }
            }
            stop()
            release()
        }

        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val durationMs = samples.size / sampleRate / channelCount * 1000

        Log.d(TAG, "DONE $sampleRate, $channelCount, $durationMs ms")
        return AudioStream(
            AudioType.ENTERTAINMENT,
            sampleRate,
            channelCount,
            AudioSource.AudioBuffer(durationMs, samples.toShortArray())
        )
    }

    private fun shortArrayFromByteBuffer(byteBuffer: ByteBuffer): ShortArray {
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        val shortArray = ShortArray(byteBuffer.remaining()/2)
        byteBuffer.asShortBuffer().get(shortArray)
        return shortArray
    }

}
