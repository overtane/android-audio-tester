package com.github.overtane.audiotester.ui

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioSource
import com.github.overtane.audiotester.audiotrack.AudioStream
import com.github.overtane.audiotester.audiotrack.AudioType
import com.github.overtane.audiotester.player.Player
import com.github.overtane.audiotester.player.StreamInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private var _audioStream = MutableLiveData<MutableList<AudioStream>>()
    val audioStream
        get() = _audioStream

    private var _audioInfoMain = MutableLiveData<StreamInfo?>()
    val audioInfoMain
        get() = _audioInfoMain

    private var _audioInfoAlt = MutableLiveData<StreamInfo?>()
    val audioInfoAlt
        get() = _audioInfoAlt

    private var player: MutableList<Player?> = mutableListOf(null, null)

    init {
        _audioStream.value = mutableListOf(DEFAULT_MAIN_STREAM, DEFAULT_ALT_STREAM)
    }

    fun onButtonClicked(view: View) {
        view.isSelected = !view.isSelected
        when (view.isSelected) {
            true -> onButtonSelected(view)
            false -> onButtonDeselected(view)
        }
    }

    private fun onButtonSelected(view: View) {
        when (view.id) {
            R.id.button_primary_audio_play_pause -> startAudio(view, MAIN_AUDIO)
            R.id.button_secondary_audio_play_pause -> startAudio(view, ALT_AUDIO)
            else -> Unit
        }
    }

    private fun startAudio(view: View, i: Int) {
        player[i] = Player(audioStream.value?.get(i)!!)
        viewModelScope.async(Dispatchers.IO) { player[i]?.play() }
        viewModelScope.launch {
            player[i]?.status()?.collect { it ->
                when (i) {
                    MAIN_AUDIO -> _audioInfoMain.value = it
                    ALT_AUDIO -> _audioInfoAlt.value = it
                }
            }
            view.isSelected = false
        }
    }

    private fun onButtonDeselected(view: View) {
        when (view.id) {
            R.id.button_primary_audio_play_pause -> player[MAIN_AUDIO]?.stop()
            R.id.button_secondary_audio_play_pause -> player[ALT_AUDIO]?.stop()
            else -> Unit
        }
    }

    fun onMainAudioClicked() {
        Log.d(TAG, "Main audio clicked")
    }

    fun onAltAudioClicked() {
        Log.d(TAG, "Alt audio clicked")
    }

    companion object {
        private const val MAIN_AUDIO = 0
        private const val ALT_AUDIO = 1

        private const val DEFAULT_DURATION_MS = 10000
        private const val DEFAULT_SAMPLE_RATE = 48000
        private const val DEFAULT_MAIN_CHANNEL_COUNT = 1
        private const val DEFAULT_ALT_CHANNEL_COUNT = 2

        private val DEFAULT_MAIN_SOURCE = AudioSource.WhiteNoise(DEFAULT_DURATION_MS)
        private val DEFAULT_ALT_SOURCE =
            AudioSource.SineWave(
                800,
                DEFAULT_SAMPLE_RATE,
                DEFAULT_ALT_CHANNEL_COUNT,
                DEFAULT_DURATION_MS
            )

        private val DEFAULT_MAIN_STREAM =
            AudioStream(
                AudioType.ENTERTAINMENT,
                DEFAULT_MAIN_SOURCE,
                DEFAULT_SAMPLE_RATE,
                DEFAULT_MAIN_CHANNEL_COUNT
            )
        private val DEFAULT_ALT_STREAM =
            AudioStream(
                AudioType.ALTERNATE,
                DEFAULT_ALT_SOURCE,
                DEFAULT_SAMPLE_RATE,
                DEFAULT_ALT_CHANNEL_COUNT
            )
    }
}