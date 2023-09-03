package com.github.overtane.audiotester.datastore

import androidx.datastore.core.DataStore
import com.github.overtane.audiotester.audiostream.AudioSource
import com.github.overtane.audiotester.audiostream.AudioStream as AudioStream
import com.github.overtane.audiotester.audiostream.AudioType as AudioType
import com.github.overtane.audiotester.datastore.UserPrefs.AudioSource as PrefsAudioSource
import com.github.overtane.audiotester.datastore.UserPrefs.AudioStream as PrefsAudioStream
import com.github.overtane.audiotester.datastore.UserPrefs.AudioType as PrefsAudioType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class PreferencesRepository(
    private val dataStore: DataStore<UserPrefs>
) {
    fun get(): MutableList<AudioStream> {
        val prefs = runBlocking { getPrefs() }
        return if (prefs.main.sampleRate == 0) {
            mutableListOf(INIT_MAIN_STREAM, INIT_ALT_STREAM, INIT_EXT_STREAM)
        } else {
            mutableListOf(
                prefs.main.asAudioStream(),
                prefs.alt.asAudioStream(),
                prefs.ext.asAudioStream()
            )
        }
    }

    fun set(streams: MutableList<AudioStream>) {
        runBlocking { setPrefs(streams) }
    }

    private fun PrefsAudioStream.asAudioStream(): AudioStream {
        return AudioStream(
            this.type.asAudioType(),
            this.sampleRate,
            this.channelCount,
            audioSourceFromPrefs(this.source, this.sampleRate, this.channelCount)
        )
    }

    private suspend fun getPrefs(): UserPrefs {
        return try {
            dataStore.data.first()
        } catch (e: Exception) {
            UserPrefs.getDefaultInstance()
        }
    }

    private suspend fun setPrefs(streams: MutableList<AudioStream>) {
        val mainStream = if (streams.size > 0) streams[0] else INIT_MAIN_STREAM
        val altStream = if (streams.size > 1) streams[1] else INIT_ALT_STREAM
        val extStream = if (streams.size > 2) streams[2] else INIT_EXT_STREAM
        dataStore.updateData { prefs ->
            prefs.toBuilder()
                .setMain(
                    prefs.main.toBuilder()
                        .setType(mainStream.type.asPrefsAudioType())
                        .setSampleRate(mainStream.sampleRate)
                        .setChannelCount(mainStream.channelCount)
                        .setSource(
                            prefs.main.source.toBuilder()
                                .setType(mainStream.source.asAudioSourceType())
                                .setDuration(mainStream.source.durationMs)
                                .setFrequency(mainStream.source.frequency())
                                .setName(mainStream.source.name())
                                .setUrl(mainStream.source.url())
                                .build()
                        )
                        .build()
                )
                .setAlt(
                    prefs.alt.toBuilder()
                        .setType(altStream.type.asPrefsAudioType())
                        .setSampleRate(altStream.sampleRate)
                        .setChannelCount(altStream.channelCount)
                        .setSource(
                            prefs.alt.source.toBuilder()
                                .setType(altStream.source.asAudioSourceType())
                                .setDuration(altStream.source.durationMs)
                                .setFrequency(altStream.source.frequency())
                                .build()
                        )
                        .build()
                )
                .setExt(
                    prefs.ext.toBuilder()
                        .setType(PrefsAudioType.ENTERTAINMENT)
                        .setSampleRate(extStream.sampleRate)
                        .setChannelCount(extStream.channelCount)
                        .setSource(
                            prefs.ext.source.toBuilder()
                                .setType(UserPrefs.AudioSourceType.URL)
                                .setDuration(extStream.source.durationMs)
                                .setName((extStream.source as? AudioSource.Sound)?.name)
                                .setUrl((extStream.source as? AudioSource.Sound)?.url)
                                .build()
                        )
                        .build()
                )
                .build()
        }
    }

    private fun PrefsAudioType.asAudioType() =
        when (this) {
            PrefsAudioType.ALERT -> AudioType.ALERT
            PrefsAudioType.ALTERNATE -> AudioType.ALTERNATE
            PrefsAudioType.DEFAULT -> AudioType.DEFAULT
            PrefsAudioType.ENTERTAINMENT -> AudioType.ENTERTAINMENT
            PrefsAudioType.SPEECH_RECOGNITION -> AudioType.SPEECH_RECOGNITION
            PrefsAudioType.TELEPHONY -> AudioType.TELEPHONY
            else -> AudioType.DEFAULT
        }

    private fun AudioType.asPrefsAudioType() =
        when (this) {
            AudioType.ALERT -> PrefsAudioType.ALERT
            AudioType.ALTERNATE -> PrefsAudioType.ALTERNATE
            AudioType.DEFAULT -> PrefsAudioType.DEFAULT
            AudioType.ENTERTAINMENT -> PrefsAudioType.ENTERTAINMENT
            AudioType.SPEECH_RECOGNITION -> PrefsAudioType.SPEECH_RECOGNITION
            AudioType.TELEPHONY -> PrefsAudioType.TELEPHONY
        }

    private fun audioSourceFromPrefs(source: PrefsAudioSource, sampleRate: Int, channelCount: Int) =
        when (source.type) {
            UserPrefs.AudioSourceType.SINE_WAVE ->
                AudioSource.SineWave(
                    source.frequency,
                    sampleRate,
                    channelCount,
                    source.duration
                )

            UserPrefs.AudioSourceType.WHITE_NOISE -> AudioSource.WhiteNoise(source.duration)
            UserPrefs.AudioSourceType.SILENCE -> AudioSource.Silence(source.duration)
            UserPrefs.AudioSourceType.URL -> AudioSource.Sound(
                source.name,
                source.url,
                source.duration
            )

            else -> AudioSource.Nothing
        }

    private fun AudioSource.asAudioSourceType() = when (this) {
        is AudioSource.SineWave -> UserPrefs.AudioSourceType.SINE_WAVE
        is AudioSource.WhiteNoise -> UserPrefs.AudioSourceType.WHITE_NOISE
        is AudioSource.Silence -> UserPrefs.AudioSourceType.SILENCE
        is AudioSource.Sound -> UserPrefs.AudioSourceType.URL
        else -> UserPrefs.AudioSourceType.UNRECOGNIZED
    }

    private fun AudioSource.frequency() = if (this is AudioSource.SineWave) this.freqHz else 0
    private fun AudioSource.name() = if (this is AudioSource.Sound) this.name else ""
    private fun AudioSource.url() = if (this is AudioSource.Sound) this.url else ""

    companion object {
        // Initial stream settings for the first time read
        private const val INIT_DURATION_MS = 10000
        private const val INIT_MAIN_SAMPLE_RATE = 8000
        private const val INIT_ALT_SAMPLE_RATE = 48000
        private const val INIT_MAIN_CHANNEL_COUNT = 2
        private const val INIT_ALT_CHANNEL_COUNT = 1
        private val INIT_MAIN_STREAM =
            AudioStream(
                AudioType.ENTERTAINMENT,
                INIT_MAIN_SAMPLE_RATE,
                INIT_MAIN_CHANNEL_COUNT,
                AudioSource.SineWave(
                    800,
                    INIT_ALT_SAMPLE_RATE,
                    INIT_ALT_CHANNEL_COUNT,
                    INIT_DURATION_MS
                )
            )
        private val INIT_ALT_STREAM =
            AudioStream(
                AudioType.ALTERNATE,
                INIT_ALT_SAMPLE_RATE,
                INIT_ALT_CHANNEL_COUNT,
                AudioSource.SineWave(
                    800,
                    INIT_ALT_SAMPLE_RATE,
                    INIT_ALT_CHANNEL_COUNT,
                    INIT_DURATION_MS
                )
            )
        private val INIT_EXT_STREAM =
            AudioStream(
                type = AudioType.ENTERTAINMENT,
                sampleRate = 0,
                channelCount = 0,
                source = AudioSource.Sound(name = "", url = "", durationMs = 0),
            )
    }
}



