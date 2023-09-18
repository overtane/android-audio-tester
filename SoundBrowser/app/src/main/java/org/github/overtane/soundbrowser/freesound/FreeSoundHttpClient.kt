package org.github.overtane.soundbrowser.freesound

import android.util.Log
import org.github.overtane.soundbrowser.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val API_KEY = BuildConfig.API_KEY
private const val SERVICE = "https://freesound.org/"

private val DEFAULT_LIST_FILTER = """
    |duration:[10 TO 60]
    | samplerate:[8000 TO 48000]
    | bitdepth:16
    | channels:[1 TO 2]
    | license:"Creative Commons 0"
    """.trimMargin().replace("\n", "")

private const val DEFAULT_LIST_FIELDS = "id,name,duration,samplerate,images"
private const val DEFAULT_DETAILS_FIELDS =
    "id,name,url,duration,samplerate,channels,bitdepth,username,license,previews"

private const val TIMEOUT: Long = 6000

object FreeSoundHttpClient {
    private val instance = HttpClient(CIO) {

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
            engine {
                requestTimeout = TIMEOUT
            }
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.v("Ktor", message)
                }

            }
            level = LogLevel.ALL  // change NONE to ALL for HTTP-logging
        }
        install(ResponseObserver) {
            onResponse { response ->
                Log.d("HTTP status:", "${response.status.value}")
            }
        }
        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    suspend fun search(query: String, page: Int): FreeSoundSearchResponse =
        instance.get(SERVICE) {
            url {
                appendPathSegments("apiv2", "search", "text")
                parameters.append("token", API_KEY)
                parameters.append("query", query)
                parameters.append("page", page.toString())
                parameters.append("filter", DEFAULT_LIST_FILTER)
                parameters.append("fields", DEFAULT_LIST_FIELDS)
            }
        }.body()

    suspend fun getSound(id: Int): FreeSoundDetailsResult =
        instance.get(SERVICE) {
            url {
                appendPathSegments("apiv2", "sounds", id.toString())
                parameters.append("token", API_KEY)
                parameters.append("fields", DEFAULT_DETAILS_FIELDS)
            }
        }.body()
}