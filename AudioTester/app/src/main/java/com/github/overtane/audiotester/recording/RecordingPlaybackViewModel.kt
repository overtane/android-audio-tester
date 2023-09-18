package com.github.overtane.audiotester.recording

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.audiostream.AudioType
import com.github.overtane.audiotester.player.PlaybackStat
import com.github.overtane.audiotester.player.Player
import com.github.overtane.audiotester.recorder.RecordStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class RecordingPlaybackViewModel(
    audioStream: AudioStream,
    recordingStat: RecordStat,
    recorded: Date
) : ViewModel() {

    enum class PlayState { STOPPED, PLAYING }

    private val playbackStream = AudioStream(
        AudioType.ENTERTAINMENT,
        audioStream.sampleRate,
        audioStream.channelCount,
        AudioSource.AudioBuffer(
            recordingStat.framesStreamed / recordingStat.sampleRate * 1000,
            recordingStat.recording.toShortArray()
        )
    )

    val originalType = audioStream.type.toString()
    val sampleRate = audioStream.sampleRate.toString()
    val recordingTime: String = dateFormat.format(recorded)

    private var _playbackInfo = MutableLiveData<PlaybackStat?>()
    val playbackInfo
        get() = _playbackInfo

    val remainingSeconds : LiveData<String> = playbackInfo.map { info ->
        var remainingTime = playbackStream.source.durationMs / 1000
        info?.also {
            val totalTime = it.totalFrames / it.sampleRate
            val elapsedTime = it.framesStreamed / it.sampleRate
            remainingTime = totalTime - elapsedTime
        }
        remainingTime.toString()
    }

    val elapsedSeconds : LiveData<String> = playbackInfo.map { info ->
        var elapsedTime = 0
        info?.also {
            elapsedTime = it.framesStreamed / it.sampleRate
        }
        elapsedTime.toString()
    }

    val playState = MutableLiveData<PlayState>()

    private var player: Player? = null

    init {
        playbackInfo.value = null
        playState.value = PlayState.STOPPED
    }

    fun onPlayClicked(view: View) = when (playState.value) {
        PlayState.STOPPED -> startPlayback()
        PlayState.PLAYING -> stopPlayback()
        else -> Unit
    }

    private fun startPlayback() {
        playState.value = PlayState.PLAYING
        (playbackStream.source as AudioSource.AudioBuffer).reset()
        player = Player(playbackStream)
        viewModelScope.async(Dispatchers.IO) { player?.play() }
        viewModelScope.launch {
            player?.status()?.collect { _playbackInfo.value = it }
            stopPlayback()
        }
    }

    private fun stopPlayback() {
        playState.value = PlayState.STOPPED
        player?.stop()
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd H:mm:ss"
        private val dateFormat = SimpleDateFormat(DATE_FORMAT)
    }
}

class RecordingPlaybackViewModelFactory(
    private val audioStream: AudioStream,
    private val recordingStat: RecordStat,
    private val recorded: Date
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordingPlaybackViewModel::class.java)) {
            @Suppress("unchecked_cast")
            return RecordingPlaybackViewModel(audioStream, recordingStat, recorded) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
