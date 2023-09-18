package org.github.overtane.soundbrowser.freesound

import kotlinx.serialization.Serializable

@Serializable
data class FreeSoundImageUlrs(
    val waveform_m: String,
    val waveform_l: String,
    val spectral_m: String,
    val spectral_l: String,
    val waveform_bw_m: String,
    val waveform_bw_l: String,
    val spectral_bw_m: String,
    val spectral_bw_l: String
)
