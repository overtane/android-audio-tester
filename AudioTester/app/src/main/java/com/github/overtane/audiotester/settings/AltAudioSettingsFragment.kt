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
import androidx.navigation.fragment.NavHostFragment
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.databinding.FragmentAltAudioSettingsBinding
import com.github.overtane.audiotester.ui.MainFragment

class AltAudioSettingsFragment : Fragment() {

    private lateinit var myViewModel: SettingsViewModel
    private lateinit var binding: FragmentAltAudioSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val initialStream = AltAudioSettingsFragmentArgs.fromBundle(requireArguments()).audioStream
        myViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(initialStream, null)
        )[SettingsViewModel::class.java]

        binding = FragmentAltAudioSettingsBinding.inflate(inflater).apply {
            viewModel = myViewModel
            lifecycleOwner = viewLifecycleOwner

            okButton.setOnClickListener {
                setFragmentResult(
                    MainFragment.ALT_REQUEST_KEY,
                    bundleOf(MainFragment.AUDIO_STREAM_BUNDLE_KEY to myViewModel.fragmentResult())
                )
                NavHostFragment.findNavController(this@AltAudioSettingsFragment).navigateUp()
            }
        }

        myViewModel.apply{
            audioStream.observe(viewLifecycleOwner) { initializeFragmentValues(it) }
            source.observe(viewLifecycleOwner) { source -> source.checkAttributeVisibility() }
        }
        return binding.root
    }

    private fun initializeFragmentValues(audioStream: AudioStream) {
        audioStream.let {
            binding.apply {
                radioGroupSampleRate.check(it.sampleRate.checkId())
                radioGroupChannelCount.check(it.playbackChannelMask.checkId())
                radioGroupAudioSource.check(it.source.checkId())
            }
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
        binding.apply {
            sliderFrequency.visibility = visible
            textFrequencyValue.visibility = visible
        }
    }

}
