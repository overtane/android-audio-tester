package com.github.overtane.audiotester.datastore

import android.util.Log
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.ui.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SoundRepository(preferencesRepository: PreferencesRepository) {

    enum class DecodeState {
        INVALID,
        DECODING,
        DECODED,
        READY,
        ERROR,
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    val decodeState: MutableStateFlow<DecodeState> = MutableStateFlow(DecodeState.INVALID)
    val sound: MutableStateFlow<Pair<String?,AudioStream?>> = MutableStateFlow(Pair(null,null))

    init {
        Log.d(TAG, "Decode Sound!")
        decodeStream(preferencesRepository.get()[MainViewModel.EXT_AUDIO])
    }

    fun decodeStream(stream: AudioStream) {
        with(stream) {
            when (source is AudioSource.Sound) {
                true -> {
                    if (needDecoding(source)) {
                        decodeState.update { DecodeState.DECODING }
                        coroutineScope.launch {
                            doDecode(source.preview)
                        }
                    }
                }

                false -> decodeState.update { DecodeState.INVALID }
            }
        }
    }

    private suspend fun doDecode(url: String) = runCatching {
        sound.update { Pair(url, Decoder.decodeMp3(url)) }
        }.onFailure {
            decodeState.update { DecodeState.ERROR }
        }.onSuccess {
            decodeState.update { DecodeState.DECODED }
            delay(100)
            decodeState.update { DecodeState.READY }
        }

    private fun needDecoding(source: AudioSource.Sound): Boolean {
        val state = decodeState.value
        val url = sound.value.first
        return !(url == source.preview && state == DecodeState.READY)
    }


}
