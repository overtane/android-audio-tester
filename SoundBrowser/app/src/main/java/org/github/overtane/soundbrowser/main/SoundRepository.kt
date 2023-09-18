package org.github.overtane.soundbrowser.main

import android.util.Log
import org.github.overtane.soundbrowser.freesound.FreeSoundHttpClient
import kotlinx.coroutines.flow.MutableStateFlow

private const val DEFAULT_QUERY = "piano"
object SoundRepository {

    val query: MutableStateFlow<String> = MutableStateFlow(DEFAULT_QUERY)
    val message: MutableStateFlow<String> = MutableStateFlow("")

    fun soundPagingSource() = SoundPagingSource(FreeSoundHttpClient, query, message)

    suspend fun getSound(id: Int) = runCatching {
        FreeSoundHttpClient.getSound(id)
    }.onFailure { e ->
        Log.d("SoundRepository", "Exception: $e")
    }
}
