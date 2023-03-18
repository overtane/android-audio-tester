package com.github.overtane.audiotester.ui

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioDirection
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
        _audioStream.value = mutableListOf(INIT_MAIN_STREAM, INIT_ALT_STREAM)
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
            R.id.button_primary_audio_play_pause -> {
                if (audioStream.value?.get(MAIN_AUDIO)?.direction != AudioDirection.PLAYBACK) {

                }
                startAudio(view, MAIN_AUDIO)
            }
            R.id.button_secondary_audio_play_pause -> startAudio(view, ALT_AUDIO)
            else -> Unit
        }
    }

    private fun startAudio(view: View, i: Int) {
        player[i] = Player(audioStream.value?.get(i)!!)
        viewModelScope.async(Dispatchers.IO) { player[i]?.play() }
        viewModelScope.launch {
            player[i]?.status()?.collect { it -> updateInfo(i, it) }
            view.isSelected = false
        }
    }

    private fun updateInfo(i: Int, info: StreamInfo) = when (i) {
        MAIN_AUDIO -> _audioInfoMain.value = info
        ALT_AUDIO -> _audioInfoAlt.value = info
        else -> Unit
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

        private const val INIT_DURATION_MS = 10000
        private const val INIT_MAIN_SAMPLE_RATE = 8000
        private const val INIT_ALT_SAMPLE_RATE = 48000
        private const val INIT_MAIN_CHANNEL_COUNT = 2
        private const val INIT_ALT_CHANNEL_COUNT = 1

        private val INIT_MAIN_SOURCE = AudioSource.WhiteNoise(INIT_DURATION_MS)
        private val INIT_ALT_SOURCE =
            AudioSource.SineWave(
                800,
                INIT_ALT_SAMPLE_RATE,
                INIT_ALT_CHANNEL_COUNT,
                INIT_DURATION_MS
            )

        private val INIT_MAIN_STREAM =
            AudioStream(
                AudioType.ENTERTAINMENT,
                INIT_MAIN_SOURCE,
                INIT_MAIN_SAMPLE_RATE,
                INIT_MAIN_CHANNEL_COUNT
            )
        private val INIT_ALT_STREAM =
            AudioStream(
                AudioType.ALTERNATE,
                INIT_ALT_SOURCE,
                INIT_ALT_SAMPLE_RATE,
                INIT_ALT_CHANNEL_COUNT
            )
    }
}