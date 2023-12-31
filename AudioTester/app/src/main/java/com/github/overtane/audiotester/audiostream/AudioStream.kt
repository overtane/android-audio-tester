package com.github.overtane.audiotester.audiostream


import android.annotation.SuppressLint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioStream(
    val type: AudioType,
    val sampleRate: Int,
    val channelCount: Int,
    val source: AudioSource,
) : Parcelable {

    val direction: AudioDirection
        get() = when (type) {
            AudioType.ALERT -> AudioDirection.PLAYBACK
            AudioType.ALTERNATE -> AudioDirection.PLAYBACK
            AudioType.DEFAULT -> AudioDirection.FULL_DUPLEX
            AudioType.ENTERTAINMENT -> AudioDirection.PLAYBACK
            AudioType.SPEECH_RECOGNITION -> AudioDirection.FULL_DUPLEX
            AudioType.TELEPHONY -> AudioDirection.FULL_DUPLEX
        }

    val playbackChannelMask: Int
        get() = when (channelCount) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            else -> AudioFormat.CHANNEL_INVALID
        }

    private val recordChannelMask: Int
        get() = when (channelCount) {
            1 -> AudioFormat.CHANNEL_IN_MONO
            2 -> AudioFormat.CHANNEL_IN_STEREO
            else -> AudioFormat.CHANNEL_INVALID }

    private val usage: Int
        get() = when (type) {
            AudioType.ALERT -> AudioAttributes.USAGE_NOTIFICATION_RINGTONE
            AudioType.ALTERNATE -> AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE
            AudioType.DEFAULT -> AudioAttributes.USAGE_UNKNOWN
            AudioType.ENTERTAINMENT -> AudioAttributes.USAGE_MEDIA
            AudioType.SPEECH_RECOGNITION -> AudioAttributes.USAGE_ASSISTANT
            AudioType.TELEPHONY -> AudioAttributes.USAGE_VOICE_COMMUNICATION
        }

    private val contentType: Int
        get() = when (type) {
            AudioType.ALERT -> AudioAttributes.CONTENT_TYPE_SONIFICATION
            AudioType.ALTERNATE -> AudioAttributes.CONTENT_TYPE_SPEECH
            AudioType.DEFAULT -> AudioAttributes.CONTENT_TYPE_UNKNOWN
            AudioType.ENTERTAINMENT -> AudioAttributes.CONTENT_TYPE_MUSIC
            AudioType.SPEECH_RECOGNITION -> AudioAttributes.CONTENT_TYPE_SPEECH
            AudioType.TELEPHONY -> AudioAttributes.CONTENT_TYPE_SPEECH
        }

    fun buildPlayback() =
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(contentType)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(SAMPLE_FORMAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(playbackChannelMask)
                    .build()
            )
            .setBufferSizeInBytes(
                AudioTrack.getMinBufferSize(sampleRate, playbackChannelMask, SAMPLE_FORMAT)
            )
            .setPerformanceMode(PERFORMANCE_MODE)
            .setTransferMode(TRANSFER_MODE)
            .build()

    @SuppressLint("MissingPermission") // Record permission is checked in viewmodel
    fun buildRecord(): AudioRecord =
        AudioRecord.Builder()
            // TODO audio source may be different for different streams
            .setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(SAMPLE_FORMAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(recordChannelMask)
                    .build()
            )
            .setBufferSizeInBytes(
                AudioRecord.getMinBufferSize(sampleRate, recordChannelMask, SAMPLE_FORMAT)
            )
            .build()

    override fun toString(): String {
        val ch = when (channelCount) {
            1 -> "mono"
            2 -> "stereo"
            else -> "no ch"
        }
        val rem = sampleRate % 1000
        val sr = if (rem == 0) (sampleRate / 1000).toString() else String.format(
            "%.1f",
            sampleRate / 1000F
        )
        return "$type $sr kHz, ${ch}, $direction"
    }

    companion object {
        private const val SAMPLE_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val PERFORMANCE_MODE = AudioTrack.PERFORMANCE_MODE_LOW_LATENCY
        private const val TRANSFER_MODE = AudioTrack.MODE_STREAM
    }
}
