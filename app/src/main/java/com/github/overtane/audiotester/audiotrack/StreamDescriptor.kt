package com.github.overtane.audiotester.audiotrack

import android.media.AudioAttributes
import android.media.AudioFormat

data class StreamDescriptor(
    val type : AudioType,
    // TODO val source : AudioSource,
    val duration : Float,
    val sampleRate : Int,
    val channelCount : Int
) {

    val direction : Direction
        get() = when (type) {
            AudioType.ALERT -> Direction.PLAYBACK
            AudioType.ALTERNATE -> Direction.PLAYBACK
            AudioType.DEFAULT -> Direction.DUPLEX
            AudioType.ENTERTAINMENT -> Direction.PLAYBACK
            AudioType.SPEECH_RECOGNITION -> Direction.DUPLEX
            AudioType.TELEPHONY -> Direction.DUPLEX
        }

    val channelMask : Int
        get() = when (channelCount) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            else -> AudioFormat.CHANNEL_INVALID
    }

    val usage : Int
        get() = when (type) {
            AudioType.ALERT -> AudioAttributes.USAGE_NOTIFICATION_RINGTONE
            AudioType.ALTERNATE -> AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE
            AudioType.DEFAULT -> AudioAttributes.USAGE_UNKNOWN
            AudioType.ENTERTAINMENT -> AudioAttributes.USAGE_MEDIA
            AudioType.SPEECH_RECOGNITION -> AudioAttributes.USAGE_ASSISTANT
            AudioType.TELEPHONY -> AudioAttributes.USAGE_VOICE_COMMUNICATION
        }

    val contentType : Int
        get() = when (type) {
            AudioType.ALERT -> AudioAttributes.CONTENT_TYPE_SONIFICATION
            AudioType.ALTERNATE -> AudioAttributes.CONTENT_TYPE_SPEECH
            AudioType.DEFAULT -> AudioAttributes.CONTENT_TYPE_UNKNOWN
            AudioType.ENTERTAINMENT -> AudioAttributes.CONTENT_TYPE_MUSIC
            AudioType.SPEECH_RECOGNITION -> AudioAttributes.CONTENT_TYPE_SPEECH
            AudioType.TELEPHONY -> AudioAttributes.CONTENT_TYPE_SPEECH
        }
}
