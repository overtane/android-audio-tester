package com.github.overtane.audiotester.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import com.github.overtane.audiotester.TAG
import com.github.overtane.audiotester.audiotrack.AudioSource
import com.github.overtane.audiotester.audiotrack.AudioStream
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.IOException

class PreferencesRepository(
    private val dataStore: DataStore<StreamPrefs>
) {
    val dataFlow = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferences.", exception)
                emit(StreamPrefs.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun getPrefs() : StreamPrefs {
        return try {
            dataStore.data.first()
        } catch (e : Exception) {
            e.printStackTrace()
            StreamPrefs.getDefaultInstance()
        }
    }

    suspend fun setPrefs() {
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setStreamType(StreamType.MAIN)
                .setType(AudioType.DEFAULT)
                .setChannelCount(2)
                .setSampleRate(16000)
                .build()
        }
    }

    fun get() : MutableList<AudioStream> {
        val prefs = runBlocking { getPrefs() }
        if (prefs.sampleRate == 0) {
            Log.d(TAG, "initialising")
            return mutableListOf(INIT_MAIN_STREAM, INIT_ALT_STREAM)
        } else {
            Log.d(TAG, "reading prefs")
            return mutableListOf(prefs.asAudioStream(), prefs.asAudioStream())
        }
    }

    fun set() {
        runBlocking { setPrefs() }
    }

    fun StreamPrefs.asAudioStream() : AudioStream {
        return AudioStream(
            com.github.overtane.audiotester.audiotrack.AudioType.ENTERTAINMENT,
            this.sampleRate,
            this.channelCount,
            INIT_MAIN_SOURCE
            )
    }

    companion object {
        private const val INIT_DURATION_MS = 10000
        private const val INIT_MAIN_SAMPLE_RATE = 8000
        private const val INIT_ALT_SAMPLE_RATE = 48000
        private const val INIT_MAIN_CHANNEL_COUNT = 2
        private const val INIT_ALT_CHANNEL_COUNT = 1

        private val INIT_MAIN_SOURCE =
            AudioSource.SineWave(
                800,
                INIT_ALT_SAMPLE_RATE,
                INIT_ALT_CHANNEL_COUNT,
                INIT_DURATION_MS
            )
        //AudioSource.WhiteNoise(INIT_DURATION_MS)
        private val INIT_ALT_SOURCE =
            AudioSource.SineWave(
                800,
                INIT_ALT_SAMPLE_RATE,
                INIT_ALT_CHANNEL_COUNT,
                INIT_DURATION_MS
            )

        private val INIT_MAIN_STREAM =
            AudioStream(
                com.github.overtane.audiotester.audiotrack.AudioType.ENTERTAINMENT,
                INIT_MAIN_SAMPLE_RATE,
                INIT_MAIN_CHANNEL_COUNT,
                INIT_MAIN_SOURCE
            )
        private val INIT_ALT_STREAM =
            AudioStream(
                com.github.overtane.audiotester.audiotrack.AudioType.ALTERNATE,
                INIT_ALT_SAMPLE_RATE,
                INIT_ALT_CHANNEL_COUNT,
                INIT_ALT_SOURCE,
            )
    }
}



