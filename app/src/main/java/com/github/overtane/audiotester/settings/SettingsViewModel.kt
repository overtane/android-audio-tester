package com.github.overtane.audiotester.settings

import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.audiostream.AudioType

class SettingsViewModel(initialStream: AudioStream, val sound: AudioStream?) :
    ViewModel() {

    enum class UiAudioSource {
        SINE_WAVE,
        WHITE_NOISE,
        SILENCE,
        SOUND,
        NOTHING
    }


    private var _audioStream = MutableLiveData<AudioStream>()
    val audioStream
        get() = _audioStream

    private var _source = MutableLiveData<UiAudioSource>()
    val source
        get() = _source

    private var _duration = MutableLiveData<Int>()
    val duration
        get() = _duration

    private var _frequency = MutableLiveData<Int>()
    val frequency
        get() = _frequency

    private var audioType = initialStream.type
    private var sampleRate  = initialStream.sampleRate
    private var channelCount = initialStream.channelCount
    var name = (sound?.source as? AudioSource.Sound)?.name ?: ""
    private var url = (sound?.source as? AudioSource.Sound)?.name ?: ""

    init {
        _audioStream.value = initialStream
        _source.value = initialStream.source.asUiAudioSource()
        _duration.value = initialStream.source.durationMs.div(1000)
        _frequency.value = when (initialStream.source) {
            is AudioSource.SineWave -> initialStream.source.freqHz
            else -> 200
        }
    }

    fun fragmentResult(): AudioStream {
        val durationMs = duration.value?.times(1000)!!
        val freqHz = frequency.value!!
        val audioSource = when (source.value!!) {
            UiAudioSource.SINE_WAVE -> AudioSource.SineWave(
                freqHz,
                sampleRate,
                channelCount,
                durationMs
            )

            UiAudioSource.SILENCE -> AudioSource.Silence(durationMs)
            UiAudioSource.WHITE_NOISE -> AudioSource.WhiteNoise(durationMs)
            UiAudioSource.SOUND -> AudioSource.Sound(name, url, durationMs)
            else -> AudioSource.Nothing
        }
        return AudioStream(audioType, sampleRate, channelCount, audioSource)
    }

    fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        when (seekBar.id) {
            R.id.slider_duration -> _duration.value = progress
            R.id.slider_frequency -> _frequency.value = progress
            else -> Unit
        }
    }

    fun onAudioFormatChanged(view: View, id: Int) {
        when (view.id) {
            R.id.radio_group_audio_type -> audioType = id.asAudioType()
            R.id.radio_group_sample_rate -> sampleRate = id.asSampleRate()
            R.id.radio_group_channel_count -> channelCount = id.asChannelCount()
            else -> Unit
        }
        _audioStream.value = fragmentResult()
    }

        private fun Int.asAudioType() = when (this) {
        R.id.radio_button_audio_type_alert -> AudioType.ALERT
        R.id.radio_button_audio_type_default -> AudioType.DEFAULT
        R.id.radio_button_audio_type_entertainment -> AudioType.ENTERTAINMENT
        R.id.radio_button_audio_type_speech_recognition -> AudioType.SPEECH_RECOGNITION
        R.id.radio_button_audio_type_telephony -> AudioType.TELEPHONY
        else -> AudioType.ENTERTAINMENT
    }

    private fun Int.asSampleRate() = when (this) {
        R.id.radio_button_sample_rate_8khz -> 8000
        R.id.radio_button_sample_rate_16khz -> 16000
        R.id.radio_button_sample_rate_24khz -> 24000
        R.id.radio_button_sample_rate_32khz -> 32000
        R.id.radio_button_sample_rate_44_1khz -> 44100
        R.id.radio_button_sample_rate_48khz -> 48000
        else -> 0
    }

    private fun Int.asChannelCount() = when (this) {
        R.id.radio_button_channel_count_mono -> 1
        R.id.radio_button_channel_count_stereo -> 2
        else -> 0
    }

    fun onAudioSourceChanged(view: View, id: Int) {
        _source.value = id.asAudioSource()
        if (_source.value == UiAudioSource.SOUND) {
            sound?.let {
                sampleRate = it.sampleRate
                channelCount = it.channelCount
                audioType = AudioType.ENTERTAINMENT
                _duration.value = sound.source.durationMs.div(1000)
                name = (sound.source as AudioSource.Sound).name
                url = sound.source.url
            }
        }
        _audioStream.value = fragmentResult()
    }

    private fun Int.asAudioSource() = when (this) {
        R.id.radio_button_audio_source_sine_wave -> UiAudioSource.SINE_WAVE
        R.id.radio_button_audio_source_white_noise -> UiAudioSource.WHITE_NOISE
        R.id.radio_button_audio_source_silence -> UiAudioSource.SILENCE
        R.id.radio_button_audio_source_sound -> UiAudioSource.SOUND
        else -> UiAudioSource.NOTHING
    }

    private fun AudioSource.asUiAudioSource() = when (this) {
        is AudioSource.SineWave -> UiAudioSource.SINE_WAVE
        is AudioSource.WhiteNoise -> UiAudioSource.WHITE_NOISE
        is AudioSource.Silence -> UiAudioSource.SILENCE
        is AudioSource.Sound -> UiAudioSource.SOUND
        else -> UiAudioSource.NOTHING
    }

}

class SettingsViewModelFactory(
    private val audioStream: AudioStream,
    private val sound: AudioStream?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("unchecked_cast")
            return SettingsViewModel(audioStream, sound) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

