package org.github.overtane.soundbrowser.freesound

import kotlinx.serialization.Serializable

@Serializable
data class FreeSoundSearchResult(
    val id: Int,
    val name: String,
    val duration: Double,
    val samplerate: Double,
    val images: FreeSoundImageUlrs
)