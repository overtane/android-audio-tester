package com.github.overtane.audiotester.audiotrack

enum class AudioDirection {
    PLAYBACK,
    RECORD,
    DUPLEX;

    override fun toString(): String {
        return super.toString().lowercase()
    }
}