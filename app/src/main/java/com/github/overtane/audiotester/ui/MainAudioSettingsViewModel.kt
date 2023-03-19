package com.github.overtane.audiotester.ui

import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioSource
import com.github.overtane.audiotester.audiotrack.AudioStream
import com.github.overtane.audiotester.audiotrack.AudioType

class MainAudioSettingsViewModel() : ViewModel() {

    enum class UiAudioSource {
        SINE_WAVE,
        WHITE_NOISE,
        SILENCE,
        NOTHING
    }

    private lateinit var audioType: AudioType
    private var sampleRate: Int = 0
    private var channelCount: Int = 0

    private var _source = MutableLiveData<UiAudioSource>()
    val source
        get() = _source

    private var _duration = MutableLiveData<Int>()
    val duration
        get() = _duration

    private var _frequency = MutableLiveData<Int>()
    val frequency
        get() = _frequency

    fun fragmentArgument(arg: AudioStream) {
        audioType = arg.type
        sampleRate = arg.sampleRate
        channelCount = arg.channelCount
        _source.value = arg.source.asUiAudioSource()
        _duration.value = arg.source.durationMs.div(1000)
        _frequency.value = when (arg.source) {
            is AudioSource.SineWave -> arg.source.freqHz
            else -> 200
        }
    }

    fun fragmentResult() : AudioStream {
        val durationMs = duration.value?.times(1000)!!
        val freqHz = frequency.value!!
        val audioSource = when (_source.value!!) {
            UiAudioSource.SINE_WAVE -> AudioSource.SineWave(
                freqHz,
                sampleRate,
                channelCount,
                durationMs
            )
            UiAudioSource.SILENCE -> AudioSource.Silence(durationMs)
            UiAudioSource.WHITE_NOISE -> AudioSource.WhiteNoise(durationMs)
            UiAudioSource.NOTHING -> AudioSource.Nothing
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

    fun onAudioFormatChanged(view: View, id: Int) = when (view.id) {
        R.id.radio_group_audio_type -> audioType = id.asAudioType()
        R.id.radio_group_sample_rate -> sampleRate = id.asSampleRate()
        R.id.radio_group_channel_count -> channelCount = id.asChannelCount()
        else -> Unit
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

    fun onAudioSourceChanged(view : View, id : Int) {
        _source.value = id.asAudioSource()
    }

    private fun Int.asAudioSource() = when (this) {
        R.id.radio_button_audio_source_sine_wave -> UiAudioSource.SINE_WAVE
        R.id.radio_button_audio_source_silence -> UiAudioSource.SILENCE
        R.id.radio_button_audio_source_white_noise -> UiAudioSource.WHITE_NOISE
        else -> UiAudioSource.NOTHING
    }

    private fun AudioSource.asUiAudioSource() = when (this){
        is AudioSource.SineWave -> UiAudioSource.SINE_WAVE
        is AudioSource.WhiteNoise -> UiAudioSource.WHITE_NOISE
        is AudioSource.Silence -> UiAudioSource.SILENCE
        else -> UiAudioSource.NOTHING
    }

}

