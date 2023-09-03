package com.github.overtane.audiotester.settings

import android.media.AudioFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.audiostream.AudioType
import com.github.overtane.audiotester.databinding.FragmentMainAudioSettingsBinding
import com.github.overtane.audiotester.ui.MainFragment


class MainAudioSettingsFragment : Fragment() {

    private lateinit var myViewModel: SettingsViewModel
    private lateinit var binding: FragmentMainAudioSettingsBinding
    private var sound: AudioStream? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val initialStream = MainAudioSettingsFragmentArgs.fromBundle(requireArguments()).audioStream
        sound = MainAudioSettingsFragmentArgs.fromBundle(requireArguments()).sound
        myViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(initialStream, sound)
        )[SettingsViewModel::class.java]

        binding = FragmentMainAudioSettingsBinding.inflate(inflater).apply {
            viewModel = myViewModel
            lifecycleOwner = viewLifecycleOwner

            okButton.setOnClickListener {
                setFragmentResult(
                    MainFragment.MAIN_REQUEST_KEY,
                    bundleOf(MainFragment.AUDIO_STREAM_BUNDLE_KEY to viewModel?.fragmentResult())
                )
                findNavController(this@MainAudioSettingsFragment).navigateUp()
            }
        }

        myViewModel.apply {
            audioStream.observe(viewLifecycleOwner) { initializeFragmentValues(it) }
            source.observe(viewLifecycleOwner) { it.checkAttributeVisibility() }
        }
        return binding.root
    }


    private fun initializeFragmentValues(audioStream: AudioStream) {
        audioStream.also {
            binding.apply {
                radioGroupAudioType.check(it.type.checkId())
                radioGroupSampleRate.check(it.sampleRate.checkId())
                radioGroupChannelCount.check(it.playbackChannelMask.checkId())
                radioGroupAudioSource.check(it.source.checkId())
            }
        }
    }

    private fun AudioType.checkId(): Int = when (this) {
        AudioType.ALERT -> R.id.radio_button_audio_type_alert
        AudioType.DEFAULT -> R.id.radio_button_audio_type_default
        AudioType.ENTERTAINMENT -> R.id.radio_button_audio_type_entertainment
        AudioType.SPEECH_RECOGNITION -> R.id.radio_button_audio_type_speech_recognition
        AudioType.TELEPHONY -> R.id.radio_button_audio_type_telephony
        else -> androidx.appcompat.R.id.none
    }

    private fun Int.checkId(): Int = when (this) {
        AudioFormat.CHANNEL_OUT_MONO -> R.id.radio_button_channel_count_mono
        AudioFormat.CHANNEL_OUT_STEREO -> R.id.radio_button_channel_count_stereo
        8000 -> R.id.radio_button_sample_rate_8khz
        16000 -> R.id.radio_button_sample_rate_16khz
        24000 -> R.id.radio_button_sample_rate_24khz
        32000 -> R.id.radio_button_sample_rate_32khz
        44100 -> R.id.radio_button_sample_rate_44_1khz
        48000 -> R.id.radio_button_sample_rate_48khz
        else -> androidx.appcompat.R.id.none
    }

    private fun AudioSource.checkId(): Int = when (this) {
        is AudioSource.SineWave -> R.id.radio_button_audio_source_sine_wave
        is AudioSource.WhiteNoise -> R.id.radio_button_audio_source_white_noise
        is AudioSource.Silence -> R.id.radio_button_audio_source_silence
        is AudioSource.Sound -> R.id.radio_button_audio_source_sound
        else -> androidx.appcompat.R.id.none
    }

    // TODO limit available sample rates
    fun AudioType.enableSampleRateByType() {
        val selections = when (this) {
            AudioType.ALERT -> booleanArrayOf(false, false, false, false, true, true)
            AudioType.ALTERNATE -> booleanArrayOf(false, false, false, false, true, true)
            AudioType.DEFAULT -> booleanArrayOf(true, true, true, true, true, true)
            AudioType.ENTERTAINMENT -> booleanArrayOf(false, false, false, false, true, true)
            AudioType.SPEECH_RECOGNITION -> booleanArrayOf(false, false, true, false, false, false)
            AudioType.TELEPHONY -> booleanArrayOf(true, true, true, true, true, true)
        }
        binding.apply {
            radioButtonSampleRate8khz.isEnabled = selections[0]
            radioButtonSampleRate16khz.isEnabled = selections[1]
            radioButtonSampleRate24khz.isEnabled = selections[2]
            radioButtonSampleRate32khz.isEnabled = selections[3]
            radioButtonSampleRate441khz.isEnabled = selections[4]
            radioButtonSampleRate48khz.isEnabled = selections[5]
        }
    }

    private fun SettingsViewModel.UiAudioSource.checkAttributeVisibility() {
        when (this) {
            SettingsViewModel.UiAudioSource.SINE_WAVE -> {
                setAttributeVisibility(
                    intArrayOf(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE)
                )
            }

            SettingsViewModel.UiAudioSource.WHITE_NOISE,
            SettingsViewModel.UiAudioSource.SILENCE -> {
                setAttributeVisibility(
                    intArrayOf(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE)
                )
            }

            SettingsViewModel.UiAudioSource.SOUND -> {
                setAttributeVisibility(
                    intArrayOf(View.VISIBLE, View.GONE, View.GONE, View.GONE, View.VISIBLE)
                )
            }

            else -> Unit
        }
    }

    private fun setAttributeVisibility(visibility: IntArray) {
        binding.apply {
            textDurationValue.visibility = visibility[0]
            sliderDuration.visibility = visibility[1]
            textFrequencyValue.visibility = visibility[2]
            sliderFrequency.visibility = visibility[3]
            textUrlValue.visibility = visibility[4]
        }
    }
}


