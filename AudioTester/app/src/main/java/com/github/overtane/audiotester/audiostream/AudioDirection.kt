package com.github.overtane.audiotester.audiostream

enum class AudioDirection {
    PLAYBACK,
    RECORD,
    FULL_DUPLEX;

    override fun toString(): String = when (this) {
            PLAYBACK -> "playback"
            RECORD -> "record"
            FULL_DUPLEX -> "playback & record"
    }
}