package com.github.overtane.audiotester.datastore

import android.util.Log
import com.github.overtane.audiotester.MainActivity
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
        DOWNLOADING,
        DECODING,
        DECODED,
        READY,
        ERROR,
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    val decodeState: MutableStateFlow<DecodeState> = MutableStateFlow(DecodeState.INVALID)
    val sound: MutableStateFlow<AudioStream?> = MutableStateFlow(null)

    init {
        Log.d(TAG, "Decode Sound!")
        decodeStream(preferencesRepository.get()[MainViewModel.EXT_AUDIO])
    }

    fun decodeStream(stream: AudioStream) {
        with(stream) {
            when (source is AudioSource.Sound) {
                true -> {
                    decodeState.update { DecodeState.DECODING }
                    coroutineScope.launch {
                        doDecode(source.preview)
                    }
                }

                false -> decodeState.update { DecodeState.INVALID }
            }
        }
    }

    private suspend fun doDecode(url: String) {
        sound.update { Decoder.decodeMp3(url) }
        decodeState.update { DecodeState.DECODED }
        delay(100)
        decodeState.update { DecodeState.READY }
    }

}