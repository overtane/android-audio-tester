package org.github.overtane.soundbrowser.freesound

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FreeSoundPreviews(
    @SerialName("preview-hq-mp3") val preview_hq_mp3: String,
    @SerialName("preview-lq-mp3") val preview_lq_mp3: String,
    @SerialName("preview-hq-ogg") val preview_hq_ogg: String,
    @SerialName("preview-lq-ogg") val preview_lq_ogg: String
)
