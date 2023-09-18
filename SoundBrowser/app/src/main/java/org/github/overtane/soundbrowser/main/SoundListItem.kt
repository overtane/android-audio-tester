package org.github.overtane.soundbrowser.main

import org.github.overtane.soundbrowser.freesound.FreeSoundSearchResult

data class SoundListItem(
    val id: Int,
    val name: String,
    val samplerate: String,
    val duration: String,
    val imageUrl: String
) {
    constructor(freeSoundItem: FreeSoundSearchResult) : this(
        id = freeSoundItem.id,
        name = freeSoundItem.name,
        samplerate = freeSoundItem.samplerate.toInt().toString() + " Hz, ",
        duration = freeSoundItem.duration.toInt().toString() + " s",
        imageUrl = freeSoundItem.images.waveform_m
    )
}
