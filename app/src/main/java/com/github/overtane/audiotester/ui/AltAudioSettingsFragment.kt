package com.github.overtane.audiotester.ui

import android.media.AudioFormat
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.NavHostFragment
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.databinding.FragmentAltAudioSettingsBinding

class AltAudioSettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by activityViewModels()
    private lateinit var binding: FragmentAltAudioSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAltAudioSettingsBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.okButton.setOnClickListener {
            setFragmentResult(
                MainFragment.ALT_REQUEST_KEY,
                bundleOf(MainFragment.AUDIO_STREAM_BUNDLE_KEY to viewModel.fragmentResult())
            )
            NavHostFragment.findNavController(this).navigateUp()
        }

        viewModel.source.observe(viewLifecycleOwner) { source -> source.checkAttributeVisibility() }

        val audioStream = AltAudioSettingsFragmentArgs.fromBundle(requireArguments()).audioStream
        initializeFragmentValues(audioStream)
        return binding.root
    }

    private fun initializeFragmentValues(audioStream: AudioStream) {
        viewModel.fragmentArgument(audioStream)
        audioStream.let {
            binding.radioGroupSampleRate.check(it.sampleRate.checkId())
            binding.radioGroupChannelCount.check(it.channelMask.checkId())
            binding.radioGroupAudioSource.check(it.source.checkId())
        }
    }

    private fun Int.checkId(): Int = when (this) {
        AudioFormat.CHANNEL_OUT_MONO -> R.id.radio_button_channel_count_mono
        AudioFormat.CHANNEL_OUT_STEREO -> R.id.radio_button_channel_count_stereo
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

    private fun SettingsViewModel.UiAudioSource.checkAttributeVisibility() {
        val visible = when (this == SettingsViewModel.UiAudioSource.SINE_WAVE) {
            true -> View.VISIBLE
            false -> View.INVISIBLE
        }
        binding.sliderFrequency.visibility = visible
        binding.textFrequencyValue.visibility = visible
    }

}
