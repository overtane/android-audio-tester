package com.github.overtane.audiotester.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.github.overtane.audiotester.R
import com.github.overtane.audiotester.audiotrack.AudioStream
import com.github.overtane.audiotester.databinding.FragmentMainBinding
import com.github.overtane.audiotester.datastore.UserPrefsSerializer
import com.github.overtane.audiotester.datastore.PreferencesRepository
import com.github.overtane.audiotester.datastore.StreamPrefs

class MainFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
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
        when (item.itemId) {
            R.id.menu_about -> {}
            R.id.menu_devices -> {}
            R.id.menu_instructions -> {}
            R.id.menu_recordings -> {}
        }
        return false
    }

    companion object {
        const val MAIN_REQUEST_KEY = "MainAudioSettings"
        const val ALT_REQUEST_KEY = "AltAudioSettings"
        const val AUDIO_STREAM_BUNDLE_KEY = "AudioStream"

        private const val DATA_STORE_FILE_NAME = "user_prefs.pb"

        private val Context.dataStore: DataStore<StreamPrefs> by dataStore(
            fileName = DATA_STORE_FILE_NAME,
            serializer = UserPrefsSerializer
        )
    }
}
