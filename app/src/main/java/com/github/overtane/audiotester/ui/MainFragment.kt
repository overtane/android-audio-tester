package com.github.overtane.audiotester.ui

import android.animation.ObjectAnimator
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.databinding.FragmentMainBinding
import com.github.overtane.audiotester.datastore.UserPrefsSerializer
import com.github.overtane.audiotester.datastore.PreferencesRepository
import com.github.overtane.audiotester.datastore.UserPrefs

class MainFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentMainBinding
    private lateinit var myViewModel: MainViewModel

    private var animator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(PreferencesRepository(requireContext().dataStore))
        )[MainViewModel::class.java]

        binding = FragmentMainBinding.inflate(inflater).apply {
            lifecycleOwner = viewLifecycleOwner
            // bind ui-data to viewModel instance (left: xml-data, right: viewModel object)
            viewModel = myViewModel
        }

        (requireActivity() as MenuHost).addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        myViewModel.isRecording.observe(viewLifecycleOwner) { recording ->
            when (recording) {
                true -> startMicAnimation()
                false -> stopMicAnimation()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        val intent = requireContext().packageManager.getLaunchIntentForPackage(SOUND_BROWSER)
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
        REQUEST_CODE,
        Intent(Intent.ACTION_DEFAULT).apply {
            component =
                ComponentName.unflattenFromString(requireContext().packageName + "/.MainActivity")
        },
        PendingIntent.FLAG_MUTABLE
    )

    companion object {
        private const val REQUEST_CODE = 0x42
        private const val SOUND_BROWSER = "org.github.overtane.soundbrowser"
        private const val SOUND_REQUEST_KEY = "$SOUND_BROWSER.SOUND_REQUEST"
        const val SOUND_REPLY_KEY = "$SOUND_BROWSER.SOUND_REPLY"

        const val MAIN_REQUEST_KEY = "MainAudioSettings"
        const val ALT_REQUEST_KEY = "AltAudioSettings"
        const val AUDIO_STREAM_BUNDLE_KEY = "AudioStream"

        private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

        private val Context.dataStore: DataStore<UserPrefs> by dataStore(
            fileName = DATA_STORE_FILE_NAME,
            serializer = UserPrefsSerializer
        )

        private const val SOUND_BROWSER_ERROR = "SoundBrowser not available"
    }
}


