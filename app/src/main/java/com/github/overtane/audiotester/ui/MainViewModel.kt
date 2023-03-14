package com.github.overtane.audiotester.ui

import android.provider.MediaStore.Audio
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioSource
import com.github.overtane.audiotester.audiotrack.AudioType
import com.github.overtane.audiotester.audiotrack.PlaybackStream
import com.github.overtane.audiotester.audiotrack.StreamDescriptor
import kotlinx.coroutines.*

class MainViewModel : ViewModel() {

    private var stream : PlaybackStream? = null
    private lateinit var player: Deferred<Unit>

    fun onButtonClicked(v: View) {
        v.isSelected = !v.isSelected
        val b = v as Button
        Log.d(TAG, "${b.text} state is ${b.isSelected}")

        if (v.id == R.id.button_primary_audio_play_pause) {
            if (v.isSelected) {

                stream = PlaybackStream(StreamDescriptor(AudioType.ENTERTAINMENT,60f, 48000, 2))
                player = viewModelScope.async(Dispatchers.IO) { stream?.play(60) }
                Log.d(TAG, "Playback started")
                viewModelScope.launch(Dispatchers.IO) { waitPlayer() }
            } else {
                player.cancel()
                stream?.stop()
            }
        }
    }

    suspend fun waitPlayer() = runBlocking {
        player.join()
        Log.d(TAG, "Playback stopped")
        // TODO change button state!
    }

}