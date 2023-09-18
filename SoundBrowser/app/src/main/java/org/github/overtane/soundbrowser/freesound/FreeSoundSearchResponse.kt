package org.github.overtane.soundbrowser.freesound

import kotlinx.serialization.Serializable

@Serializable
data class FreeSoundSearchResponse(
    val count: Int,
    val previous: String?,
    val next: String?,
    val results: List<FreeSoundSearchResult>,
)

