package com.github.overtane.audiotester.ui


import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.audiotrack.AudioStream
import com.github.overtane.audiotester.datastore.PreferencesRepository
import com.github.overtane.audiotester.player.Player
import com.github.overtane.audiotester.player.StreamStat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainViewModel(
    preferencesRepository: PreferencesRepository
): ViewModel() {

    private var _audioStream = MutableLiveData<MutableList<AudioStream>>()
    val audioStream
        get() = _audioStream

    private var _audioInfoMain = MutableLiveData<StreamStat?>()
    val audioInfoMain
        get() = _audioInfoMain

    private var _audioInfoAlt = MutableLiveData<StreamStat?>()
    val audioInfoAlt
        get() = _audioInfoAlt

    private var player: MutableList<Player>

    //private val userPrefs = preferencesRepository.dataFlow

    init {
        val prefs = preferencesRepository.get()
        _audioStream.value = prefs
        preferencesRepository.set()
        player =  mutableListOf(Player(prefs[0]), Player(prefs[1]))
    }

    fun setMainAudio(audioStream: AudioStream) =  _audioStream.value?.set(MAIN_AUDIO, audioStream)

    fun onMainAudioClicked(view: View) {
        if (!player[MAIN_AUDIO].isPlaying()) {
            audioStream.value?.get(0)?.let {
                    view.findNavController()
                        .navigate(MainFragmentDirections.actionMainAudioSettings(it))
            }
        }
    }

    fun setAltAudio(audioStream: AudioStream) =  _audioStream.value?.set(ALT_AUDIO, audioStream)

    fun onAltAudioClicked(view: View) {
        if (!player[ALT_AUDIO].isPlaying()) {
            view.findNavController().navigate(MainFragmentDirections.actionAltAudioSettings())
        }
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
        viewModelScope.async(Dispatchers.IO) { player[i].play() }
        viewModelScope.launch {
            player[i].status().collect { updateInfo(i, it) }
            stopAudio(view, i)
        }
    }

    private fun updateInfo(i: Int, info: StreamStat) = when (i) {
        MAIN_AUDIO -> _audioInfoMain.value = info
        ALT_AUDIO -> _audioInfoAlt.value = info
        else -> Unit
    }

    private fun onButtonDeselected(view: View) {
        when (view.id) {
            R.id.button_primary_audio_play_pause -> stopAudio(view, MAIN_AUDIO)
            R.id.button_secondary_audio_play_pause -> stopAudio(view, ALT_AUDIO)
            else -> Unit
        }
    }

    private fun stopAudio(view: View, i: Int) {
        player[i].stop()
        view.isSelected = false
    }

    companion object {
        const val MAIN_AUDIO = 0
        const val ALT_AUDIO = 1
    }
}

class MainViewModelFactory(
    private val preferencesRepository: PreferencesRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("unchecked_cast")
            return MainViewModel(preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
