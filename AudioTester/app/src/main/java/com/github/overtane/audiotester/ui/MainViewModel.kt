package com.github.overtane.audiotester.ui


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.findNavController
import com.github.overtane.audiotester.AudioTesterApp
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.SOUND_EXTRA_CHANNELS
import com.github.overtane.audiotester.SOUND_EXTRA_DURATION
import com.github.overtane.audiotester.SOUND_EXTRA_NAME
import com.github.overtane.audiotester.SOUND_EXTRA_PREVIEW
import com.github.overtane.audiotester.SOUND_EXTRA_SAMPLE_RATE
import com.github.overtane.audiotester.SOUND_EXTRA_URL
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiostream.AudioDirection
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.audiostream.AudioType
import com.github.overtane.audiotester.datastore.PreferencesRepository
import com.github.overtane.audiotester.datastore.SoundRepository
import com.github.overtane.audiotester.player.Player
import com.github.overtane.audiotester.player.PlaybackStat
import com.github.overtane.audiotester.recorder.Recorder
import com.github.overtane.audiotester.recorder.RecordStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class MainViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val soundRepository: SoundRepository
) : ViewModel() {

    private var _liveStreams = MutableLiveData<MutableList<AudioStream>>()
    val liveStreams
        get() = _liveStreams

    private var _playbackInfoMain = MutableLiveData<PlaybackStat?>()
    val playbackInfoMain
        get() = _playbackInfoMain

    private var _playbackInfoAlt = MutableLiveData<PlaybackStat?>()
    val playbackInfoAlt
        get() = _playbackInfoAlt

    private var _recordInfo = MutableLiveData<RecordStat?>()
    val recordInfo
        get() = _recordInfo

    private val _decodeState = MutableLiveData<SoundRepository.DecodeState>()
    val decodeState: LiveData<SoundRepository.DecodeState> = _decodeState

    private var player: MutableList<Player>
    private var recorder: Recorder? = null

    private var _recorded: Date = Calendar.getInstance().time
    val recorded
        get() = _recorded

    private var _isRecording = MutableLiveData<Boolean>()
    val isRecording
        get() = _isRecording
    val isPlaying: Boolean
        get() = isSomeonePlaying()


    var buttonClick = MutableLiveData<Boolean>()
    var buttonStates = mutableMapOf (
        R.id.button_primary_audio_play_pause to false,
        R.id.button_primary_audio_duck to false,
        R.id.button_primary_audio_repeat to false,
        R.id.button_secondary_audio_play_pause to false,
        R.id.button_secondary_audio_duck to false,
        R.id.button_secondary_audio_repeat to false)

    init {
        Log.d(TAG, "INIT VIEWMODEL")
        viewModelScope.launch {
            soundRepository.decodeState.collectLatest { _decodeState.postValue(it) }
        }

        val prefs = preferencesRepository.get()
        _liveStreams.value = prefs
        player = mutableListOf(Player(prefs[MAIN_AUDIO]), Player(prefs[ALT_AUDIO]))
        _isRecording.value = false
        buttonClick.value = false
    }

    fun setMainAudio(audioStream: AudioStream) {
        _liveStreams.value?.set(MAIN_AUDIO, audioStream)
        preferencesRepository.set(liveStreams.value!!)
        _playbackInfoMain.value = null // Clear streaming data
        _recordInfo.value = null
    }
    
    fun setAltAudio(audioStream: AudioStream) {
        _liveStreams.value?.set(ALT_AUDIO, audioStream)
        preferencesRepository.set(liveStreams.value!!)
        _playbackInfoAlt.value = null // Clear streaming data
    }

    fun setSound(bundle: Bundle?) {
        bundle?.let {
            val stream = AudioStream(
                type = AudioType.ENTERTAINMENT,
                sampleRate = bundle.getInt(SOUND_EXTRA_SAMPLE_RATE),
                channelCount = bundle.getInt(SOUND_EXTRA_CHANNELS),
                source = AudioSource.Sound(
                    name = bundle.getString(SOUND_EXTRA_NAME) ?: "",
                    url = bundle.getString(SOUND_EXTRA_URL) ?: "",
                    preview = bundle.getString(SOUND_EXTRA_PREVIEW) ?: "",
                    durationMs = bundle.getInt(SOUND_EXTRA_DURATION) * 1000
                )
            )
            _liveStreams.value?.set(EXT_AUDIO, stream)
            setMainAudio(stream)
            soundRepository.decodeStream(stream)
        }
    }

    fun onButtonClicked(view: View) {
        when (flipButtonState(view)) {
            true -> onButtonSelected(view)
            false -> onButtonDeselected(view)
        }
    }

    private fun flipButtonState(view: View) : Boolean {
        val state = !buttonStates[view.id]!!
        buttonStates[view.id] = state
        buttonClick.value = !buttonClick.value!!
        return state
    }

    private fun onButtonSelected(view: View) {
        when (view.id) {
            R.id.button_primary_audio_play_pause -> {
                liveStreams.value?.get(MAIN_AUDIO)?.let {
                    if (it.direction != AudioDirection.RECORD)
                        startPlayback(view, MAIN_AUDIO)
                    if (it.direction != AudioDirection.PLAYBACK)
                        startRecord()
                }
            }

            R.id.button_secondary_audio_play_pause -> startPlayback(view, ALT_AUDIO)
            else -> Unit
        }
    }

    private fun startPlayback(view: View, i: Int) {
        player[i] = Player(getStream(i))
        viewModelScope.async(Dispatchers.IO) { player[i].play() }
        viewModelScope.launch {
            player[i].status().collect { updateInfo(i, it) }
            stopPlayback(view, i)
        }
    }

    private fun getStream(i: Int) = when (i) {
        ALT_AUDIO -> liveStreams.value?.get(ALT_AUDIO)!!
        MAIN_AUDIO -> {
            // TODO if sound exist and is selected
            if (soundIsSelectedAndAvailable()) {
                with(soundRepository.sound.value.second!!) {
                    (this.source as AudioSource.AudioBuffer).reset()
                    this
                }
            } else {
                liveStreams.value?.get(MAIN_AUDIO)!!
            }
        }

        else -> AudioStream(AudioType.DEFAULT, 48000, 2, AudioSource.Nothing)
    }

    private fun soundIsSelectedAndAvailable(): Boolean =
        (liveStreams.value?.get(MAIN_AUDIO)?.source is AudioSource.Sound) &&
                (soundRepository.decodeState.value == SoundRepository.DecodeState.READY)

    private fun startRecord() {
        recorder = Recorder(liveStreams.value?.get(0)!!)
        Log.d(TAG, "$recorder")
        viewModelScope.async(Dispatchers.IO) { recorder?.record() }
        _isRecording.value = true
        viewModelScope.launch {
            recorder?.status()?.collect { _recordInfo.value = it }
            recorder?.stop()
            _isRecording.value = false
        }
    }

    private fun updateInfo(i: Int, info: PlaybackStat) = when (i) {
        MAIN_AUDIO -> _playbackInfoMain.value = info
        ALT_AUDIO -> _playbackInfoAlt.value = info
        else -> Unit
    }

    private fun onButtonDeselected(view: View) {
        when (view.id) {
            R.id.button_primary_audio_play_pause -> {
                stopPlayback(view, MAIN_AUDIO)
                stopRecord()
            }

            R.id.button_secondary_audio_play_pause -> stopPlayback(view, ALT_AUDIO)
            else -> Unit
        }
    }

    private fun stopPlayback(view: View, i: Int) {
        Log.d(TAG, "Stop Playback")
        player[i].stop()
        flipButtonState(view)
    }

    private fun stopRecord() {
        recorder?.stop()
        recorder = null
        _recorded = Calendar.getInstance().time
        _isRecording.value = false
    }

    private fun isSomeonePlaying() =
        player[MAIN_AUDIO].isPlaying() || player[ALT_AUDIO].isPlaying() || _isRecording.value!!

    companion object {
        const val MAIN_AUDIO = 0
        const val ALT_AUDIO = 1
        const val EXT_AUDIO = 2

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as AudioTesterApp
                MainViewModel(app.preferencesRepository, app.soundRepository)
            }
        }
    }
}

