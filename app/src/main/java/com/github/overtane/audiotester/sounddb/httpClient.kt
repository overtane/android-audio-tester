package com.github.overtane.audiotester.sounddb

import io.ktor.client.*
import io.ktor.client.engine.cio.*

object httpClient {

    val client = HttpClient(CIO) {
        expectSuccess = true
    }
}