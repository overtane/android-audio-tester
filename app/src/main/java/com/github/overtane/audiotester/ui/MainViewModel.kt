package com.github.overtane.audiotester.ui

import android.util.Log
import android.view.View
import android.widget.Button
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

    private var main_player: Player? = null
    private var alt_player: Player? = null
    private val main_stream =
        AudioStream(AudioType.ENTERTAINMENT, AudioSource.WhiteNoise(10000), 48000, 1)
        //AudioStream(AudioType.ENTERTAINMENT, AudioSource.SineWave(400, 8000, 1, 10000), 8000, 1)
    private val alt_stream =
        //AudioStream(AudioType.ALTERNATE, AudioSource.WhiteNoise(10000), 48000, 1)
        AudioStream(AudioType.ALTERNATE, AudioSource.SineWave(800, 48000, 2, 10000), 48000, 2)

    fun onButtonClicked(v: View) {
        v.isSelected = !v.isSelected
        val b = v as Button
        Log.d(TAG, "${b.text} state is ${b.isSelected}")
        if (v.id == R.id.button_primary_audio_play_pause) {
            if (v.isSelected) {
                main_player = Player(main_stream)
                viewModelScope.async(Dispatchers.IO) { main_player?.playAsync() }
            } else {
                main_player?.stop()
            }
        }
        if (v.id == R.id.button_secondary_audio_play_pause) {
            if (v.isSelected) {
                alt_player = Player(alt_stream)
                viewModelScope.async(Dispatchers.IO) { alt_player?.playAsync() }
            } else {
                alt_player?.stop()
            }
        }
    }
}