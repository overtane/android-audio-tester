package org.github.overtane.soundbrowser.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import org.github.overtane.soundbrowser.R
import org.github.overtane.soundbrowser.databinding.FragmentSoundDetailsBinding
import kotlinx.coroutines.launch
import org.github.overtane.soundbrowser.MainActivity
import org.github.overtane.soundbrowser.SOUND_REPLY_KEY

class SoundDetailsDialog : DialogFragment() {

    private lateinit var myViewModel: SoundDetailsViewModel
    private lateinit var binding: FragmentSoundDetailsBinding
    private var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { id = it.getInt(ARG_ID) } ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myViewModel = ViewModelProvider(
            this,
            SoundDetailsViewModelFactory(id)
        )[SoundDetailsViewModel::class.java]

        binding = FragmentSoundDetailsBinding.inflate(layoutInflater).apply {
            viewModel = myViewModel
            lifecycleOwner = viewLifecycleOwner

            detailsClose.setOnClickListener { dismiss() }

            detailsUseSoundButton.setOnClickListener {
                activity?.supportFragmentManager?.setFragmentResult(
                    SOUND_REPLY_KEY,
                    myViewModel.fragmentResult()
                )
                dismiss()
            }
        }

        myViewModel.details.observe(this) {
            if (it != null) {
                binding.detailsDialog.visibility = View.VISIBLE
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                myViewModel.state.collectLatest { state ->
                    when (state) {
                        SoundDetailsViewModel.PlaybackState.ERROR -> dismissWithError()
                        SoundDetailsViewModel.PlaybackState.LOADING -> Unit
                        else -> binding.detailsLoadingWheel.visibility = View.GONE
                    }
                    binding.detailsPlayButton.isClickable = when (state) {
                        SoundDetailsViewModel.PlaybackState.LOADING,
                        SoundDetailsViewModel.PlaybackState.COMPLETED -> false

                        else -> true
                    }
                    val id = when (state) {
                        SoundDetailsViewModel.PlaybackState.ERROR,
                        SoundDetailsViewModel.PlaybackState.LOADING -> R.string.loading

                        SoundDetailsViewModel.PlaybackState.STOPPED,
                        SoundDetailsViewModel.PlaybackState.COMPLETED -> R.string.play

                        SoundDetailsViewModel.PlaybackState.PLAYING -> R.string.pause
                    }
                    binding.detailsPlayButton.text = getString(id)
                    myViewModel.playByState(state)
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        myViewModel.onDestroyView()
    }

    private fun dismissWithError() {
        val duration = Toast.LENGTH_SHORT
        Toast.makeText(context, LOAD_ERROR, duration).show()
        dismiss()
    }

    companion object {
        private const val LOAD_ERROR = "Cannot load sound"
        private const val ARG_ID = "id"

        @JvmStatic
        fun newInstance(id: Int) =
            SoundDetailsDialog().apply {
                arguments = Bundle().apply { putInt(ARG_ID, id) }
            }
    }
}