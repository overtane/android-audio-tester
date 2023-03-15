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

    private var player: Player? = null
    private val stream =
        AudioStream(AudioType.ENTERTAINMENT, AudioSource.SineWave(100, 44100, 10000), 44100, 2)


    fun onButtonClicked(v: View) {
        v.isSelected = !v.isSelected
        val b = v as Button
        Log.d(TAG, "${b.text} state is ${b.isSelected}")
        if (v.id == R.id.button_primary_audio_play_pause) {
            if (v.isSelected) {
                player = Player(stream)
                viewModelScope.async(Dispatchers.IO) { player?.playAsync() }
            } else {
                player?.stop()
            }
        }
    }
}