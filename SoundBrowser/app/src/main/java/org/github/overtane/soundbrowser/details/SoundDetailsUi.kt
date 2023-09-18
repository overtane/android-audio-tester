package org.github.overtane.soundbrowser.details

import org.github.overtane.soundbrowser.freesound.FreeSoundDetailsResult

data class SoundDetailsUi(
    val id: Int,
    val name: String,
    val url: String,
    val duration: String,
    val samplerate: String,
    val channels: String,
    val bitdepth: String,
    val username: String,
    val previewUrl: String
) {
    constructor(freeSoundResult: FreeSoundDetailsResult) : this(
        id = freeSoundResult.id,
        name = freeSoundResult.name,
        url = freeSoundResult.url,
        duration = freeSoundResult.duration.toInt().toString(),
        samplerate = freeSoundResult.samplerate.toInt().toString(),
        channels = freeSoundResult.channels.toString(),
        bitdepth = freeSoundResult.bitdepth.toString(),
        username = freeSoundResult.username,
        previewUrl = freeSoundResult.previews.preview_lq_mp3
    )
}
