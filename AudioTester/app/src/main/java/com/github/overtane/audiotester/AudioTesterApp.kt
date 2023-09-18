package com.github.overtane.audiotester

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.github.overtane.audiotester.datastore.PreferencesRepository
import com.github.overtane.audiotester.datastore.SoundRepository
import com.github.overtane.audiotester.datastore.UserPrefs
import com.github.overtane.audiotester.datastore.UserPrefsSerializer

class AudioTesterApp : Application() {

    val preferencesRepository by lazy {
        PreferencesRepository(applicationContext.dataStore)
    }
    val soundRepository by lazy {
        SoundRepository(preferencesRepository)
    }

    companion object {
        private const val DATA_STORE_FILE_NAME = "user_prefs.pb"
        private val Context.dataStore: DataStore<UserPrefs> by dataStore(
            fileName = DATA_STORE_FILE_NAME,
            serializer = UserPrefsSerializer
        )

    }
}