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
    var isRecording = MutableLiveData<Boolean>()
    private var recorded: Date = Calendar.getInstance().time

    init {
        Log.d(TAG, "INIT VIEWMODEL")
        viewModelScope.launch {
            soundRepository.decodeState.collectLatest { _decodeState.postValue(it) }
        }

        val prefs = preferencesRepository.get()
        _liveStreams.value = prefs
        player = mutableListOf(Player(prefs[MAIN_AUDIO]), Player(prefs[ALT_AUDIO]))
        isRecording.value = false
    }

    fun setMainAudio(audioStream: AudioStream) {
        _liveStreams.value?.set(MAIN_AUDIO, audioStream)
        preferencesRepository.set(liveStreams.value!!)
        _playbackInfoMain.value = null // Clear streaming data
        _recordInfo.value = null
    }

    fun onMainAudioClicked(view: View) {
        val sound = liveStreams.value?.get(EXT_AUDIO)
        if (!isPlaying()) {
            liveStreams.value?.get(MAIN_AUDIO)?.let {
                view.findNavController()
                    .navigate(MainFragmentDirections.actionMainAudioSettings(it, sound))
            }
        }
    }

    fun setAltAudio(audioStream: AudioStream) {
        _liveStreams.value?.set(ALT_AUDIO, audioStream)
        preferencesRepository.set(liveStreams.value!!)
        _playbackInfoAlt.value = null // Clear streaming data
    }

    fun onAltAudioClicked(view: View) {
        if (!isPlaying()) {
            liveStreams.value?.get(ALT_AUDIO)?.let {
                view.findNavController()
                    .navigate(MainFragmentDirections.actionAltAudioSettings(it))
            }
        }
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
        view.isSelected = !view.isSelected
        when (view.isSelected) {
            true -> onButtonSelected(view)
            false -> onButtonDeselected(view)
        }
    }

    fun onMicButtonClicked(view: View): Boolean {
        val stream = liveStreams.value?.get(MAIN_AUDIO)!!
        val recording = recordInfo.value
        Log.d(TAG, "Recording size ${recording?.recording?.size} bytes")
        if (view.isSelected && !isPlaying() && recording != null) {
            view.findNavController().navigate(
                MainFragmentDirections.actionRecordingPlaybackFragment(stream, recording, recorded)
            )
            return true
        }
        if (!view.isSelected || isPlaying()) return true
        return false
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
        recorded = Calendar.getInstance().time
        recorder = Recorder(liveStreams.value?.get(0)!!)
        Log.d(TAG, "$recorder")
        viewModelScope.async(Dispatchers.IO) { recorder?.record() }
        isRecording.value = true
        viewModelScope.launch {
            recorder?.status()?.collect { _recordInfo.value = it }
            recorder?.stop()
            isRecording.value = false
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
        view.isSelected = false
    }

    private fun stopRecord() {
        recorder?.stop()
        recorder = null
        isRecording.value = false
    }

    private fun isPlaying() =
        player[MAIN_AUDIO].isPlaying() || player[ALT_AUDIO].isPlaying() || isRecording()

    private fun isRecording() = recorder?.isRecording() ?: false

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

