package com.github.overtane.audiotester.ui

import android.media.AudioFormat
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioSource
import com.github.overtane.audiotester.audiotrack.AudioStream
import com.github.overtane.audiotester.audiotrack.AudioType
import com.github.overtane.audiotester.databinding.FragmentMainAudioSettingsBinding

class MainAudioSettingsFragment : Fragment() {

    private val viewModel: MainAudioSettingsViewModel by activityViewModels()
    private lateinit var binding: FragmentMainAudioSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainAudioSettingsBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.okButton.setOnClickListener {
            setFragmentResult(
                MainFragment.MAIN_REQUEST_KEY,
                bundleOf(MainFragment.AUDIO_STREAM_BUNDLE_KEY to viewModel.fragmentResult())
            )
            findNavController(this).navigateUp()
        }

        viewModel.source.observe(viewLifecycleOwner) { source -> source.checkAttributeVisibility() }

        val audioStream = MainAudioSettingsFragmentArgs.fromBundle(requireArguments()).audioStream
        initializeFragmentValues(audioStream)
        return binding.root
    }

    private fun initializeFragmentValues(audioStream: AudioStream) {
        viewModel.fragmentArgument(audioStream)
        audioStream.let {
            binding.radioGroupAudioType.check(it.type.checkId())
            binding.radioGroupSampleRate.check(it.sampleRate.checkId())
            binding.radioGroupChannelCount.check(it.channelMask.checkId())
            binding.radioGroupAudioSource.check(it.source.checkId())
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
        else -> androidx.appcompat.R.id.none
    }

    fun AudioType.enableSampleRateByType() {
        val selections = when (this) {
            AudioType.ALERT -> booleanArrayOf(false, false, false, false, true, true)
            AudioType.ALTERNATE -> booleanArrayOf(false, false, false, false, true, true)
            AudioType.DEFAULT -> booleanArrayOf(true, true, true, true, true, true)
            AudioType.ENTERTAINMENT -> booleanArrayOf(false, false, false, false, true, true)
            AudioType.SPEECH_RECOGNITION -> booleanArrayOf(false, false, true, false, false, false)
            AudioType.TELEPHONY -> booleanArrayOf(true, true, true, true, true, true)
        }
        binding.radioButtonSampleRate8khz.isEnabled = selections[0]
        binding.radioButtonSampleRate16khz.isEnabled = selections[1]
        binding.radioButtonSampleRate24khz.isEnabled = selections[2]
        binding.radioButtonSampleRate32khz.isEnabled = selections[3]
        binding.radioButtonSampleRate441khz.isEnabled = selections[4]
        binding.radioButtonSampleRate48khz.isEnabled = selections[5]
    }

    fun MainAudioSettingsViewModel.UiAudioSource.checkAttributeVisibility() {
        val visible = when (this == MainAudioSettingsViewModel.UiAudioSource.SINE_WAVE) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
        binding.sliderFrequency.visibility = visible
        binding.textFrequencyValue.visibility = visible
    }
}


