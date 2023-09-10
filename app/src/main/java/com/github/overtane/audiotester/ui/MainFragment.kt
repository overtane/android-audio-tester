package com.github.overtane.audiotester.ui

import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.SOUND_BROWSER_PACKAGE
import com.github.overtane.audiotester.SOUND_REQUEST_CODE
import com.github.overtane.audiotester.SOUND_REQUEST_KEY
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.databinding.FragmentMainBinding
import com.github.overtane.audiotester.datastore.SoundRepository.DecodeState

class MainFragment : Fragment(), MenuProvider {

    private val myViewModel: MainViewModel by viewModels { MainViewModel.Factory }

    private lateinit var binding: FragmentMainBinding

    private var animator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentMainBinding.inflate(inflater).apply {
            lifecycleOwner = viewLifecycleOwner
            // bind ui-data to viewModel instance (left: xml-data, right: viewModel object)
            viewModel = myViewModel

            buttonPrimaryAudioRecording.setOnClickListener {
                if (!myViewModel.onMicButtonClicked(it)) {
                    Toast.makeText(context, "No recording", Toast.LENGTH_SHORT).show()
                }
            }
        }

        (requireActivity() as MenuHost).addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myViewModel.apply {
            isRecording.observe(viewLifecycleOwner) { recording ->
                when (recording) {
                    true -> startMicAnimation()
                    false -> stopMicAnimation()
                }
            }
            decodeState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    DecodeState.DECODING ->
                        Toast.makeText(context, "Decoding sound", Toast.LENGTH_SHORT).show()
                    DecodeState.DECODED ->
                        Toast.makeText(context, "Sound ready", Toast.LENGTH_SHORT).show()
                    DecodeState.ERROR ->
                        Toast.makeText(context, "Sound unavailable", Toast.LENGTH_SHORT).show()
                    else -> Unit
                }
            }

            MainFragmentArgs.fromBundle(requireArguments()).sound?.let {
                setSound(it)
            }
        }

        // Use fragment result listeners to get user input from settings fragment
        setFragmentResultListener(MAIN_REQUEST_KEY) { _, bundle ->
            val result = bundle.getParcelable<AudioStream>(AUDIO_STREAM_BUNDLE_KEY)
            result?.let {
                myViewModel.setMainAudio(it)
            }
        }

        setFragmentResultListener(ALT_REQUEST_KEY) { _, bundle ->
            val result = bundle.getParcelable<AudioStream>(AUDIO_STREAM_BUNDLE_KEY)
            result?.let {
                myViewModel.setAltAudio(it)
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
        menuInflater.inflate(R.menu.main_overflow_menu, menu)

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.soundBrowser -> openSoundBrowserApp()
            else -> NavigationUI.onNavDestinationSelected(item, findNavController())
        }
        return true
    }

    private fun startMicAnimation() {
        val icon = binding.buttonPrimaryAudioRecording

        animator =
            ObjectAnimator.ofFloat(icon, View.ALPHA, 0f).apply {
                duration = 600
                repeatCount = Animation.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                start()
            }
    }

    private fun stopMicAnimation() {
        val icon = binding.buttonPrimaryAudioRecording
        animator?.cancel()
        icon.alpha = 1.0f
        animator = null
    }

    private fun openSoundBrowserApp() = runCatching {
        val intent =
            requireContext().packageManager.getLaunchIntentForPackage(SOUND_BROWSER_PACKAGE)
        intent?.apply {
            putExtra(SOUND_REQUEST_KEY, pendingIntent())
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ContextCompat.startActivity(requireContext(), this, null)
        } ?: throw ActivityNotFoundException()
    }.onFailure {
        Toast.makeText(requireContext(), SOUND_BROWSER_ERROR, Toast.LENGTH_SHORT).show()
    }

    private fun pendingIntent() = PendingIntent.getActivity(
        context,
        SOUND_REQUEST_CODE,
        Intent(Intent.ACTION_DEFAULT).apply {
            component =
                ComponentName.unflattenFromString(requireContext().packageName + "/.MainActivity")
        },
        PendingIntent.FLAG_MUTABLE
    )

    companion object {
        const val MAIN_REQUEST_KEY = "MainAudioSettings"
        const val ALT_REQUEST_KEY = "AltAudioSettings"
        const val AUDIO_STREAM_BUNDLE_KEY = "AudioStream"

        private const val SOUND_BROWSER_ERROR = "SoundBrowser not available"
    }
}


