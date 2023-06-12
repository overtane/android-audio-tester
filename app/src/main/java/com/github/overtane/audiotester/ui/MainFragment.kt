package com.github.overtane.audiotester.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
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
import com.github.overtane.audiotester.MainActivity
import com.github.overtane.audiotester.Manifest
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.audiostream.AudioStream
import com.github.overtane.audiotester.databinding.FragmentMainBinding
import com.github.overtane.audiotester.datastore.UserPrefsSerializer
import com.github.overtane.audiotester.datastore.PreferencesRepository
import com.github.overtane.audiotester.datastore.UserPrefs

class MainFragment : Fragment(), MenuProvider {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(PreferencesRepository(requireContext().dataStore))
        )[MainViewModel::class.java]
        // bind ui-data to viewModel instance (left: xml-data, right: viewModel object)
        binding.viewModel = viewModel

        (requireActivity() as MenuHost).addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use fragment result listeners to get user input from settings fragment
        setFragmentResultListener(MAIN_REQUEST_KEY) { _, bundle ->
            val result = bundle.getParcelable<AudioStream>(AUDIO_STREAM_BUNDLE_KEY)
            result?.let {
                viewModel.setMainAudio(it)
            }
        }

        setFragmentResultListener(ALT_REQUEST_KEY) { _, bundle ->
            val result = bundle.getParcelable<AudioStream>(AUDIO_STREAM_BUNDLE_KEY)
            result?.let {
                viewModel.setAltAudio(it)
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_overflow_menu, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, findNavController())
    }

    companion object {
        const val MAIN_REQUEST_KEY = "MainAudioSettings"
        const val ALT_REQUEST_KEY = "AltAudioSettings"
        const val AUDIO_STREAM_BUNDLE_KEY = "AudioStream"

        private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

        private val Context.dataStore: DataStore<UserPrefs> by dataStore(
            fileName = DATA_STORE_FILE_NAME,
            serializer = UserPrefsSerializer
        )
    }
}
