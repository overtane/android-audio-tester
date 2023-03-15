package com.github.overtane.audiotester.ui

import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioSource
import com.github.overtane.audiotester.audiotrack.AudioType
import com.github.overtane.audiotester.audiotrack.AudioStream
import com.github.overtane.audiotester.player.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class MainViewModel : ViewModel() {

    private var _mainAudioStream = MutableLiveData<AudioStream>()
    val mainAudioStream
        get() = _mainAudioStream

    private var _altAudioStream = MutableLiveData<AudioStream>()
    val altAudioStream
        get() = _altAudioStream

    private var mainPlayer: Player? = null
    private var altPlayer: Player? = null

    init {
        val mainAudioSource = AudioSource.WhiteNoise(10000)
        _mainAudioStream.value = AudioStream(AudioType.ENTERTAINMENT, mainAudioSource, 48000, 1)
        val altAudioSource = AudioSource.SineWave(800, 48000, 2, 10000)
        _altAudioStream.value = AudioStream(AudioType.ALTERNATE, altAudioSource, 48000, 2)
    }

    fun onButtonClicked(v: View) {
        v.isSelected = !v.isSelected
        val b = v as Button
        Log.d(TAG, "${b.text} state is ${b.isSelected}")
        if (v.id == R.id.button_primary_audio_play_pause) {
            if (v.isSelected) {
                mainPlayer = Player(mainAudioStream.value!!)
                viewModelScope.async(Dispatchers.IO) { mainPlayer?.playAsync() }
            } else {
                mainPlayer?.stop()
            }
        }
        if (v.id == R.id.button_secondary_audio_play_pause) {
            if (v.isSelected) {
                altPlayer = Player(altAudioStream.value!!)
                viewModelScope.async(Dispatchers.IO) { altPlayer?.playAsync() }
            } else {
                altPlayer?.stop()
            }
        }
    }

    fun onMainAudioClicked() {
        Log.d(TAG, "Main audio clicked")
    }

    fun onAltAudioClicked() {
        Log.d(TAG, "Alt audio clicked")
    }
}