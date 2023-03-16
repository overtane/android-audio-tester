package com.github.overtane.audiotester.audiotrack

enum class AudioDirection {
    PLAYBACK,
    RECORD,
    DUPLEX;

    override fun toString(): String = when (this) {
            PLAYBACK -> "playback"
            RECORD -> "record"
            DUPLEX -> "playback & record"
    }
}