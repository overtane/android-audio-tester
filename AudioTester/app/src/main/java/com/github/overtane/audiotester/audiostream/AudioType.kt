package com.github.overtane.audiotester.audiostream

enum class AudioType {
    ALERT,
    ALTERNATE,
    DEFAULT,
    ENTERTAINMENT,
    SPEECH_RECOGNITION,
    TELEPHONY;

    override fun toString() : String = when (this) {
        ALERT -> "Alert"
        ALTERNATE -> "Alt"
        DEFAULT -> "Default"
        ENTERTAINMENT -> "Media"
        SPEECH_RECOGNITION -> "Speech"
        TELEPHONY -> "Telephony"
    }
}