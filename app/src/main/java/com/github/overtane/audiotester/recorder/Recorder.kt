package com.github.overtane.audiotester.recorder

import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiostream.AudioStream
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Recorder(private val stream: AudioStream) {

    private val record = stream.buildRecord()
    private lateinit var recorder: Deferred<Unit>

    private val samples = mutableListOf<Short>()
    private val status =
        RecordStat(stream.sampleRate, stream.source.durationMs, samples)

    init {
        Log.d(TAG, "Audio format: $stream")
        Log.d(TAG, "Audio source: ${stream.source}")
    }

    fun status() : Flow<RecordStat> = flow {
        emit(status)
        do {
            delay((1000 / EMIT_FREQ_HZ).toLong())
            emit(status)
        } while (record.recordingState == AudioRecord.RECORDSTATE_RECORDING)
    }

    suspend fun record() = coroutineScope {
        recorder = async { recordLoop() }
        Log.d(TAG, "Record started")
        recorder.join()
        Log.d(TAG, "Record stopped")
        record.stop()
        record.release()
    }

    private suspend fun recordLoop() {
        val duration = stream.source.durationMs
        val buf = ShortArray(record.bufferSizeInFrames * record.channelCount * 2)
        record.startRecording()
        status.deviceId = record.routedDevice.id
        status.bufferSizeInFrames = record.bufferSizeInFrames
        Log.d(TAG, "Device id ${status.deviceId}")
        Log.d(TAG, "Buffer size in samples ${buf.size}")

        withTimeout(duration.toLong()) {
            while (isActive) { // is active until cancelled
                // Note: Read returns number of bytes
                val read = record.read(buf, 0, buf.size)
                status.framesStreamed += read / record.channelCount
                status.latencyMs = latencyMs()
                status.recording += buf.asList()
                //Log.d(TAG, "Wrote $read samples: ${buf[0]}, ${buf[1]}, ${buf[2]}, ${buf[3]}")
            }
            Log.d(TAG, "Record loop exited")
        }
        Log.d(TAG, "Record time is up")
    }

    fun stop() {
        recorder.cancel()
    }

    fun isRecording() : Boolean {
        return record.recordingState == AudioRecord.RECORDSTATE_RECORDING
    }

    private fun latencyMs() = 42

    companion object {
        private const val EMIT_FREQ_HZ = 5
    }
}