package com.github.overtane.audiotester.recording

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.databinding.FragmentRecordingPlaybackBinding
import com.github.overtane.audiotester.recorder.RecordStat
import java.util.Date

class RecordingPlaybackFragment : Fragment() {

    private lateinit var myViewModel: RecordingPlaybackViewModel
    private lateinit var binding: FragmentRecordingPlaybackBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myViewModel = ViewModelProvider(
            this,
            RecordingPlaybackViewModelFactory(
                RecordingPlaybackFragmentArgs.fromBundle(requireArguments()).audioStream,
                RecordingPlaybackFragmentArgs.fromBundle(requireArguments()).recording,
                RecordingPlaybackFragmentArgs.fromBundle(requireArguments()).recorded,
            )
        )[RecordingPlaybackViewModel::class.java]

        binding = FragmentRecordingPlaybackBinding.inflate(inflater).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = myViewModel
        }

        myViewModel.playState.observe(viewLifecycleOwner) { state ->
            when (state) {
                RecordingPlaybackViewModel.PlayState.PLAYING ->
                    binding.playButton.text = getString(R.string.button_text_stop)
                RecordingPlaybackViewModel.PlayState.STOPPED ->
                    binding.playButton.text = getString(R.string.button_text_play)
                else -> Unit
            }
        }

        return binding.root
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
