package com.github.overtane.audiotester.audiotrack

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

data class AudioStream(
    val type: AudioType,
    // TODO val source : AudioSource,
    val duration: Int, // TODO move to source! milliseconds
    val sampleRate: Int,
    val channelCount: Int
) {

    val direction: AudioDirection
        get() = when (type) {
            AudioType.ALERT -> AudioDirection.PLAYBACK
            AudioType.ALTERNATE -> AudioDirection.PLAYBACK
            AudioType.DEFAULT -> AudioDirection.DUPLEX
            AudioType.ENTERTAINMENT -> AudioDirection.PLAYBACK
            AudioType.SPEECH_RECOGNITION -> AudioDirection.DUPLEX
            AudioType.TELEPHONY -> AudioDirection.DUPLEX
        }

    private val channelMask: Int
        get() = when (channelCount) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            else -> AudioFormat.CHANNEL_INVALID
        }

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
                    .setChannelMask(channelMask)
                    .build()
            )
            .setBufferSizeInBytes(
                AudioTrack.getMinBufferSize(sampleRate, channelCount, SAMPLE_FORMAT)
            )
            .setPerformanceMode(PERFORMANCE_MODE)
            .setTransferMode(TRANSFER_MODE)
            .build()

    companion object {
        private const val SAMPLE_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val PERFORMANCE_MODE = AudioTrack.PERFORMANCE_MODE_LOW_LATENCY
        private const val TRANSFER_MODE = AudioTrack.MODE_STREAM
    }
}
